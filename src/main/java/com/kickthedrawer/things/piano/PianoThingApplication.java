package com.kickthedrawer.things.piano;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.kickthedrawer.things.piano"})
public class PianoThingApplication {

    private static final Logger logger = LoggerFactory.getLogger(PianoThingApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(PianoThingApplication.class, args);
    }

}
