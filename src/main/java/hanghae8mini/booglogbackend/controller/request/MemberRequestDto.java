package hanghae8mini.booglogbackend.controller.request;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberRequestDto {

    @NotEmpty(message = "아이디 입력은 필수입니다.")
    @Size(min = 6, max = 12, message = "아이디의 길이는 6자에서 12자 사이입니다.")
    @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "영문 대/소 문자 및 숫자만 사용")
    private String account;

    @NotEmpty(message = "닉네임 입력은 필수입니다.")
    @Size(min = 3, max = 8, message = "닉네임의 길이는 3자에서 8글자 사이입니다.")
    @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "영문 대/소 문자 및 숫자만 사용")
    private String nickname;

    @NotEmpty(message = "비밀번호 입력은 필수 입니다.")
    @Size(min = 6, max = 12, message = "비밀번호길이는 6자에서 12자 사이입니다.")
    @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "영문 대/소 문자 및 숫자만 사용")
    private String password;

    @NotEmpty(message = "비밀번호 확인 입력은 필수 입니다.")
    @Size(min = 6, max = 12, message = "비밀번호길이는 6자에서 12자 사이입니다.")
    @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "영문 대/소 문자 및 숫자만 사용")
    private String passwordCheck;

    private MultipartFile imageUrl;


}
