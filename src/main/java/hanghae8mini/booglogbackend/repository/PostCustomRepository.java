package hanghae8mini.booglogbackend.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import hanghae8mini.booglogbackend.domain.Post;
import static hanghae8mini.booglogbackend.domain.QPost.*;
import static hanghae8mini.booglogbackend.domain.QMember.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PostCustomRepository {

    private JPAQueryFactory jpaQueryFactory;

  /*  // 게시글의 댓글 전체 가져오기
    public List<Post> findAllByPostRandom(){
        return jpaQueryFactory.select(*, rand())
                .leftJoin(comment.parent)
                .fetchJoin()
                .where(comment.post.id.eq(post.getId()))
                .orderBy(comment.parent.id.asc().nullsFirst(), comment.createdAt.asc())
                .fetch();
        return null;
    }*/

    // 로그인 메인페이지 조회(작성자의 독서 카테고리 별)
    public List<Post> PostByCategoryMain(Long memberId, Long lastPostId, int size ){

        return jpaQueryFactory.selectDistinct(Projections.constructor(Post.class))
                .from(post)
                .join(member)
                .on(member.memberId.eq(post.member.memberId))
                .where(member.memberId.eq(memberId))
                .fetch();
    }
}
