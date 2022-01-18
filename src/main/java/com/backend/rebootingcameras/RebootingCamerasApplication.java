package com.backend.rebootingcameras;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class RebootingCamerasApplication {

    public static void main(String[] args) {
        SpringApplication.run(RebootingCamerasApplication.class, args);

    }

}
