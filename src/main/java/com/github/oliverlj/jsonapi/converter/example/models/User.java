package com.github.oliverlj.jsonapi.converter.example.models;

import java.io.Serializable;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;

import lombok.Data;

@Data
@Type("user")
public class User implements Serializable {

	private static final long serialVersionUID = -3218586871869104743L;

	@Id
	private Long id;

	private String email;

	private String userName;

}
