package org.demo.server.infra.common.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.demo.server.infra.common.exception.NotFoundException;
import org.demo.server.infra.common.exception.response.BindErrorResponse;
import org.demo.server.infra.common.exception.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class CommonExceptionHandler {

    /**
     * 폼 등록 시, 유효하지 않은 입력 값 예외
     *
     * @param e BindException
     * @return 에러 내용 응답
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<List<BindErrorResponse>> handleBindException(BindException e) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;

        // 모든 에러 메시지
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        List<BindErrorResponse> errorResponses = fieldErrors.stream()
                .map(err -> new BindErrorResponse(httpStatus.value(), err.getField(), err.getDefaultMessage()))
                .collect(Collectors.toList());

        return ResponseEntity.status(httpStatus).contentType(MediaType.APPLICATION_JSON).body(errorResponses);
    }

    /**
     * 경로 파라미터에 유효하지 않은 입력 값, 파일 미첨부 등 예외
     *
     * @param e IllegalArgumentException
     * @return 에러 내용 응답
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        ErrorResponse errorResponse = new ErrorResponse(httpStatus.value(), e.getMessage());
        return ResponseEntity.status(httpStatus).contentType(MediaType.APPLICATION_JSON).body(errorResponse);
    }

    /**
     * 존재하지 않는 객체 예외
     *
     * @param e NotFoundException
     * @return 예외 내용 응답
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> NotFoundException(NotFoundException e) {
        HttpStatus httpStatus = HttpStatus.NOT_FOUND;
        ErrorResponse errorResponse = new ErrorResponse(httpStatus.value(), e.getMessage());
        return ResponseEntity.status(httpStatus).contentType(MediaType.APPLICATION_JSON).body(errorResponse);
    }
}
