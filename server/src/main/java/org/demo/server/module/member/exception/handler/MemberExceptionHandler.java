package org.demo.server.module.member.exception.handler;

import org.demo.server.infra.common.exception.response.ErrorResponse;
import org.demo.server.module.member.exception.DuplicatedEmailException;
import org.demo.server.module.member.exception.InvalidVerificationCodeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

@RestControllerAdvice
public class MemberExceptionHandler {

    /**
     * 이메일 중복 예외
     *
     * @param e DuplicatedEmailException
     * @return 예외 내용 응답
     */
    @ExceptionHandler(DuplicatedEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmail(DuplicatedEmailException e) {
        HttpStatus httpStatus = HttpStatus.CONFLICT;
        ErrorResponse errorResponse = new ErrorResponse(httpStatus.value(), e.getMessage());
        return ResponseEntity.status(httpStatus).body(errorResponse);
    }

    /**
     * 유효하지 않은 인증 메일
     *
     * @param e InvalidVerificationCodeException
     * @return 예외 내용 응답
     */
    @ExceptionHandler(InvalidVerificationCodeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidVerificationCode(InvalidVerificationCodeException e) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        ErrorResponse errorResponse = new ErrorResponse(httpStatus.value(), e.getMessage());
        return ResponseEntity.status(httpStatus).body(errorResponse);
    }
}
