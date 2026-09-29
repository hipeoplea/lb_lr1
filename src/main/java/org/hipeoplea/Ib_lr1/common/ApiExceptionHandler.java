package org.hipeoplea.Ib_lr1.common;

import org.hipeoplea.Ib_lr1.auth.UsernameAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    ResponseEntity<ErrorResponse> usernameAlreadyExists() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("Пользователь с таким логином уже существует"));
    }

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ErrorResponse> statusException(ResponseStatusException exception) {
        return ResponseEntity.status(exception.getStatusCode())
                .body(new ErrorResponse(exception.getReason() == null ? "Ошибка запроса" : exception.getReason()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> invalidRequest() {
        return ResponseEntity.badRequest().body(new ErrorResponse("Проверьте формат и длину полей запроса"));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ErrorResponse> unreadableRequest() {
        return ResponseEntity.badRequest().body(new ErrorResponse("Некорректный JSON"));
    }

    public record ErrorResponse(String error) {
    }
}
