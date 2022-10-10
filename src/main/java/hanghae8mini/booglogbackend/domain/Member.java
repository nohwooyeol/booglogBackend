
package hanghae8mini.booglogbackend.domain;

import hanghae8mini.booglogbackend.controller.request.MemberRequestDto;
import hanghae8mini.booglogbackend.domain.Timestamped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
public class Member extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @Column(nullable = false)
    private String account;  // 로그인 아이디

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nickname;  // 서비스 내 사용하는 닉네임

    @Column
    private String imageUrl;

    public Member(MemberRequestDto requestDto, String imageUrl) {
        this.account = requestDto.getAccount();
        this.password = requestDto.getPassword();
        this.nickname = requestDto.getNickname();
        this.imageUrl = imageUrl;
    }



}

