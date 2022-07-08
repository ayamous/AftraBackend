package ma.itroad.aace.eth.messaging.service.support;

import ma.itroad.aace.eth.core.common.api.messaging.enums.PushNotificationContentType;
import org.springframework.stereotype.Service;

@Service
public class PushNotificationContent implements MessagingContent<PushNotificationContentType> {

    @Override
    public String prepare(PushNotificationContentType type) {
        return null;
    }
}
