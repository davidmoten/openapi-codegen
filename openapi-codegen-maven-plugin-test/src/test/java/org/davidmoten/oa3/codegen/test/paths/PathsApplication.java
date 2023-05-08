package org.davidmoten.oa3.codegen.test.paths;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties
@SpringBootApplication
public class PathsApplication {

    public static void main(String[] args) {
        SpringApplication.run(PathsApplication.class, args);
    }

}