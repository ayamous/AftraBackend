package ma.itroad.aace.eth.messaging.service.support;

import java.util.Map;

public interface MessagingContent<T extends Enum> {
    default String prepare(T type) {
        return null;
    }

    default String prepare(T type, Map<String, String> params) {
        return null;
    }

    default String prepare(String templateName, Object params) {
        return null;
    }

    default String prepare(T type, Object params) {
        return null;
    }

    default String prepare(T type, Object... params) {
        return null;
    }
}
