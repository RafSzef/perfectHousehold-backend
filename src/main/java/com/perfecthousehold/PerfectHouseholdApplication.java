package com.perfecthousehold;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.perfecthousehold.security.dao")
public class PerfectHouseholdApplication {

    public static void main(String[] args) {
        // Automatically load variables from .env file for local development
        DotenvLoader.load();
        SpringApplication.run(PerfectHouseholdApplication.class, args);
    }
}
