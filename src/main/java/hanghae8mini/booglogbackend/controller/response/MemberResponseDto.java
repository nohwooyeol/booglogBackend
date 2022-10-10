package hanghae8mini.booglogbackend.controller.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponseDto {
    private String account;
    private String nickname;

    //토큰값 바디에 담을 변수
    private String imageUrl;
    private String accessToken; //엑세스토큰값
    private String refreshToken; //리프레쉬토큰값
}
