package hanghae8mini.booglogbackend.aop;

import hanghae8mini.booglogbackend.controller.response.ResponseDto;
import hanghae8mini.booglogbackend.domain.Member;
import hanghae8mini.booglogbackend.exception.LoginFailException;
import hanghae8mini.booglogbackend.utils.Jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Aspect
@RequiredArgsConstructor
@Component
public class LoginCheckAop {


    private final TokenProvider tokenProvider;

    @Before("@annotation(hanghae8mini.booglogbackend.annotation.LoginCheck)")
    public void loginCheck(){

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes()).getRequest();

        if (null == request.getHeader("RefreshToken") || null == request.getHeader("Authorization")) {
                throw new LoginFailException();
        }
    }

    @Transactional
    public Member validateMember(HttpServletRequest request) {
        if (!tokenProvider.validateToken(request.getHeader("RefreshToken"))) {
            return null;
        }
        return tokenProvider.getMemberFromAuthentication();
    }



}
