package ma.itroad.aace.eth.coref;

import ma.itroad.aace.eth.coref.model.entity.CountryRef;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.xmlpull.v1.XmlPullParserException;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import org.springframework.scheduling.annotation.EnableAsync;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableJpaRepositories({"ma.itroad.aace.eth.coref.repository"})
@EntityScan(basePackages = {"ma.itroad.aace.eth.coref.model.entity"})
@SpringBootApplication(scanBasePackages = {"ma.itroad.aace.eth.coref", "ma.itroad.aace.eth.core"})
@EnableDiscoveryClient
@EnableAsync
@Import(RepositoryRestMvcConfiguration.class)
@EnableSwagger2
public class CoreRefApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoreRefApplication.class, args);
    }



}
