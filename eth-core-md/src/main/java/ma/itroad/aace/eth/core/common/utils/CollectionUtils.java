package ma.itroad.aace.eth.core.common.utils;

import java.util.Collection;
import java.util.function.Consumer;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

public class CollectionUtils {
    private CollectionUtils () {}

    public static boolean isNullOrEmpty(Collection<?> collection) {
        return isEmpty(collection);
    }
    public static boolean isNotEmpty(Collection<?> collection) {
        return !isNullOrEmpty(collection);
    }

    public static <T> void parallelConsume(Collection<T> collection, Consumer<T> consumer) {
        collection.parallelStream().forEach(consumer);
    }
}
