package com.moviecat.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.moviecat.exception.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String VALIDATION_FAILED = "Validation failed";
    private static final String UNEXPECTED_ERROR = "An unexpected error occurred";

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException exception) {
        HttpStatus status = Objects.requireNonNull(exception.getStatus(), "Exception status must not be null");
        return ResponseEntity.status(status)
                .body(buildErrorResponse(status, exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        logBadRequest(request, VALIDATION_FAILED, errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildErrorResponse(HttpStatus.BAD_REQUEST, VALIDATION_FAILED, errors));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request) {
        String message = "Invalid value '%s' for parameter '%s'"
                .formatted(exception.getValue(), exception.getName());
        logBadRequest(request, message, null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildErrorResponse(HttpStatus.BAD_REQUEST, message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {
        Throwable cause = exception.getMostSpecificCause();
        String message = "Malformed JSON request";
        if (cause instanceof InvalidFormatException invalidFormatException
                && invalidFormatException.getPath() != null
                && !invalidFormatException.getPath().isEmpty()) {
            String fieldName = invalidFormatException.getPath().get(0).getFieldName();
            message = "Invalid value for field '%s'".formatted(fieldName);
        }
        logBadRequest(request, message, null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildErrorResponse(HttpStatus.BAD_REQUEST, message));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleHandlerMethodValidationException(
            HandlerMethodValidationException exception,
            HttpServletRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getParameterValidationResults().forEach(result -> {
            String parameterName = result.getMethodParameter().getParameterName();
            String errorField = parameterName != null ? parameterName : "parameter";
            if (result.getContainerIndex() != null) {
                errorField = "%s[%d]".formatted(errorField, result.getContainerIndex());
            }
            String fieldKey = errorField;
            result.getResolvableErrors().forEach(error ->
                    errors.putIfAbsent(fieldKey, resolveMessage(error.getDefaultMessage(), VALIDATION_FAILED)));
        });

        if (errors.isEmpty()) {
            logBadRequest(request, VALIDATION_FAILED, null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(buildErrorResponse(HttpStatus.BAD_REQUEST, VALIDATION_FAILED));
        }
        logBadRequest(request, VALIDATION_FAILED, errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildErrorResponse(HttpStatus.BAD_REQUEST, VALIDATION_FAILED, errors));
    }

    @ExceptionHandler({IllegalArgumentException.class, ConstraintViolationException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException exception, HttpServletRequest request) {
        if (exception instanceof ConstraintViolationException constraintViolationException) {
            Map<String, String> errors = new LinkedHashMap<>();
            constraintViolationException.getConstraintViolations().forEach(violation ->
                    errors.put(violation.getPropertyPath().toString(), violation.getMessage()));
            logBadRequest(request, VALIDATION_FAILED, errors);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(buildErrorResponse(HttpStatus.BAD_REQUEST, VALIDATION_FAILED, errors));
        }

        String message = exception.getMessage() != null ? exception.getMessage() : "Bad request";
        logBadRequest(request, message, null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildErrorResponse(HttpStatus.BAD_REQUEST, message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception exception) {
        log.error("Unhandled exception", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, UNEXPECTED_ERROR));
    }

    private ErrorResponse buildErrorResponse(HttpStatus status, String message) {
        return new ErrorResponse(status.value(), message, LocalDateTime.now());
    }

    private ErrorResponse buildErrorResponse(
            HttpStatus status,
            String message,
            Map<String, String> errors) {
        return new ErrorResponse(status.value(), message, LocalDateTime.now(), errors);
    }

    private String resolveMessage(String message, String fallback) {
        if (message == null || message.isBlank()) {
            return fallback;
        }
        return message;
    }

    private void logBadRequest(HttpServletRequest request, String message, Map<String, String> errors) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        if (errors == null || errors.isEmpty()) {
            log.warn("Bad request 400 on {} {}: {}", method, uri, message);
            return;
        }
        log.warn("Bad request 400 on {} {}: {}, errors={}", method, uri, message, errors);
    }
}
