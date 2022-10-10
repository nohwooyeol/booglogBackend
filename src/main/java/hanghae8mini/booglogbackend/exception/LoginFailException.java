package hanghae8mini.booglogbackend.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class LoginFailException extends RuntimeException{

    LoginFailException(String message){
        super(message);
    }
}
