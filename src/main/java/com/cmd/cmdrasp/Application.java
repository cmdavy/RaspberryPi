package com.cmd.cmdrasp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Created by chrisdavy on 5/24/17.
 */
@SpringBootApplication
@EnableAsync
public class Application {

    public static void main (String[] args)
    {
        SpringApplication.run(Application.class, args);
    }
}
