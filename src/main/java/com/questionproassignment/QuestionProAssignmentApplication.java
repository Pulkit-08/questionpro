package com.questionproassignment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan
public class QuestionProAssignmentApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuestionProAssignmentApplication.class, args);
		System.out.println("Hello From Pulkit..");

	}

}
