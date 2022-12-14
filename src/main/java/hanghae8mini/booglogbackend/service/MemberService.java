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
//    public ResponseDto<?> signUp(MemberRequestDto requestDto) { //????????????
//        if (null != isPresentAccount(requestDto.getAccount())) {
//            return ResponseDto.fail("DUPLICATED_ACCOUNT",
//                    "????????? ????????? ?????????.");
//        }
//        if (!requestDto.getPassword().equals(requestDto.getPasswordCheck())) {
//            return ResponseDto.fail("PASSWORDS_NOT_MATCHED",
//                    "??????????????? ???????????? ????????? ???????????? ????????????.");
//        }
//
//        Member member = Member.builder()
//                .account(requestDto.getAccount())
//                .password(passwordEncoder.encode(requestDto.getPassword())) //????????? ??????
//                .nickname(requestDto.getNickname())
//                .imageUrl(requestDto.getImageUrl())
//                .build();
//        memberRepository.save(member);
//        return ResponseDto.success("???????????? ??????");
//    }

    // ???????????? (?????? ?????? ?????? ??????)
    @Transactional
    public ResponseDto<?> signUp(@ModelAttribute MemberRequestDto requestDto) throws IOException { //????????????

        if (null != isPresentAccount(requestDto.getAccount())) {
            return ResponseDto.fail("DUPLICATED_ACCOUNT",
                    "????????? ????????? ?????????.");
        }
        if (!requestDto.getPassword().equals(requestDto.getPasswordCheck())) {
            return ResponseDto.fail("PASSWORDS_NOT_MATCHED",
                    "??????????????? ???????????? ????????? ???????????? ????????????.");
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
                .password(passwordEncoder.encode(requestDto.getPassword())) //????????? ??????
                .nickname(requestDto.getNickname())
                .imageUrl(imgUrl)
                .build();
        memberRepository.save(member);
        return ResponseDto.success("???????????? ??????");
    }

    @Transactional
    public ResponseDto<?> login(LoginRequestDto requestDto, HttpServletResponse response) {
        Member member = isPresentAccount(requestDto.getAccount());
        if (null == member) {
            return ResponseDto.fail("MEMBER_NOT_FOUND",
                    "???????????? ?????? ??? ????????????.");
        }

        if(!passwordEncoder.matches(requestDto.getPassword(), member.getPassword())){
            return ResponseDto.fail("PASSWORD","??????????????? ?????? ????????????");
        }

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(requestDto.getAccount(), requestDto.getPassword());
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        TokenDto tokenDto = tokenProvider.generateTokenDto(authentication);
        tokenToHeaders(tokenDto, response);

        Optional<Member> member1 = memberRepository.findByAccount(requestDto.getAccount());

        String imageUrl = member1.get().getImageUrl();

        //?????? ??????
//        return ResponseDto.success(
//                MemberResponseDto.builder()
//                        .account(member.getAccount())
//                        .nickname(member.getNickname())
//                        .build()
//        );
        //????????? ????????? ????????? ??????

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
    public ResponseDto<?> idCheck(String account) { //????????? ????????????
        Optional<Member> optionalMember = memberRepository.findByAccount(account);
        if (optionalMember.isPresent()) {
            return ResponseDto.fail("DUPLICATED_ACCOUNT", "????????? ????????? ?????????.");
        }
        return ResponseDto.success("?????? ????????? ??????????????????.");
    }

    @Transactional
    public ResponseDto<?> nicknameCheck(String nickname) { //????????? ????????????
        Optional<Member> optionalMember = memberRepository.findByNickname(nickname);
        if (optionalMember.isPresent()) {
            return ResponseDto.fail("DUPLICATED_NICKNAME", "????????? ????????? ?????????.");
        }
        return ResponseDto.success("?????? ????????? ??????????????????.");
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
