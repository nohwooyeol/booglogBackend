package hanghae8mini.booglogbackend.exception;

import hanghae8mini.booglogbackend.controller.response.ResponseDto;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NumberFormatException.class)
    public ResponseDto<?> handleNumberFormatException(){
        return ResponseDto.fail("NumberFormatException","잘못된 요청입니다. ");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseDto<?> handleHttpRequestMethodNotSupportedException(final HttpRequestMethodNotSupportedException e) {
        return ResponseDto.fail("HttpRequestMethodNotSupportedException" , "등록되지 않는 URI요청입니다.");
    }

    @ExceptionHandler(LoginFailException.class)
    public ResponseDto<?> handleLoginFailException() {
        return ResponseDto.fail("LoginFailException" , "로그인이 필요합니다");
    }

}
