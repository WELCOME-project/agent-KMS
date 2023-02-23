package com.welcome.kms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KmsApplication {

	public static void main(String[] args) {
		System.setProperty("server.servlet.context-path", "/kms/api");
		SpringApplication.run(KmsApplication.class, args);
	}
}
