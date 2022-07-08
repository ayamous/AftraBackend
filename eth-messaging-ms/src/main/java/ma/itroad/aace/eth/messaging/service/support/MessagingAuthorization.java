package ma.itroad.aace.eth.messaging.service.support;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.itroad.aace.eth.core.common.api.messaging.domain.Ack;
import ma.itroad.aace.eth.core.common.api.messaging.enums.MessageStatus;
import ma.itroad.aace.eth.core.common.api.messaging.enums.MessageType;
import ma.itroad.aace.eth.messaging.configuration.SmsConfig;
import ma.itroad.aace.eth.messaging.inbound.AppMessageService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
@Slf4j
@AllArgsConstructor
public class MessagingAuthorization {

    private final AppMessageService appMessageService;
    private final SmsConfig smsConfig;

    public Ack<Boolean> canDeliverTo(String recipient, MessageType type) {
        Ack<Boolean> ack = new Ack<>();
        switch (type) {
            case EMAIL:
            case PUSH_NOTIFICATION:
                return ack.data(true).code(HttpStatus.OK.value());
            case SMS:
                long max = smsConfig.getMessage().getMax();
                long time = smsConfig.getMessage().getTime();
                ChronoUnit chronoUnit = smsConfig.getMessage().getChronoUnit();
                LocalDateTime date = LocalDateTime.now().minus(time, chronoUnit);
                long recipientCount = appMessageService.countAllByRecipientAndStatusAndCreatedAtAfter(recipient, MessageStatus.DELIVERED, date);
                if (recipientCount < max) {
                    return ack.data(true).code(HttpStatus.OK.value());
                } else {
                    String message = String.format("Failed to authorize message delivery. Caused by %s cannot receive more than %s message(s) every %s %s",
                            recipient, max, time, chronoUnit.name().toLowerCase());
                    log.info(message);
                    ack.data(false).message(message).code(HttpStatus.INTERNAL_SERVER_ERROR.value());
                    return ack;
                }
            default:
                String message = String.format("Failed to authorize message delivery. Message type not handled %s", type);
                log.error(message);
                return ack.data(false).message(message).code(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }
}
