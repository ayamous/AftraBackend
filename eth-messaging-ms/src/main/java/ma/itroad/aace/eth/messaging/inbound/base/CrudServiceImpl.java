package ma.itroad.aace.eth.messaging.inbound.base;

import lombok.AllArgsConstructor;
import ma.itroad.aace.eth.core.model.entity.BaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public abstract class CrudServiceImpl<T extends BaseEntity, ID extends Serializable, R extends JpaRepository<T, ID>> implements CrudService<T, ID> {

    protected final R repository;

    @Override
    public T save(T entity) {
        return repository.save(entity);
    }

    @Override
    public T update(T entity) {
        return repository.save(entity);
    }

    @Override
    public void delete(T entity) {
        repository.delete(entity);
    }

    @Override
    public void deleteById(ID id) {
        repository.deleteById(id);
    }

    @Override
    public List<T> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<T> findById(ID id) {
        return repository.findById(id);
    }
}
