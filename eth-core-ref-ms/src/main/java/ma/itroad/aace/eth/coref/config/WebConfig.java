package ma.itroad.aace.eth.coref.config;

import ma.itroad.aace.eth.coref.service.converter.CountryTypeToEnumConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {

        registry.addConverter(new CountryTypeToEnumConverter());

    }
}
