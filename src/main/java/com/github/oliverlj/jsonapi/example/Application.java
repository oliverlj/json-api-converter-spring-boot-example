package com.github.oliverlj.jsonapi.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.github.oliverlj.jsonapi.configuration.EnableJsonApiTypes;

@EnableJsonApiTypes
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
