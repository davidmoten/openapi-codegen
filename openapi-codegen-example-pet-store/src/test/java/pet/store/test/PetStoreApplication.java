package pet.store.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import pet.store.Globals;

@EnableConfigurationProperties
@SpringBootApplication(scanBasePackageClasses = Globals.class)
public class PetStoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetStoreApplication.class, args);
    }

}