package hanghae8mini.booglogbackend.repository;

import hanghae8mini.booglogbackend.domain.Comment;
import hanghae8mini.booglogbackend.domain.Post;
import hanghae8mini.booglogbackend.domain.Comment;
import hanghae8mini.booglogbackend.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByPost(Post post);
}
