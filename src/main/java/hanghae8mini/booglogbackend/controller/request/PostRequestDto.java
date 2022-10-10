package hanghae8mini.booglogbackend.controller.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.Column;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostRequestDto {

    private String title;
    private String bookTitle;
    private String author;
    private String content;
    private String category;
    private String nickname;
    private MultipartFile imageUrl;

    // 테스트용
    public PostRequestDto(String category, String nickname) {
        this.title = "글제목";
        this.bookTitle = "책제목";
        this.author = "글작가";
        this.content = "글내용";
        this.imageUrl = null;
        this.category = category;
        this.nickname = nickname;
    }
}
