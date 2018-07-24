package com.github.oliverlj.jsonapi.converter.example;

import java.util.Arrays;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication
public class Application {

	@Bean
	public HttpMessageConverters converters() {
		return new HttpMessageConverters(false, Arrays.asList(new JsonApiHttpMessageConverter(new ObjectMapper(),
				Arrays.asList("com.github.oliverlj.jsonapi.converter.example.models"))));
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
