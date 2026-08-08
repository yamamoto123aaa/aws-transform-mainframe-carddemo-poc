package com.carddemo.interestcalc;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableBatchProcessing
public class InterestCalcApplication {

    public static void main(String[] args) {
        SpringApplication.run(InterestCalcApplication.class, args);
    }
}
