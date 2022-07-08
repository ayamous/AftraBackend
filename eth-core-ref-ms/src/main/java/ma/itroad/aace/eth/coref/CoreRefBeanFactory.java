package ma.itroad.aace.eth.coref;

import ma.itroad.aace.eth.core.common.EthClient;
import ma.itroad.aace.eth.core.common.api.messaging.MessagingAPI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class CoreRefBeanFactory {

    @Bean
    public EthClient client(@Value("${eth.api.url.messaging}") String messagingUrl, RestTemplate restTemplate) {
        return EthClient.builder().messaging(EthClient.build(MessagingAPI.class, messagingUrl, restTemplate)).build();
    }
}
