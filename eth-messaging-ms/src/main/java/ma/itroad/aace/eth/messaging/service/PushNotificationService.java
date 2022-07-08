package ma.itroad.aace.eth.messaging.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.itroad.aace.eth.core.common.api.messaging.domain.Ack;
import ma.itroad.aace.eth.core.common.api.messaging.domain.AppMessage;
import ma.itroad.aace.eth.core.common.api.messaging.enums.MessageStatus;
import ma.itroad.aace.eth.messaging.core.entity.AppMessageEntity;
import ma.itroad.aace.eth.messaging.inbound.AppMessageService;
import org.modelmapper.ModelMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class PushNotificationService implements MessagingService, NotificationService {

    private final AppMessageService appMessageService;
    private final ModelMapper modelMapper;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Override
    public List<Ack<AppMessage>> process(Collection<AppMessage> messages) {
        return messages.stream().map(appMessage -> {
            log.info("Sending a push notification to {}", appMessage.getRecipient());
            Ack<AppMessage> ack = new Ack<>();
            appMessage.setStatus(MessageStatus.DELIVERED);
            appMessageService.save(modelMapper.map(appMessage, AppMessageEntity.class));
            ack.ok("Notification delivered", appMessage);
            simpMessagingTemplate.convertAndSend("/topic/".concat(appMessage.getRecipient()), ack);
            return ack;
        }).collect(Collectors.toList());
    }

    @Override
    public AppMessage notify(AppMessage message) {
        Arrays.stream(message.getAllRecipients()).distinct().forEach(s -> {
            simpMessagingTemplate.convertAndSend("/topic/".concat(s), message);
        });
        message.setStatus(MessageStatus.DELIVERED);
        return message;
    }
}
