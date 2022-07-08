package ma.itroad.aace.eth.messaging.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.util.List;

@Getter
@AllArgsConstructor
@ConstructorBinding
@ConfigurationProperties("aace.eth.mail")
public class MailConfig {
    private final String sender;
    private final String subject;
    private final List<String> support;
}

