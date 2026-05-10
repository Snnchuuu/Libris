package com.libris;

import com.vaadin.flow.component.dependency.StyleSheet;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// bu sınıf frontend için lazım silmeyin aga
@SpringBootApplication
@StyleSheet("styles/styles.css")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
