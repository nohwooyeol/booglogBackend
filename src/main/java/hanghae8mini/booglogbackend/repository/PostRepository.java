package hanghae8mini.booglogbackend.repository;

import com.querydsl.core.types.Projections;
import hanghae8mini.booglogbackend.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import static hanghae8mini.booglogbackend.domain.QPost.post;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findAllByOrderByModifiedAtDesc();

    // 비로그인 메인페이지 조회
    @Query(value = "select * from post order by view desc limit :size", nativeQuery = true)
    public List<Post> PostAllRandom(@Param(value = "size") int size);

    @Query(value ="SELECT distinct * " +
            "FROM post " +
            "WHERE category IN ( " +
            "        select distinct p.category " +
            "        from post as p " +
            "                 join member as m " +
            "                      on p.member_member_id = m.member_id " +
            "        where m.member_id = :memberId" +
            "    ) " +
            "ORDER BY view desc " +
            "limit :size offset :lastPostId ", nativeQuery = true)
    public List<Post> findAllByMemberIdAndCategory(@Param(value = "memberId") Long memberId,
                                              @Param(value = "lastPostId") Long lastPostId,
                                              @Param(value = "size") int size);

    public List<Post> findAllByMemberMemberId(Long memberid);

    // 조회수
    @Modifying
    @Query("update Post set view = view + 1 where postId = :postId")
    int updateView(@Param(value = "postId") Long postId);

    // 게시글 작성자 아이디로 찾기
    Post findByMemberMemberId(Long memberId);

    // 게시글 아이디로 찾기
    Post findByPostId(Long postId);


}
