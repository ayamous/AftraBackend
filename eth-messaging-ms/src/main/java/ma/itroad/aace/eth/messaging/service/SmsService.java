package ma.itroad.aace.eth.messaging.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.itroad.aace.eth.core.common.api.messaging.domain.Ack;
import ma.itroad.aace.eth.core.common.api.messaging.domain.AppMessage;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;


@Service
@AllArgsConstructor
@Slf4j
public class SmsService implements MessagingService {

    @Override
    public List<Ack<AppMessage>> process(Collection<AppMessage> messages) {
        return null;
    }
}
