package ch.nexown.adresscrawler.Configuration;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Created by buma on 19.01.2017.
 */

@SpringBootConfiguration
public class ExampleConfiguration {


    @Bean("uncompleteExample")
    public String exampleString() {
        return "I am an uncompleted example!";
    }
}
