package ma.itroad.aace.eth.messaging;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync(proxyTargetClass = true)
@SpringBootApplication(scanBasePackages = "ma.itroad.aace.eth")
public class MessagingService {

    public static void main(String[] args) {
        SpringApplication.run(MessagingService.class, args);
    }

}
