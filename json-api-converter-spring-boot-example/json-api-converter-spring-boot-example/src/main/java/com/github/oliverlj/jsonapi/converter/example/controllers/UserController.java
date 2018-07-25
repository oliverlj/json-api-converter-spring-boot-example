package com.github.oliverlj.jsonapi.converter.example.controllers;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.oliverlj.jsonapi.converter.example.models.User;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("users")
@Slf4j
public class UserController {

	private static final AtomicLong ID_GENERATOR = new AtomicLong();

	private static Map<Long, User> users = new ConcurrentHashMap<>();

	@PostMapping
	public User create(@RequestBody @Valid User user) {
		log.info("Create user [{},{}]", user.getUsername(), user.getEmail());
		if (user.getId() == null) {
			user.setId(ID_GENERATOR.getAndIncrement());
		}
		users.put(user.getId(), user);
		return user;
	}

	@GetMapping
	public Collection<User> findAll() {
		return users.values();
	}

	@GetMapping("{id}")
	public JSONAPIDocument<User> findAll(@PathVariable("id") long id) {
		return new JSONAPIDocument<User>(users.get(id));
	}

}
