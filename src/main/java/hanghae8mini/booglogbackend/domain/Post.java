package hanghae8mini.booglogbackend.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;
import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Post extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String bookTitle;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false)
    private String content;

    @Column(nullable = true)
    private String imageUrl;

    @Column(columnDefinition = "integer default 0", nullable = false)
    private int view; //조회수

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> commentList;

    @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;


    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Category category;

    public Post(Long postId, String title, String bookTitle, String author, String content, String imageUrl, Category category) {
        this.title = title;
        this.bookTitle = bookTitle;
        this.author = author;
        this.content = content;
        this.imageUrl = imageUrl;
        this.category = category;
    }
}
