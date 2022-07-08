package ma.itroad.aace.eth.coref.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;

public interface ICrudService<M extends Serializable,B extends Serializable> {

    public B save(M model);

    M findOneById(B bean);

    Page<M> getAll(Pageable pageable);

    void delete(M model);
}
