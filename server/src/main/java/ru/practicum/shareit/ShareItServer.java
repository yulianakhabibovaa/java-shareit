package ru.practicum.shareit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan
@SpringBootApplication
public class ShareItServer {

    public static void main(String[] args) {
        SpringApplication.run(ShareItServer.class, args);
    }

}