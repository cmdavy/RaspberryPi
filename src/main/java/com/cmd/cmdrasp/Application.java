package com.cmd.cmdrasp;

import com.cmd.cmdrasp.configuration.SwaggerConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.concurrent.Executor;

/**
 * Created by chrisdavy on 5/24/17.
 */
@SpringBootApplication
@EnableAsync
@EnableSwagger2
public class Application extends AsyncConfigurerSupport{

    public static void main (String[] args)
    {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("RaspLookup-");
        executor.initialize();
        return executor;
    }
}
