package com.beone.api;

import com.beone.api.support.TestcontainersConfiguration;
import org.springframework.boot.SpringApplication;

public class TestBeoneApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(BeoneApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
