package com.perfecthousehold.transport.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Centralized REST exception handler returning RFC 9457 {@link ProblemDetail} payloads.
 * Extension properties (such as {@code timestamp} and {@code errors}) rely on
 * {@code ProblemDetailJacksonMixin} configured in {@link com.perfecthousehold.config.JacksonConfig}
 * to serialize at the top level of the JSON response.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String ERROR_BASE_URI = "https://perfecthousehold.local/errors/";
    private static final String ABOUT_BLANK = "about:blank";

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        err -> err.getDefaultMessage() != null ? err.getDefaultMessage() : "Invalid value",
                        (first, second) -> first
                ));

        ProblemDetail problem = createProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                "One or more fields failed validation.",
                "validation-error",
                extractRequestUri(request)
        );
        problem.setProperty("errors", fieldErrors);

        return handleExceptionInternal(ex, problem, headers, HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        List<String> errors = ex.getAllErrors().stream()
                .map(err -> err.getDefaultMessage() != null ? err.getDefaultMessage() : "Invalid parameter")
                .toList();

        ProblemDetail problem = createProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Parameter Validation Failed",
                "Request parameters failed validation.",
                "parameter-validation-error",
                extractRequestUri(request)
        );
        problem.setProperty("errors", errors);

        return handleExceptionInternal(ex, problem, headers, HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex,
            @Nullable Object body,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request) {

        ResponseEntity<Object> response = super.handleExceptionInternal(ex, body, headers, statusCode, request);
        if (response != null && response.getBody() instanceof ProblemDetail problem) {
            enrichProblemDetail(problem, statusCode, extractRequestUri(request));
        }
        return response;
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        log.warn("Authentication failed on {}: {}", request.getRequestURI(), ex.getMessage());
        return createProblemDetail(
                HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                "Authentication is required or credentials are invalid.",
                "unauthorized",
                request.getRequestURI()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied on {}: {}", request.getRequestURI(), ex.getMessage());
        return createProblemDetail(
                HttpStatus.FORBIDDEN,
                "Forbidden",
                "You do not have permission to perform this action.",
                "forbidden",
                request.getRequestURI()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Bad request on {}: {}", request.getRequestURI(), ex.getMessage());
        return createProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                ex.getMessage(),
                "bad-request",
                request.getRequestURI()
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Data integrity violation on {}: {}", request.getRequestURI(), ex.getMostSpecificCause().getMessage());
        return createProblemDetail(
                HttpStatus.CONFLICT,
                "Conflict",
                "The request conflicts with existing data.",
                "conflict",
                request.getRequestURI()
        );
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnhandledException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception occurred on {}", request.getRequestURI(), ex);
        return createProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occurred. Please try again later.",
                "internal-error",
                request.getRequestURI()
        );
    }

    private ProblemDetail createProblemDetail(
            HttpStatus status,
            String title,
            String detail,
            String typeSlug,
            @Nullable String requestUri) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setType(URI.create(ERROR_BASE_URI + typeSlug));
        if (requestUri != null && !requestUri.isBlank()) {
            problem.setInstance(URI.create(requestUri));
        }
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    private void enrichProblemDetail(ProblemDetail problem, HttpStatusCode statusCode, @Nullable String requestUri) {
        if (problem.getType() == null || ABOUT_BLANK.equals(problem.getType().toString())) {
            problem.setType(URI.create(ERROR_BASE_URI + defaultTypeSlug(statusCode)));
        }
        if (problem.getInstance() == null && requestUri != null && !requestUri.isBlank()) {
            problem.setInstance(URI.create(requestUri));
        }
        if (problem.getProperties() == null || !problem.getProperties().containsKey("timestamp")) {
            problem.setProperty("timestamp", Instant.now());
        }
    }

    @Nullable
    private String extractRequestUri(WebRequest request) {
        if (request instanceof ServletWebRequest servletWebRequest) {
            return servletWebRequest.getRequest().getRequestURI();
        }
        return null;
    }

    private String defaultTypeSlug(HttpStatusCode statusCode) {
        return switch (statusCode.value()) {
            case 400 -> "bad-request";
            case 401 -> "unauthorized";
            case 403 -> "forbidden";
            case 404 -> "not-found";
            case 405 -> "method-not-allowed";
            case 409 -> "conflict";
            case 415 -> "unsupported-media-type";
            default -> "http-" + statusCode.value();
        };
    }
}
