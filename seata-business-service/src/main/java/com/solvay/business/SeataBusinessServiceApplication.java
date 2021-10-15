package com.solvay.business;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
@MapperScan(basePackages = "com.solvay.business.mapper")
public class SeataBusinessServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SeataBusinessServiceApplication.class, args);
    }

}
