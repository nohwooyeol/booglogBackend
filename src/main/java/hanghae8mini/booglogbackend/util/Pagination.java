package hanghae8mini.booglogbackend.util;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Pagination {

    private int lastPostId; // 현재 페이지에 그려진 게시물 중 가장 작은 값. (역순이기 떄문에, 가장 아래 있는 글이 가장 오래되고 번호가 작음)
    private int size; // 가져올 글의 개수

}
