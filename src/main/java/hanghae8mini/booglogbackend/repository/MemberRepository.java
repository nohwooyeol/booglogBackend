package hanghae8mini.booglogbackend.repository;

import hanghae8mini.booglogbackend.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {


    Optional<Member> findByNickname(String nickname);

    Optional<Member> findByMemberId(Long memberId);

    Optional<Member> findByAccount(String account);

}
