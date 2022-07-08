package ma.itroad.aace.eth.core.service;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import ma.itroad.aace.eth.core.model.entity.AuditEntity;
import ma.itroad.aace.eth.core.model.enums.RequestOriginEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;


public interface IBaseService<E extends AuditEntity, B extends Serializable> {
	E save(E entity);
	E findOneById(Long id);
	List<E> findByCreatedOnOrUpdatedOn(Date date);
	List<E> findByCreatedByOrUpdatedBy(String username);
	List<E> findByRequestOrigin(RequestOriginEnum requestOrigin);
	Page<E> findAll(Specification<E> spec, Pageable pageable);
	Page<B> getAll(Pageable pageable);
}
