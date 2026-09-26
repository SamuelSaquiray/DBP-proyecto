package com.recaudia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class RecaudIaApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecaudIaApplication.class, args);
    }
}
