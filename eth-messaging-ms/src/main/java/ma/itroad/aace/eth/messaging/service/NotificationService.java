package ma.itroad.aace.eth.messaging.service;

import ma.itroad.aace.eth.core.common.api.messaging.domain.AppMessage;

public interface NotificationService {

    AppMessage notify(AppMessage message);
}
