package hanghae8mini.booglogbackend.repository;

import hanghae8mini.booglogbackend.domain.Member;
import hanghae8mini.booglogbackend.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByMember(Member member);
}
