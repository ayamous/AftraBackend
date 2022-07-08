package ma.itroad.aace.eth.messaging;

import ma.itroad.aace.eth.messaging.configuration.MailConfig;
import ma.itroad.aace.eth.messaging.configuration.SmsConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({SmsConfig.class, MailConfig.class})
public class MessagingBeanFactory {
}
