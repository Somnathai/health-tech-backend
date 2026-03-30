package com.healthtech.symptom_analyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SymptomAnalyzerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SymptomAnalyzerApplication.class, args);
	}

}
