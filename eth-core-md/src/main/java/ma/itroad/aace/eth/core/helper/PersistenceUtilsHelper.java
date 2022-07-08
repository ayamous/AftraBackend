package ma.itroad.aace.eth.core.helper;

import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;

public class PersistenceUtilsHelper {

    @SuppressWarnings({ "unchecked" })
    public static <T> T unProxifyEntity(final T proxied) {
        T entity = proxied;
        if (entity != null && entity instanceof HibernateProxy) {
            Hibernate.initialize(entity);
            entity = (T) ((HibernateProxy) entity).getHibernateLazyInitializer().getImplementation();
        }
        return entity;
    }
}
