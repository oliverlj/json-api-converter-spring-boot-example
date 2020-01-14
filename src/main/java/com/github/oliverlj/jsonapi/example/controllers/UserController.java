package com.github.oliverlj.jsonapi.example.controllers;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.oliverlj.jsonapi.example.models.User;
import com.gitlab.oliverlj.jsonapi.configuration.parameters.FilterOperator;
import com.gitlab.oliverlj.jsonapi.configuration.parameters.FilterParameters;
import com.gitlab.oliverlj.jsonapi.exceptions.UnprocessableEntityException;

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
        if (users.values().stream().map(User::getUsername).anyMatch(username -> username.equals(user.getUsername()))) {
            throw new UnprocessableEntityException("name", "Username " + user.getUsername() + " already exists!");
        }
        users.put(user.getId(), user);
        return user;
    }

    @GetMapping
    public Collection<User> filter(FilterParameters filterParameters) {
        Set<String> usernames = filterParameters.getFilters().getOrDefault("username", Collections.emptyMap()).getOrDefault(FilterOperator.EQ,
                Collections.emptySet());
        if (!usernames.isEmpty()) {
            return users.values().stream().filter(user -> usernames.contains(user.getUsername())).collect(Collectors.toList());
        } else {
            return users.values();
        }
    }

    @GetMapping("{id}")
    public User findAll(@PathVariable("id") long id) {
        return users.get(id);
    }

}
