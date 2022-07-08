package ma.itroad.aace.eth.messaging.endpoint;

import lombok.AllArgsConstructor;
import ma.itroad.aace.eth.core.common.api.messaging.MessagingAPI;
import ma.itroad.aace.eth.core.common.api.messaging.domain.Ack;
import ma.itroad.aace.eth.core.common.api.messaging.domain.AppMessage;
import ma.itroad.aace.eth.core.common.api.messaging.enums.MessageStatus;
import ma.itroad.aace.eth.core.common.api.messaging.enums.MessageType;
import ma.itroad.aace.eth.messaging.service.MailService;
import ma.itroad.aace.eth.messaging.service.PushNotificationService;
import ma.itroad.aace.eth.messaging.service.SmsService;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@RestController
@AllArgsConstructor
public class MessagingEndpoint implements MessagingAPI {

    private final SmsService smsService;
    private final MailService mailService;
    private final PushNotificationService pushNotificationService;

    @Override
    public ResponseEntity<Ack<List<Ack<AppMessage>>>> sendSms(@Valid Collection<AppMessage> messages) {
        return ResponseEntity.ok(smsService.send(messages));
    }

    @Override
    public ResponseEntity<Ack<List<Ack<AppMessage>>>> sendMail(@Valid Collection<AppMessage> messages) {
        return ResponseEntity.ok(mailService.send(messages));
    }

    @Override
    @MessageMapping("/notify")
    public ResponseEntity<AppMessage> sendNotification(@Valid AppMessage message) {
        if (message == null || ArrayUtils.isEmpty(message.getAllRecipients())) {
            return ResponseEntity.ok(null);
        }
        List<AppMessage> messages = new ArrayList<>();
        Arrays.stream(message.getAllRecipients()).distinct().forEach(recipient -> {
            AppMessage appMessage = new AppMessage();
            appMessage.setContent(message.getContent());
            appMessage.setSender(message.getSender());
            appMessage.setType(MessageType.PUSH_NOTIFICATION);
            appMessage.setRecipient(recipient);
            messages.add(appMessage);
        });
        pushNotificationService.send(messages);
        message.setStatus(MessageStatus.DELIVERED);
        return ResponseEntity.ok(message);
    }

    @Override
    public ResponseEntity<Void> sendMailAsync(@Valid Collection<AppMessage> messages) {
        try {
            mailService.sendAsync(messages);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
