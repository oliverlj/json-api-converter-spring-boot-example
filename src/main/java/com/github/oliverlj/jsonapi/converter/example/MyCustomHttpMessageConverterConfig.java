package com.github.oliverlj.jsonapi.converter.example;

import java.util.Arrays;

import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MyCustomHttpMessageConverterConfig {

	@Bean(name = "myCustomHttpMessageConverter")
	public HttpMessageConverters myCustomHttpMessageConverter() {
		return new HttpMessageConverters(new JsonApiHttpMessageConverter(new ObjectMapper(),
				Arrays.asList("com.github.oliverlj.jsonapi.converter.example.models")));
	}
}
