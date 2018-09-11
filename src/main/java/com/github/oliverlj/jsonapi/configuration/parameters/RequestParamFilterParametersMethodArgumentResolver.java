package com.github.oliverlj.jsonapi.configuration.parameters;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * a {@link HandlerMethodArgumentResolver} for {@link FilterParameters}.
 * 
 * @author Olivier LE JACQUES (o.le.jacques@gmail.com)
 *
 */
public class RequestParamFilterParametersMethodArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String REGEX_FILTER_KEY = "filter\\[.*\\]\\[.*\\]";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return FilterParameters.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public @NonNull Object resolveArgument(MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
            @Nullable WebDataBinderFactory binderFactory) throws Exception {
        FilterParameters filterParameters = new FilterParameters();
        Map<String, String[]> parameterMap = webRequest.getParameterMap();
        Set<Entry<String, String[]>> validParameters = parameterMap.entrySet().stream().filter(this::isParameterValid).collect(Collectors.toSet());
        for (Entry<String, String[]> validParameter : validParameters) {
            String key = validParameter.getKey();
            String attribute = key.substring(key.indexOf('[') + 1, key.indexOf(']'));
            FilterOperator filterOperator = FilterOperator.valueOf(key.substring(key.lastIndexOf('[') + 1, key.lastIndexOf(']')));
            Set<String> values = Arrays.asList(validParameter.getValue()).stream().map(String::trim).filter(v -> !v.isEmpty()).collect(Collectors.toSet());
            filterParameters.getFilters().putIfAbsent(attribute, new HashMap<>());
            filterParameters.getFilters().get(attribute).putIfAbsent(filterOperator, new HashSet<>());
            filterParameters.getFilters().get(attribute).get(filterOperator).addAll(values);
        }
        return filterParameters;
    }

    private boolean isParameterValid(java.util.Map.Entry<String, String[]> entry) {
        return entry.getKey().matches(REGEX_FILTER_KEY) && Arrays.asList(entry.getValue()).stream().anyMatch(value -> !value.trim().isEmpty());
    }

}