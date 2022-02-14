package com.backend.rebootingcameras;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling
@SpringBootApplication
public class RebootingCamerasApplication extends SpringBootServletInitializer {


    public static final Logger log =
            LoggerFactory.getLogger(RebootingCamerasApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(RebootingCamerasApplication.class, args);
//        log.debug("Starting my application in debug with {} args", args.length);
//        log.info("Starting my application with {} args.", args.length);
    }

}
