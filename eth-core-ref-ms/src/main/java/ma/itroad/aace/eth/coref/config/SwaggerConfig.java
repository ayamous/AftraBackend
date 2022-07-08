package ma.itroad.aace.eth.coref.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.UriComponentsBuilder;
import springfox.documentation.PathProvider;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.paths.DefaultPathProvider;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

import java.util.Collections;

import static springfox.documentation.spring.web.paths.Paths.removeAdjacentForwardSlashes;


@Configuration
@EnableSwagger2
public class SwaggerConfig {

    private final String BASE_PATH = "/api/coref";
    @Bean
    public Docket restApi(){
        return new Docket(DocumentationType.SWAGGER_2)
                .pathProvider(new PathProvider() {
                    @Override
                    public String getOperationPath(String operationPath) {
                        return operationPath.replace(BASE_PATH, "");
                    }
                    @Override
                    public String getResourceListingPath(String groupName, String apiDeclaration) {
                        return BASE_PATH;
                    }
                })
                .groupName("REST API (V1)")
                .select()
                .apis(RequestHandlerSelectors.basePackage("ma.itroad.aace.eth.coref.controller"))
                .paths(PathSelectors.ant("/api/v1/**"))
                .build()
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo(){
        return new ApiInfo(
                "Springfox API Specification",
                "Spring REST APIs",
                "V01",
                "www.itadventure.org",
                new Contact("IT","www.itadventure.org", "itadventure@gmail.com"),
                "Lincense of API",
                "www.itadventure.org",
                Collections.emptyList()
        );
    }


}

