package ma.itroad.aace.eth.messaging.inbound.base;

import ma.itroad.aace.eth.core.model.entity.BaseEntity;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public interface CrudService<T extends BaseEntity, ID extends Serializable> {
    T save(@NotNull T entity);

    T update(@NotNull T entity);

    void delete(@NotNull T entity);

    void deleteById(@NotNull ID id);

    List<T> findAll();

    Optional<T> findById(@NotNull ID id);
}
