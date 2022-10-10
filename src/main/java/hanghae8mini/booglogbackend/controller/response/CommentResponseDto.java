package hanghae8mini.booglogbackend.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponseDto {
    private Long commentId;  // pk
    private String nickname;  // 댓글 작성자
    private String content;  // 댓글 내용
    private LocalDateTime createdAt;  // 생성일
    private LocalDateTime modifiedAt;  // 수정일


}


