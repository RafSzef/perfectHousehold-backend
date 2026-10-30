package com.perfecthousehold;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PerfectHouseholdApplication {

    public static void main(String[] args) {
        // Automatically load variables from .env file for local development
        DotenvLoader.load();
        SpringApplication.run(PerfectHouseholdApplication.class, args);
    }
}
