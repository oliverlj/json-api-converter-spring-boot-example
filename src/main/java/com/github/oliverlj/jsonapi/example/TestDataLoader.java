package com.github.oliverlj.jsonapi.example;

import java.util.stream.IntStream;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.github.oliverlj.jsonapi.example.controllers.UserController;
import com.github.oliverlj.jsonapi.example.models.User;

@Configuration
public class TestDataLoader {

	@Autowired
	private UserController userController;

	@PostConstruct
	public void setup() {
		IntStream.rangeClosed(1, 2).mapToObj(i -> {
			var user = new User();
			user.setUsername("user" + i);
			user.setEmail("user" + i + "@domain.com");
			return user;
		}).forEach(userController::create);
	}
}
