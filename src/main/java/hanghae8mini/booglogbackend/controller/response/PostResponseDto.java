package hanghae8mini.booglogbackend.controller.response;

import hanghae8mini.booglogbackend.domain.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostResponseDto {

    private Long postId;
    private String title;
    private String bookTitle;
    private String content;
    private String author;
    private String nickname;
    private String imageUrl;
    private Category category;
    private LocalDateTime createdAt;
    private List<CommentResponseDto> commentResponseDtoList;
    private int view;

    // private List<CommentResponseDto> commentResponseDtoList;


    @Override
    public String toString() {
        return "PostResponseDto{" +
                "postId=" + postId +
                ", title='" + title + '\'' +
                ", bookTitle='" + bookTitle + '\'' +
                ", content='" + content + '\'' +
                ", author='" + author + '\'' +
                ", nickname='" + nickname + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", view='" + view + '\'' +
                ", category='" + category + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
