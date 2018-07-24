package com.github.oliverlj.jsonapi.configuration;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.util.WebUtils;

import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.JSONAPISpecConstants;
import com.github.jasminb.jsonapi.models.errors.Error;
import com.github.jasminb.jsonapi.models.errors.Source;
import com.github.oliverlj.jsonapi.configuration.converters.ConstraintViolationConverter;
import com.github.oliverlj.jsonapi.configuration.converters.ErrorConverter;
import com.github.oliverlj.jsonapi.exceptions.UnprocessableEntityException;

/**
 * A response entity exception handler for request which accepts {@code application/vnd.api+json} that wish to provide centralized exception handling across all
 * {@code @RequestMapping} methods.
 * 
 * @author Olivier LE JACQUES (o.le.jacques@gmail.com)
 *
 */
@ControllerAdvice
@ComponentScan(basePackageClasses = ErrorConverter.class)
public class JsonApiResponseEntityExceptionHandler {

    @Autowired
    private ConstraintViolationConverter constraintViolationConverter;

    @ExceptionHandler(value = { UnprocessableEntityException.class })
    protected ResponseEntity<Object> handleUnprocessableEntity(UnprocessableEntityException ex, WebRequest request) {
        if (canAcceptJsonApi(request)) {
            HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
            JSONAPIDocument<?> body = JSONAPIDocument.createErrorDocument(Arrays.asList(newError(ex)));
            return handleExceptionInternal(ex, body, new HttpHeaders(), status, request);
        } else {
            throw ex;
        }
    }

    private Error newError(UnprocessableEntityException ex) {
        Error error = new Error();
        error.setStatus(String.valueOf(HttpStatus.UNPROCESSABLE_ENTITY.value()));
        error.setTitle(ex.getMessage());
        ex.getField().ifPresent((field) -> {
            error.setSource(new Source());
            error.getSource().setPointer(String.join("/", JSONAPISpecConstants.DATA, JSONAPISpecConstants.ATTRIBUTES, field));
        });
        return error;
    }

    private Error newError(FieldError fieldError) {
        Error error = new Error();
        error.setStatus(String.valueOf(HttpStatus.UNPROCESSABLE_ENTITY.value()));
        error.setTitle(fieldError.getDefaultMessage());
        error.setSource(new Source());
        error.getSource().setPointer(String.join("/", JSONAPISpecConstants.DATA, JSONAPISpecConstants.ATTRIBUTES, fieldError.getField()));
        return error;
    }

    @ExceptionHandler(value = { MethodArgumentNotValidException.class })
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, WebRequest request)
            throws MethodArgumentNotValidException {
        if (canAcceptJsonApi(request)) {
            HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
            BindingResult bindingResult = ex.getBindingResult();
            List<Error> errors = bindingResult.getAllErrors().stream().filter(error -> error instanceof FieldError).map(FieldError.class::cast)
                    .map(this::newError).collect(Collectors.toList());
            JSONAPIDocument<?> body = JSONAPIDocument.createErrorDocument(errors);
            return handleExceptionInternal(ex, body, new HttpHeaders(), status, request);
        } else {
            throw ex;
        }
    }

    @ExceptionHandler(value = { ConstraintViolationException.class })
    protected ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) throws MethodArgumentNotValidException {
        if (canAcceptJsonApi(request)) {
            HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
            List<Error> errors = ex.getConstraintViolations().stream().map(constraintViolationConverter::convert).collect(Collectors.toList());
            JSONAPIDocument<?> body = JSONAPIDocument.createErrorDocument(errors);
            return handleExceptionInternal(ex, body, new HttpHeaders(), status, request);
        } else {
            throw ex;
        }
    }

    private boolean canAcceptJsonApi(WebRequest request) {
        return Arrays.asList(request.getHeaderValues(HttpHeaders.ACCEPT)).stream().anyMatch(JsonApiHttpMessageConverter.APPLICATION_JSON_API_VALUE::equals);
    }

    /**
     * A single place to customize the response body of all Exception types.
     * <p>
     * The default implementation sets the {@link WebUtils#ERROR_EXCEPTION_ATTRIBUTE} request attribute and creates a {@link ResponseEntity} from the given
     * body, headers, and status.
     * 
     * @param ex      the exception
     * @param body    the body for the response
     * @param headers the headers for the response
     * @param status  the response status
     * @param request the current request
     */
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        if (HttpStatus.INTERNAL_SERVER_ERROR.equals(status)) {
            request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, ex, WebRequest.SCOPE_REQUEST);
        }
        return new ResponseEntity<>(body, headers, status);
    }

}