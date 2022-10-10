package hanghae8mini.booglogbackend.service;

import hanghae8mini.booglogbackend.annotation.LoginCheck;
import hanghae8mini.booglogbackend.controller.request.CommentRequestDto;
import hanghae8mini.booglogbackend.controller.response.CommentResponseDto;
import hanghae8mini.booglogbackend.controller.response.ResponseDto;
import hanghae8mini.booglogbackend.domain.Comment;
import hanghae8mini.booglogbackend.domain.Member;
import hanghae8mini.booglogbackend.domain.Post;
import hanghae8mini.booglogbackend.repository.CommentRepository;
import hanghae8mini.booglogbackend.repository.MemberRepository;
import hanghae8mini.booglogbackend.repository.PostRepository;
import hanghae8mini.booglogbackend.util.CheckMemberUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final CheckMemberUtil checkMemberUtil;
    private final MemberRepository memberRepository;

    // 댓글 작성
    @LoginCheck
    @Transactional
    public ResponseDto<?> createComment(Long postId, CommentRequestDto requestDto, HttpServletRequest request) {

        Member member = checkMemberUtil.validateMember(request);
        if (null == member) {
            return ResponseDto.fail("INVALID_TOKEN", "Token이 유효하지 않습니다.");
        }

        Post post = postRepository.findByPostId(postId);
        if (null == post) {
            return ResponseDto.fail("NOT_FOUND", "존재하지 않는 게시글 id 입니다.");
        }

        Comment comment = Comment.builder()
                .post(post)
                .member(member)
                .nickname(member.getNickname())
                .content(requestDto.getContent())
                .build();
        commentRepository.save(comment);
        return ResponseDto.success(
                CommentResponseDto.builder()
                        .commentId(comment.getCommentId())
                        .nickname(comment.getNickname())
                        .content(comment.getContent())
                        .createdAt(comment.getCreatedAt())
                        .build()
        );
    }

    // 댓글 수정
    @Transactional
    @LoginCheck
    public ResponseDto<?> updateComment(Long commentId, CommentRequestDto requestDto, HttpServletRequest request) {
        // 1. check login status
        // 2. validate user info
        // 3. check post is present or not
        // 4. check comment is present or not
        // 5. check the user whether the user is the writer

        Member member = checkMemberUtil.validateMember(request);
        if (null == member) {
            return ResponseDto.fail("INVALID_TOKEN", "Token이 유효하지 않습니다.");
        }

        Optional<Comment> optionalComment = commentRepository.findById(commentId);
        Comment comment = optionalComment.orElse(null);
        if (null == comment) {
            return ResponseDto.fail("NOT_FOUND", "존재하지 않는 댓글 ID 입니다.");
        }

        comment.update(requestDto);
        return ResponseDto.success(
                CommentResponseDto.builder()
                        .commentId(comment.getCommentId())
                        .nickname(comment.getNickname())
                        .content(comment.getContent())
                        .modifiedAt(comment.getModifiedAt())
                        .build()
        );


    }

    @Transactional
    @LoginCheck
    public ResponseDto<?> deleteComment(Long commentId, HttpServletRequest request) {

        Member member = checkMemberUtil.validateMember(request);
        if (null == member) {
            return ResponseDto.fail("INVALID_TOKEN", "Token이 유효하지 않습니다.");
        }

        Optional<Comment> optionalComment = commentRepository.findById(commentId);
        Comment comment = optionalComment.orElse(null);
        if (null == comment) {
            return ResponseDto.fail("NOT_FOUND", "존재하지 않는 댓글 ID 입니다.");
        }

        commentRepository.delete(comment);
        return ResponseDto.success(true, "삭제가 완료되었습니다.");
    }
}
