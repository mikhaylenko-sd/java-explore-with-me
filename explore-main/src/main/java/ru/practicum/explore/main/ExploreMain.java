package ru.practicum.explore.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "ru.practicum.explore")
public class ExploreMain {
    public static void main(String[] args) {
        SpringApplication.run(ExploreMain.class, args);
    }
}
