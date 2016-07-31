package com.lkl.zipkin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 *
 * Created by liaokailin on 16/7/27.
 */
@SpringBootApplication
public class Application {


    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Application.class);
        app.run(args);


    }
}
