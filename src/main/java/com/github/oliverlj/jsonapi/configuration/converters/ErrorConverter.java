package com.github.oliverlj.jsonapi.configuration.converters;

import com.github.jasminb.jsonapi.models.errors.Error;

/**
 * Convert an {@link Object} in a {@link Error}.
 * 
 * @author Olivier LE JACQUES (o.le.jacques@gmail.com)
 *
 * @param <S> the source of error
 */
public interface ErrorConverter<S> {

    Error convert(S source);

}
