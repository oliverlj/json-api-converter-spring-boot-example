package com.github.oliverlj.jsonapi.exceptions;

import java.util.Optional;

import org.springframework.lang.NonNull;

import lombok.Getter;

/**
 * A generic exception for an unprocessable entity.
 * 
 * @author Olivier LE JACQUES (o.le.jacques@gmail.com)
 *
 */
@Getter
public class UnprocessableEntityException extends RuntimeException {

    private static final long serialVersionUID = 4263597376937376571L;

    @NonNull
    private Optional<String> field;

    public UnprocessableEntityException(String message) {
        super(message);
        this.field = Optional.empty();
    }

    public UnprocessableEntityException(String field, String message) {
        super(message);
        this.field = Optional.of(field);
    }

}
