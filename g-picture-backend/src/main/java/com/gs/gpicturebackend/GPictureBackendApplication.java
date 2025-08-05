package com.gs.gpicturebackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan("com.gs.gpicturebackend.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
public class GPictureBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(GPictureBackendApplication.class, args);
    }

}
