package com.github.oliverlj.jsonapi.configuration.converters;

import java.util.stream.StreamSupport;

import javax.validation.ConstraintViolation;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.github.jasminb.jsonapi.JSONAPISpecConstants;
import com.github.jasminb.jsonapi.models.errors.Error;
import com.github.jasminb.jsonapi.models.errors.Source;

@Component
public class ConstraintViolationConverter implements ErrorConverter<ConstraintViolation<?>> {

    @Override
    public Error convert(ConstraintViolation<?> source) {
        Error error = new Error();
        error.setStatus(String.valueOf(HttpStatus.UNPROCESSABLE_ENTITY.value()));
        error.setTitle(source.getMessage());
        StreamSupport.stream(source.getPropertyPath().spliterator(), false).reduce((first, second) -> second).ifPresent((lastNode) -> {
            error.setSource(new Source());
            error.getSource().setPointer(String.join("/", JSONAPISpecConstants.DATA, JSONAPISpecConstants.ATTRIBUTES, lastNode.getName()));
        });
        return error;
    }

}
