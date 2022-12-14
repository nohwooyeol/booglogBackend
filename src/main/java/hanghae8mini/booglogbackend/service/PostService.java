package hanghae8mini.booglogbackend.service;



import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import hanghae8mini.booglogbackend.annotation.LoginCheck;
import hanghae8mini.booglogbackend.controller.request.PostRequestDto;
import hanghae8mini.booglogbackend.controller.response.CommentResponseDto;
import hanghae8mini.booglogbackend.controller.response.PostResponseDto;
import hanghae8mini.booglogbackend.controller.response.ResponseDto;
import hanghae8mini.booglogbackend.domain.Category;
import hanghae8mini.booglogbackend.domain.Comment;
import hanghae8mini.booglogbackend.domain.Member;
import hanghae8mini.booglogbackend.domain.Post;
import hanghae8mini.booglogbackend.repository.CommentRepository;
import hanghae8mini.booglogbackend.repository.MemberRepository;
import hanghae8mini.booglogbackend.repository.PostCustomRepository;
import hanghae8mini.booglogbackend.repository.PostRepository;
import hanghae8mini.booglogbackend.shared.CommonUtils;
import hanghae8mini.booglogbackend.util.CheckMemberUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final CheckMemberUtil checkMemberUtil;

    private final PostCustomRepository postCustomRepository;
    private final CommentRepository commentRepository;

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;
    private final static String VIEWCOOKIENAME = "alreadyViewCookie";

    @LoginCheck
    @Transactional
    public ResponseDto<?> createPost(PostRequestDto requestDto, HttpServletRequest request) throws IOException {

        Member member = checkMemberUtil.validateMember(request);

        if (null == member) {
            return ResponseDto.fail("INVALID_TOKEN", "Token??? ???????????? ????????????.");
        }

        Category categoryEnum = null;

        try{
            categoryEnum = Category.valueOf(requestDto.getCategory());
        } catch(IllegalArgumentException e) {
            return ResponseDto.fail("BAD_REQUEST", "??????????????? ?????? ???????????????.");
        }

        String imgUrl = null;

        if (!requestDto.getImageUrl().isEmpty()) {
            String fileName = CommonUtils.buildFileName(requestDto.getImageUrl().getOriginalFilename());
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(requestDto.getImageUrl().getContentType());
            amazonS3Client.putObject(new PutObjectRequest(bucketName, fileName, requestDto.getImageUrl().getInputStream(), objectMetadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
            imgUrl = amazonS3Client.getUrl(bucketName, fileName).toString();
            String[] nameWithNoS3info = imgUrl.split(".com/");
            imgUrl = nameWithNoS3info[1];
        }


        Post post = Post.builder()
                .member(member)
                .title(requestDto.getTitle())
                .bookTitle(requestDto.getBookTitle())
                .content(requestDto.getContent())
                .imageUrl(imgUrl)
                .category(categoryEnum)
                .author(requestDto.getAuthor())
                .build();
        postRepository.save(post);
        return ResponseDto.success(true,"????????? ?????????????????????.");
    }

    // ????????? ????????????
    @Transactional(readOnly = true)
    public ResponseDto<?> getPost(Long postId, HttpServletRequest request) {

        // ????????????
        //Member member = validationMemberById(1l);
//        Member member = validationMemberById(2l);

        Member member = checkMemberUtil.validateMember(request);

        // ????????? ??????????????? ?????? ??????(???????????????) ????????? ?????? ????????????
        if(!"/api/post".equals(request.getRequestURI().substring(0,9))){
            if (null == member) {
                return ResponseDto.fail("INVALID_TOKEN", "Token??? ???????????? ????????????.");
            }
        }

        Post post = checkMemberUtil.isPresentPost(postId);
        if(!"/api/post".equals(request.getRequestURI().substring(0,9))){
            if(!post.getMember().getMemberId().equals(member.getMemberId())){
                return ResponseDto.fail("BAD_REQUEST", "???????????? ????????? ??? ????????????.");
            }
        }

        if (null == post) {
            return ResponseDto.fail("NOT_FOUND", "???????????? ?????? ????????? id ?????????.");
        }

        List<Comment> commentList = commentRepository.findAllByPost(post);  // comment List ????????????
        List<CommentResponseDto> commentResponseDtoList = new ArrayList<>();  // ?????? ????????? ?????? ?????????

        for (Comment comment : commentList) {
            commentResponseDtoList.add(
                    CommentResponseDto.builder()
                            .commentId(comment.getCommentId())
                            .nickname(comment.getNickname())
                            .content(comment.getContent())
                            .createdAt(comment.getCreatedAt())
                            .modifiedAt(comment.getModifiedAt())
                            .build()
            );
        }

        return ResponseDto.success(
                PostResponseDto.builder()
                        .postId(post.getPostId())
                        .title(post.getTitle())
                        .bookTitle(post.getBookTitle())
                        .author(post.getAuthor())
                        .nickname(post.getMember().getNickname())
                        .category(post.getCategory())
                        .content(post.getContent())
                        .imageUrl(post.getImageUrl())
                        .view(post.getView())
                        .commentResponseDtoList(commentResponseDtoList)
                        .createdAt(post.getCreatedAt())
                        .build()
        );
    }

    // ????????? ??????????????? ????????????
    @Transactional(readOnly = true)
    public ResponseDto<?> getAllPost(Long lastPostId, int size, HttpServletRequest request) {

        Member member = checkMemberUtil.validateMember(request);
        List<PostResponseDto> postResponseDtoList = new ArrayList<>();
        List<Post> postList = new ArrayList<>();
        // ???????????? ??????
        if (member == null) {
            List<Post> temp = new ArrayList<>();
            postList = postRepository.PostAllRandom(size);
        } else { // ????????? ??????
            postList = postRepository.findAllByMemberIdAndCategory(1l, lastPostId, size);
        }
        postResponseDtoList = postList.stream().map((post) -> PostResponseDto.builder()
                .postId(post.getPostId())
                .title(post.getTitle())
                .bookTitle(post.getBookTitle())
                .author(post.getAuthor())
                .nickname(post.getMember().getNickname())
                .category(post.getCategory())
                .content(post.getContent())
                .imageUrl(post.getImageUrl())
                .view(post.getView())
                .createdAt(post.getCreatedAt())
                .build()).collect(Collectors.toList());

        return ResponseDto.success(postResponseDtoList);

    }

    // ???????????? ????????? ?????????

    @Transactional(readOnly = true)
    @LoginCheck
    public ResponseDto<?> getAllPostByMember(HttpServletRequest request) {

        Member member = checkMemberUtil.validateMember(request);
        if (null == member) {
            return ResponseDto.fail("INVALID_TOKEN", "Token??? ???????????? ????????????.");
        }
        List<Post> postList = postRepository.findAllByMemberMemberId(member.getMemberId());
        List<PostResponseDto> postResponseDtoList = new ArrayList<>();
        for (Post post : postList) {

            postResponseDtoList.add(
                    PostResponseDto.builder()
                            .postId(post.getPostId())
                            .title(post.getTitle())
                            .bookTitle(post.getBookTitle())
                            .author(post.getAuthor())
                            .nickname(post.getMember().getNickname())
                            .category(post.getCategory())
                            .content(post.getContent())
                            .imageUrl(post.getImageUrl())
                            .view(post.getView())
                            .createdAt(post.getCreatedAt())
                            .build()
            );
        }

        return ResponseDto.success(postResponseDtoList);
    }

    @Transactional
    @LoginCheck
    public ResponseDto<?> updatePost(Long postId, PostRequestDto requestDto, HttpServletRequest request) {

        Member member = checkMemberUtil.validateMember(request);
        if (null == member) {
            return ResponseDto.fail("INVALID_TOKEN", "Token??? ???????????? ????????????.");
        }
        Category categoryEnum = null;

        Post post = postRepository.findByPostId(postId);

        if(!post.getMember().getMemberId().equals(member.getMemberId())){
            return ResponseDto.fail("BAD_REQUEST", "???????????? ????????? ??? ????????????.");
        }
        try{
            categoryEnum = Category.valueOf(requestDto.getCategory());
        } catch(IllegalArgumentException e) {
            return ResponseDto.fail("BAD_REQUEST", "??????????????? ?????? ???????????????.");
        }

        post = Post.builder()
                .title(requestDto.getTitle())
                .bookTitle(requestDto.getBookTitle())
                .content(requestDto.getContent())
                .imageUrl(post.getImageUrl())
                .category(categoryEnum)
                .author(requestDto.getAuthor())
                .build();
        postRepository.save(post);
        return ResponseDto.success(true, "????????? ?????????????????????.");

    }

    @Transactional
    @LoginCheck
    public ResponseDto<?> deletePost(Long postId, HttpServletRequest request) {

        Member member = checkMemberUtil.validateMember(request);
        if (null == member) {
            return ResponseDto.fail("INVALID_TOKEN", "Token??? ???????????? ????????????.");
        }
        Post post = postRepository.findByPostId(postId);

        if(!post.getMember().getMemberId().equals(member.getMemberId())){
            return ResponseDto.fail("BAD_REQUEST", "???????????? ????????? ??? ????????????.");
        }
        postRepository.deleteById(postId);
        return ResponseDto.success(true, "????????? ?????????????????????.");
    }

    @Transactional
    public int updateView(Long postId, HttpServletRequest request, HttpServletResponse response) {

        Cookie[] cookies = request.getCookies();
        boolean checkCookie = false;
        int result = 0;
        if(cookies != null){
            for (Cookie cookie : cookies)
            {
                // ?????? ????????? ??? ?????? ??????
                if (cookie.getName().equals(VIEWCOOKIENAME+postId)) checkCookie = true;

            }
            if(!checkCookie){
                Cookie newCookie = createCookieForForNotOverlap(postId);
                response.addCookie(newCookie);
                result = postRepository.updateView(postId);
            }
        } else {
            Cookie newCookie = createCookieForForNotOverlap(postId);
            response.addCookie(newCookie);
            result = postRepository.updateView(postId);
        }
        return result;
    }


    /*
    * ????????? ?????? ????????? ?????? ?????? ?????? ?????????
    * @param cookie
    * @return
    * */
    private Cookie createCookieForForNotOverlap(Long postId) {
        Cookie cookie = new Cookie(VIEWCOOKIENAME+postId, String.valueOf(postId));
        cookie.setComment("????????? ?????? ?????? ?????? ??????");	// ?????? ?????? ?????? ??????
        cookie.setMaxAge(getRemainSecondForTommorow()); 	// ????????? ??????.
        cookie.setHttpOnly(true);				// ??????????????? ?????? ??????
        return cookie;
    }

    // ?????? ??? ???????????? ?????? ??????(???)
    private int getRemainSecondForTommorow() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tommorow = LocalDateTime.now().plusDays(1L).truncatedTo(ChronoUnit.DAYS);
        return (int) now.until(tommorow, ChronoUnit.SECONDS);
    }
}
