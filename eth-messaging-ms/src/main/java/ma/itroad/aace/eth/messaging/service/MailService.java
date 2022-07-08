package ma.itroad.aace.eth.messaging.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.itroad.aace.eth.core.common.api.messaging.domain.Ack;
import ma.itroad.aace.eth.core.common.api.messaging.domain.AckStatus;
import ma.itroad.aace.eth.core.common.api.messaging.domain.AppMessage;
import ma.itroad.aace.eth.core.common.api.messaging.enums.MailContentType;
import ma.itroad.aace.eth.core.common.api.messaging.enums.MessageStatus;
import ma.itroad.aace.eth.core.common.api.messaging.enums.MessageType;
import ma.itroad.aace.eth.core.common.utils.CodeUtils;
import ma.itroad.aace.eth.core.common.utils.CollectionUtils;
import ma.itroad.aace.eth.core.common.utils.PatternUtils;
import ma.itroad.aace.eth.messaging.configuration.MailConfig;
import ma.itroad.aace.eth.messaging.core.entity.AppMessageEntity;
import ma.itroad.aace.eth.messaging.inbound.AppMessageService;
import ma.itroad.aace.eth.messaging.service.support.MailContent;
import ma.itroad.aace.eth.messaging.service.support.MessagingAuthorization;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class MailService implements MessagingService {

    private final JavaMailSender mailSender;
    private final AppMessageService appMessageService;
    private final MailContent mailContent;
    private final MessagingAuthorization messagingAuthorization;
    private final ModelMapper mapper;
    private final MailConfig mailConfig;

    @Override
    public List<Ack<AppMessage>> process(Collection<AppMessage> messages) {
        List<Ack<AppMessage>> acks = new ArrayList<>();
        CollectionUtils.parallelConsume(messages, appMessage -> {
            Ack<AppMessage> ack = new Ack<>();
            if (appMessage.getType() == null) {
                appMessage.setType(MessageType.EMAIL_HTML);
            }

            if (MessageStatus.FAILED_DUPLICATED_RECIPIENT.equals(appMessage.getStatus())) {
                ack.of(null, HttpStatus.CONFLICT.value(), "Duplicated recipient", appMessage);
                appMessageService.save(mapper.map(appMessage, AppMessageEntity.class));
                acks.add(ack.data(appMessage));
                return;
            }

            ack.data(appMessage);
            if (!PatternUtils.isEmail(appMessage.getRecipient())) {
                appMessage.setStatus(MessageStatus.FAILED);
                ack.message("Invalid mail address").code(HttpStatus.INTERNAL_SERVER_ERROR.value());
                acks.add(ack);
                appMessageService.save(mapper.map(appMessage, AppMessageEntity.class));
                return;
            }
            Ack<Boolean> authAck = messagingAuthorization.canDeliverTo(appMessage.getRecipient(), MessageType.EMAIL);
            if (!authAck.getData()) {
                ack.of(authAck);
                appMessage.setStatus(MessageStatus.FAILED);
                appMessageService.save(mapper.map(appMessage, AppMessageEntity.class));
                acks.add(ack.data(appMessage));
                return;
            }

            try {
                appMessage.setSender(mailConfig.getSender());
                log.info("Sending mail from {} to {}", appMessage.getSender(), appMessage.getRecipient());
                sendMail(appMessage);
                appMessage.setStatus(MessageStatus.DELIVERED);
                ack.code(AckStatus.ACK_OK);
            } catch (MailException e) {
                log.error("Error while sending message from {} to {}", appMessage.getSender(), appMessage.getRecipient(), e);
                appMessage.setStatus(MessageStatus.FAILED);
                ack.code(AckStatus.ACK_NOK);
            }
            AppMessageEntity entity = mapper.map(appMessage, AppMessageEntity.class);
            ack.data(appMessage);
            acks.add(ack);
            appMessageService.save(entity);
        });
        return acks;
    }

    private void sendMail(AppMessage appMessage) {
        Assert.notNull(appMessage, "Message request must not be null");
        mailSender.send(mimeMessage -> {
            MimeMessageHelper mimeMessageHelper;
            mimeMessageHelper = new MimeMessageHelper(mimeMessage, false);
            mimeMessageHelper.setFrom(appMessage.getSender() == null ? mailConfig.getSender() : appMessage.getSender());
            mimeMessageHelper.setTo(appMessage.getRecipient());
            mimeMessageHelper.setSubject(StringUtils.isEmpty(appMessage.getSubject()) ? "NO NAME" : appMessage.getSubject());
            String content;
            if (appMessage.getContent() == null || appMessage.getContent().isEmpty()) {
                content = mailContent.prepare(MailContentType.ACCOUNT_ACTIVATION);
                if (appMessage.hasTemplateProperties()) {
                    content = mailContent.prepare(String.valueOf(appMessage.getProperty(AppMessage.TEMPLATE_KEY_NAME)), appMessage.getProperty(AppMessage.TEMPLATE_PARAMS_KEY_NAME));
                }
            } else {
                content = appMessage.getContent();
            }
            mimeMessageHelper.setText(content, MessageType.EMAIL_HTML.equals(appMessage.getType()));
        });
    }
}
