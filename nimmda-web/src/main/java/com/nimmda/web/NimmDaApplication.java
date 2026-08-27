package com.nimmda.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.nimmda")
public class NimmDaApplication {

    public static void main(String[] args) {
        String port = System.getenv("PORT");
        if (port != null && !port.isBlank()) {
            System.setProperty("server.port", port.trim());
        }
        SpringApplication.run(NimmDaApplication.class, args);
    }
}
