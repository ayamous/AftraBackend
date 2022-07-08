package ma.itroad.aace.eth.messaging.inbound.impl;

import ma.itroad.aace.eth.core.common.api.messaging.domain.AppMessage;
import ma.itroad.aace.eth.core.common.api.messaging.enums.MessageStatus;
import ma.itroad.aace.eth.messaging.core.entity.AppMessageEntity;
import ma.itroad.aace.eth.messaging.inbound.AppMessageService;
import ma.itroad.aace.eth.messaging.inbound.base.CrudServiceImpl;
import ma.itroad.aace.eth.messaging.repository.AppMessageJpaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppMessageServiceImpl extends CrudServiceImpl<AppMessageEntity, Long, AppMessageJpaRepository> implements AppMessageService {

    @Value("${aace.eth.message.max-retry:10}")
    private int retry;

    public AppMessageServiceImpl(AppMessageJpaRepository repository) {
        super(repository);
    }

    @Override
    public List<AppMessageEntity> findAllByRecipient(String recipient) {
        return repository.findAllByRecipient(recipient);
    }

    @Override
    public List<AppMessageEntity> findAllByRecipientAndCreatedAtAfter(String recipient, LocalDateTime createdAt) {
        return repository.findAllByRecipientAndCreatedAtAfter(recipient, createdAt);
    }

    @Override
    public long countAllByRecipientAndCreatedAtAfter(String recipient, LocalDateTime createdAt) {
        return repository.countAllByRecipientAndCreatedAtAfter(recipient, createdAt);
    }

    @Override
    public AppMessageEntity findLastByRecipientAndCreatedAt(String recipient) {
        return repository.findTopByRecipientOrderByCreatedAtDesc(recipient);
    }

    @Override
    public long countAllByRecipientAndStatusAndCreatedAtAfter(String recipient, MessageStatus status, LocalDateTime createdAt) {
        return repository.countAllByRecipientAndStatusAndCreatedAtAfter(recipient, status, createdAt);
    }

    @Override
    public AppMessageEntity sendMail(AppMessage appMessage) {
        return null;
    }

    @Override
    public AppMessageEntity sendSms(AppMessage appMessage) {
        return null;
    }

    @Override
    public List<AppMessageEntity> findAllRetryable() {
        return repository.findAllByRetryLessThanEqualAndStatusIn(retry, MessageStatus.FAILED);
    }
}
