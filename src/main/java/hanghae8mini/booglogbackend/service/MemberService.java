package hanghae8mini.booglogbackend.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import hanghae8mini.booglogbackend.controller.request.MemberRequestDto;
import hanghae8mini.booglogbackend.controller.requestDto.LoginRequestDto;
import hanghae8mini.booglogbackend.controller.requestDto.TokenDto;
import hanghae8mini.booglogbackend.controller.response.ResponseDto;
import hanghae8mini.booglogbackend.controller.responseDto.MemberResponseDto;
import hanghae8mini.booglogbackend.domain.Member;
import hanghae8mini.booglogbackend.repository.MemberRepository;
import hanghae8mini.booglogbackend.shared.CommonUtils;
import hanghae8mini.booglogbackend.utils.Jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;

    private final PasswordEncoder passwordEncoder;

    private final TokenProvider tokenProvider;

    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    private final String cloudFrontDomain = "https://d1ig9s8koiuspp.cloudfront.net/";

//    @Transactional
//    public ResponseDto<?> signUp(MemberRequestDto requestDto) { //회원가입
//        if (null != isPresentAccount(requestDto.getAccount())) {
//            return ResponseDto.fail("DUPLICATED_ACCOUNT",
//                    "중복된 아이디 입니다.");
//        }
//        if (!requestDto.getPassword().equals(requestDto.getPasswordCheck())) {
//            return ResponseDto.fail("PASSWORDS_NOT_MATCHED",
//                    "비밀번호와 비밀번호 확인이 일치하지 않습니다.");
//        }
//
//        Member member = Member.builder()
//                .account(requestDto.getAccount())
//                .password(passwordEncoder.encode(requestDto.getPassword())) //복호화 저장
//                .nickname(requestDto.getNickname())
//                .imageUrl(requestDto.getImageUrl())
//                .build();
//        memberRepository.save(member);
//        return ResponseDto.success("회원가입 성공");
//    }

    // 회원가입 (사진 동시 등록 버전)
    @Transactional
    public ResponseDto<?> signUp(@ModelAttribute MemberRequestDto requestDto) throws IOException { //회원가입

        if (null != isPresentAccount(requestDto.getAccount())) {
            return ResponseDto.fail("DUPLICATED_ACCOUNT",
                    "중복된 아이디 입니다.");
        }
        if (!requestDto.getPassword().equals(requestDto.getPasswordCheck())) {
            return ResponseDto.fail("PASSWORDS_NOT_MATCHED",
                    "비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        String imgUrl = null;

        if (!requestDto.getImageUrl().isEmpty()) {
            String fileName = CommonUtils.buildFileName(requestDto.getImageUrl().getOriginalFilename());
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(requestDto.getImageUrl().getContentType());
            amazonS3Client.putObject(new PutObjectRequest(bucketName, fileName, requestDto.getImageUrl().getInputStream(), objectMetadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
            imgUrl = amazonS3Client.getUrl(bucketName, fileName).toString();
            String[] nameWithNoS3info = imgUrl.split(".com/");
            imgUrl = nameWithNoS3info[1];
        }

        Member member = Member.builder()
                .account(requestDto.getAccount())
                .password(passwordEncoder.encode(requestDto.getPassword())) //복호화 저장
                .nickname(requestDto.getNickname())
                .imageUrl(imgUrl)
                .build();
        memberRepository.save(member);
        return ResponseDto.success("회원가입 성공");
    }

    @Transactional
    public ResponseDto<?> login(LoginRequestDto requestDto, HttpServletResponse response) {
        Member member = isPresentAccount(requestDto.getAccount());
        if (null == member) {
            return ResponseDto.fail("MEMBER_NOT_FOUND",
                    "사용자를 찾을 수 없습니다.");
        }

        if(!passwordEncoder.matches(requestDto.getPassword(), member.getPassword())){
            return ResponseDto.fail("PASSWORD","비밀번호가 같지 않습니다");
        }

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(requestDto.getAccount(), requestDto.getPassword());
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        TokenDto tokenDto = tokenProvider.generateTokenDto(authentication);
        tokenToHeaders(tokenDto, response);

        Optional<Member> member1 = memberRepository.findByAccount(requestDto.getAccount());

        String imageUrl = member1.get().getImageUrl();

        //기존 방식
//        return ResponseDto.success(
//                MemberResponseDto.builder()
//                        .account(member.getAccount())
//                        .nickname(member.getNickname())
//                        .build()
//        );
        //바디에 넣어서 보내는 형식

        return ResponseDto.success(
                MemberResponseDto.builder()
                        .account(member.getAccount())
                        .nickname(member.getNickname())
                        .accessToken(tokenDto.getAccessToken())
                        .refreshToken(tokenDto.getRefreshToken())
                        .imageUrl(imageUrl)
                        .build()
        );
    }


    @Transactional
    public ResponseDto<?> idCheck(String account) { //아이디 중복체크
        Optional<Member> optionalMember = memberRepository.findByAccount(account);
        if (optionalMember.isPresent()) {
            return ResponseDto.fail("DUPLICATED_ACCOUNT", "중복된 아이디 입니다.");
        }
        return ResponseDto.success("가입 가능한 아이디입니다.");
    }

    @Transactional
    public ResponseDto<?> nicknameCheck(String nickname) { //닉네임 중복체크
        Optional<Member> optionalMember = memberRepository.findByNickname(nickname);
        if (optionalMember.isPresent()) {
            return ResponseDto.fail("DUPLICATED_NICKNAME", "중복된 닉네임 입니다.");
        }
        return ResponseDto.success("가입 가능한 닉네임입니다.");
    }

    @Transactional(readOnly = true)
    public Member isPresentAccount(String account) {
        Optional<Member> optionalMember = memberRepository.findByAccount(account);
        return optionalMember.orElse(null);
    }
    @Transactional
    public void tokenToHeaders(TokenDto tokenDto, HttpServletResponse response) {
        response.addHeader("Authorization", "Bearer " + tokenDto.getAccessToken());
        response.addHeader("RefreshToken", tokenDto.getRefreshToken());
        response.addHeader("Access-Token-Expire-Time", tokenDto.getAccessTokenExpiresIn().toString());
    }



    public ResponseDto<?> logout(HttpServletRequest request) {
        if (!tokenProvider.validateToken(request.getHeader("RefreshToken"))) {
            return ResponseDto.fail("INVALID_TOKEN", "refresh token is invalid");
        }
        Member member = tokenProvider.getMemberFromAuthentication();
        if (null == member) {
            return ResponseDto.fail("MEMBER_NOT_FOUND",
                    "member not found");
        }

        return tokenProvider.deleteRefreshToken(member);
    }
}
