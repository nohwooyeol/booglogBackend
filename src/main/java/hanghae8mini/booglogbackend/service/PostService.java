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
            return ResponseDto.fail("INVALID_TOKEN", "Token이 유효하지 않습니다.");
        }

        Category categoryEnum = null;

        try{
            categoryEnum = Category.valueOf(requestDto.getCategory());
        } catch(IllegalArgumentException e) {
            return ResponseDto.fail("BAD_REQUEST", "카테고리에 없는 항목입니다.");
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
        return ResponseDto.success(true,"작성이 완료되었습니다.");
    }

    // 게시글 상세조회
    @Transactional(readOnly = true)
    public ResponseDto<?> getPost(Long postId, HttpServletRequest request) {

        // 테스트용
        //Member member = validationMemberById(1l);
//        Member member = validationMemberById(2l);

        Member member = checkMemberUtil.validateMember(request);

        // 게시글 상세조회가 아닌 경우(수정페이지) 로그인 여부 체크하기
        if(!"/api/post".equals(request.getRequestURI().substring(0,9))){
            if (null == member) {
                return ResponseDto.fail("INVALID_TOKEN", "Token이 유효하지 않습니다.");
            }
        }

        Post post = checkMemberUtil.isPresentPost(postId);
        if(!"/api/post".equals(request.getRequestURI().substring(0,9))){
            if(!post.getMember().getMemberId().equals(member.getMemberId())){
                return ResponseDto.fail("BAD_REQUEST", "작성자만 수정할 수 있습니다.");
            }
        }

        if (null == post) {
            return ResponseDto.fail("NOT_FOUND", "존재하지 않는 게시글 id 입니다.");
        }

        List<Comment> commentList = commentRepository.findAllByPost(post);  // comment List 데려오기
        List<CommentResponseDto> commentResponseDtoList = new ArrayList<>();  // 최종 보여줄 댓글 꾸러미

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

    // 게시글 메인페이지 목록조회
    @Transactional(readOnly = true)
    public ResponseDto<?> getAllPost(Long lastPostId, int size, HttpServletRequest request) {

        Member member = checkMemberUtil.validateMember(request);
        List<PostResponseDto> postResponseDtoList = new ArrayList<>();
        List<Post> postList = new ArrayList<>();
        // 비로그인 로직
        if (member == null) {
            List<Post> temp = new ArrayList<>();
            postList = postRepository.PostAllRandom(size);
        } else { // 로그인 로직
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

    // 작성자의 게시글 리스트

    @Transactional(readOnly = true)
    @LoginCheck
    public ResponseDto<?> getAllPostByMember(HttpServletRequest request) {

        Member member = checkMemberUtil.validateMember(request);
        if (null == member) {
            return ResponseDto.fail("INVALID_TOKEN", "Token이 유효하지 않습니다.");
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
            return ResponseDto.fail("INVALID_TOKEN", "Token이 유효하지 않습니다.");
        }
        Category categoryEnum = null;

        Post post = postRepository.findByPostId(postId);

        if(!post.getMember().getMemberId().equals(member.getMemberId())){
            return ResponseDto.fail("BAD_REQUEST", "작성자만 수정할 수 있습니다.");
        }
        try{
            categoryEnum = Category.valueOf(requestDto.getCategory());
        } catch(IllegalArgumentException e) {
            return ResponseDto.fail("BAD_REQUEST", "카테고리에 없는 항목입니다.");
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
        return ResponseDto.success(true, "수정이 완료되었습니다.");

    }

    @Transactional
    @LoginCheck
    public ResponseDto<?> deletePost(Long postId, HttpServletRequest request) {

        Member member = checkMemberUtil.validateMember(request);
        if (null == member) {
            return ResponseDto.fail("INVALID_TOKEN", "Token이 유효하지 않습니다.");
        }
        Post post = postRepository.findByPostId(postId);

        if(!post.getMember().getMemberId().equals(member.getMemberId())){
            return ResponseDto.fail("BAD_REQUEST", "작성자만 삭제할 수 있습니다.");
        }
        postRepository.deleteById(postId);
        return ResponseDto.success(true, "삭제가 완료되었습니다.");
    }

    @Transactional
    public int updateView(Long postId, HttpServletRequest request, HttpServletResponse response) {

        Cookie[] cookies = request.getCookies();
        boolean checkCookie = false;
        int result = 0;
        if(cookies != null){
            for (Cookie cookie : cookies)
            {
                // 이미 조회를 한 경우 체크
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
    * 조회수 중복 방지를 위한 쿠키 생성 메소드
    * @param cookie
    * @return
    * */
    private Cookie createCookieForForNotOverlap(Long postId) {
        Cookie cookie = new Cookie(VIEWCOOKIENAME+postId, String.valueOf(postId));
        cookie.setComment("조회수 중복 증가 방지 쿠키");	// 쿠키 용도 설명 기재
        cookie.setMaxAge(getRemainSecondForTommorow()); 	// 하루를 준다.
        cookie.setHttpOnly(true);				// 서버에서만 조작 가능
        return cookie;
    }

    // 다음 날 정각까지 남은 시간(초)
    private int getRemainSecondForTommorow() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tommorow = LocalDateTime.now().plusDays(1L).truncatedTo(ChronoUnit.DAYS);
        return (int) now.until(tommorow, ChronoUnit.SECONDS);
    }
}
