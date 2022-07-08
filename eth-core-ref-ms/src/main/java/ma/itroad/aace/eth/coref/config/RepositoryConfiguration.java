package ma.itroad.aace.eth.coref.config;

import ma.itroad.aace.eth.coref.handler.CountryRefEventHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RepositoryConfiguration {

    @Bean
    CountryRefEventHandler countryRefEventHandler(){
        return new CountryRefEventHandler();
    }
}
