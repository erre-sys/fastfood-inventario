package ec.com.erre.inventory.infrastructure.in.rest;

import ec.com.erre.inventory.application.InvalidFieldException;
import ec.com.erre.inventory.domain.CategoryAlreadyExistsException;
import ec.com.erre.inventory.domain.CategoryNotFoundException;
import ec.com.erre.inventory.domain.InvalidConversionException;
import ec.com.erre.inventory.domain.ItemAlreadyExistsException;
import ec.com.erre.inventory.domain.ItemNotFoundException;
import ec.com.erre.inventory.domain.UnitAlreadyExistsException;
import ec.com.erre.inventory.domain.UnitNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.InvalidNullException;
import tools.jackson.databind.exc.UnrecognizedPropertyException;

/**
 * Every error of the API as Problem Details (RFC 9457): a stable {@code code}, a Spanish {@code
 * title} and {@code detail}, and {@code errors} per field for invalid input. The messages are in
 * {@code messages.properties}; internal exception messages never reach the client.
 */
@RestControllerAdvice
class ProblemHandler extends ResponseEntityExceptionHandler {

    private static final Locale SPANISH = Locale.of("es");

    private final MessageSource messages;

    ProblemHandler(MessageSource messages) {
        this.messages = messages;
    }

    @ExceptionHandler(UnitNotFoundException.class)
    ResponseEntity<Object> unitNotFound(UnitNotFoundException ex, WebRequest request) {
        return problem(HttpStatus.NOT_FOUND, ErrorCode.UNIT_NOT_FOUND, request, ex.code());
    }

    @ExceptionHandler(UnitAlreadyExistsException.class)
    ResponseEntity<Object> unitAlreadyExists(UnitAlreadyExistsException ex, WebRequest request) {
        return problem(HttpStatus.CONFLICT, ErrorCode.UNIT_ALREADY_EXISTS, request, ex.code());
    }

    @ExceptionHandler(InvalidConversionException.class)
    ResponseEntity<Object> invalidConversion(InvalidConversionException ex, WebRequest request) {
        return problem(
                HttpStatus.UNPROCESSABLE_CONTENT,
                ErrorCode.INVALID_CONVERSION,
                request,
                ex.source().code(),
                ex.source().dimension(),
                ex.target().code(),
                ex.target().dimension());
    }

    @ExceptionHandler(ItemNotFoundException.class)
    ResponseEntity<Object> itemNotFound(ItemNotFoundException ex, WebRequest request) {
        return problem(HttpStatus.NOT_FOUND, ErrorCode.ITEM_NOT_FOUND, request);
    }

    @ExceptionHandler(ItemAlreadyExistsException.class)
    ResponseEntity<Object> itemAlreadyExists(ItemAlreadyExistsException ex, WebRequest request) {
        return problem(HttpStatus.CONFLICT, ErrorCode.ITEM_ALREADY_EXISTS, request, ex.name());
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    ResponseEntity<Object> categoryNotFound(CategoryNotFoundException ex, WebRequest request) {
        return problem(HttpStatus.NOT_FOUND, ErrorCode.CATEGORY_NOT_FOUND, request);
    }

    @ExceptionHandler(CategoryAlreadyExistsException.class)
    ResponseEntity<Object> categoryAlreadyExists(
            CategoryAlreadyExistsException ex, WebRequest request) {
        return problem(HttpStatus.CONFLICT, ErrorCode.CATEGORY_ALREADY_EXISTS, request, ex.name());
    }

    /** A well-formed field that cannot be used (inactive category, unknown unit...). */
    @ExceptionHandler(InvalidFieldException.class)
    ResponseEntity<Object> invalidField(InvalidFieldException ex, WebRequest request) {
        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        describe(problem, ErrorCode.VALIDATION_FAILED, request);
        problem.setProperty(
                "errors",
                List.of(
                        new FieldMessage(
                                ex.field(), text("field." + ex.reason() + "." + ex.field()))));
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(EmptyPatchException.class)
    ResponseEntity<Object> emptyPatch(EmptyPatchException ex, WebRequest request) {
        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        describe(problem, ErrorCode.VALIDATION_FAILED, request);
        problem.setDetail(text("problem.EMPTY_PATCH.detail"));
        return ResponseEntity.badRequest().body(problem);
    }

    /** The token is valid but lacks the permission of the operation ({@code @PreAuthorize}). */
    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<Object> accessDenied(AccessDeniedException ex, WebRequest request) {
        return problem(HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN, request);
    }

    /** Spring MVC errors (invalid parameters, unreadable body, media type): same shape. */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex,
            Object body,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request) {
        var response = super.handleExceptionInternal(ex, body, headers, statusCode, request);
        if (response != null && response.getBody() instanceof ProblemDetail problem) {
            var code = codeFor(statusCode);
            if (code != null) {
                describe(problem, code, request);
            }
            var errors = fieldErrors(ex);
            if (!errors.isEmpty()) {
                problem.setProperty("errors", errors);
            }
        }
        return response;
    }

    private static ErrorCode codeFor(HttpStatusCode status) {
        if (status.value() == HttpStatus.BAD_REQUEST.value()) {
            return ErrorCode.VALIDATION_FAILED;
        }
        if (status.value() == HttpStatus.UNSUPPORTED_MEDIA_TYPE.value()) {
            return ErrorCode.UNSUPPORTED_MEDIA_TYPE;
        }
        return null;
    }

    private ResponseEntity<Object> problem(
            HttpStatus status, ErrorCode code, WebRequest request, Object... args) {
        var problem = ProblemDetail.forStatus(status);
        describe(problem, code, request, args);
        return ResponseEntity.status(status).body(problem);
    }

    private void describe(
            ProblemDetail problem, ErrorCode code, WebRequest request, Object... args) {
        problem.setTitle(text("problem." + code + ".title"));
        problem.setDetail(text("problem." + code + ".detail", args));
        problem.setProperty("code", code.name());
        if (request instanceof ServletWebRequest servlet) {
            HttpServletRequest http = servlet.getRequest();
            problem.setInstance(URI.create(http.getRequestURI()));
        }
    }

    private List<FieldMessage> fieldErrors(Exception ex) {
        var errors = new ArrayList<FieldMessage>();
        switch (ex) {
            case MethodArgumentNotValidException invalid ->
                    invalid.getBindingResult()
                            .getFieldErrors()
                            .forEach(error -> errors.add(fieldMessage(error)));
            case HandlerMethodValidationException invalid ->
                    invalid.getParameterValidationResults()
                            .forEach(result -> {
                                if (result instanceof ParameterErrors beanErrors) {
                                    beanErrors
                                            .getFieldErrors()
                                            .forEach(error -> errors.add(fieldMessage(error)));
                                } else {
                                    var name = parameterName(result.getMethodParameter());
                                    result.getResolvableErrors()
                                            .forEach(error ->
                                                    errors.add(
                                                            new FieldMessage(
                                                                    name, resolve(error))));
                                }
                            });
            case MethodArgumentTypeMismatchException mismatch ->
                    errors.add(new FieldMessage(mismatch.getName(), text("field.invalid")));
            case MissingServletRequestParameterException missing ->
                    errors.add(
                            new FieldMessage(missing.getParameterName(), text("field.required")));
            case HttpMessageNotReadableException unreadable -> {
                var error = bodyError(unreadable);
                if (error != null) {
                    errors.add(error);
                }
            }
            default -> {
                // Other errors carry no field information
            }
        }
        return errors.stream().distinct().toList();
    }

    /** The field of an unreadable body: unknown, explicitly null, or of the wrong type. */
    private FieldMessage bodyError(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();
        while (cause != null && !(cause instanceof JacksonException)) {
            cause = cause.getCause();
        }
        if (!(cause instanceof JacksonException jackson) || jackson.getPath().isEmpty()) {
            return null;
        }
        var path = jackson.getPath();
        var field = path.get(path.size() - 1).getPropertyName();
        if (field == null) {
            return null;
        }
        var key =
                switch (jackson) {
                    case UnrecognizedPropertyException _ -> "field.unknown";
                    case InvalidNullException _ -> "field.null";
                    default -> "field.invalid";
                };
        return new FieldMessage(field, text(key));
    }

    private FieldMessage fieldMessage(FieldError error) {
        return new FieldMessage(error.getField(), resolve(error));
    }

    private static String parameterName(MethodParameter parameter) {
        var annotation = parameter.getParameterAnnotation(RequestParam.class);
        if (annotation != null && !annotation.name().isEmpty()) {
            return annotation.name();
        }
        if (annotation != null && !annotation.value().isEmpty()) {
            return annotation.value();
        }
        return parameter.getParameterName();
    }

    private String resolve(MessageSourceResolvable error) {
        return messages.getMessage(error, SPANISH);
    }

    private String text(String key, Object... args) {
        return messages.getMessage(key, args, SPANISH);
    }
}
