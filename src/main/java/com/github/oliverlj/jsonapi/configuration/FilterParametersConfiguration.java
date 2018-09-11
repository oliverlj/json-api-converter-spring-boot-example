package com.github.oliverlj.jsonapi.configuration;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.github.oliverlj.jsonapi.configuration.parameters.RequestParamFilterParametersMethodArgumentResolver;

@Configuration
public class FilterParametersConfiguration implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new RequestParamFilterParametersMethodArgumentResolver());
    }

}
