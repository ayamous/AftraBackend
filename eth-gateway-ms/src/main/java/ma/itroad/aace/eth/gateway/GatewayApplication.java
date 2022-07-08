package ma.itroad.aace.eth.gateway;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import springfox.documentation.swagger.web.*;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableZuulProxy
@EnableFeignClients("ma.itroad.aace.eth.gateway")
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class }, scanBasePackages = {
        "ma.itroad.aace.eth.gateway", "org.springframework.http.codec"
})
@EnableDiscoveryClient
@EnableSwagger2
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

}
