package com.github.oliverlj.jsonapi.converter.example;

import java.util.Arrays;

import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class MyCustomHttpMessageConverterConfig {

	@Bean(name = "myCustomHttpMessageConverter")
	public HttpMessageConverters myCustomHttpMessageConverter() {
		return new HttpMessageConverters(new JsonApiHttpMessageConverter(new ObjectMapper(),
				Arrays.asList("com.github.oliverlj.jsonapi.converter.example.models")));
	}
}
