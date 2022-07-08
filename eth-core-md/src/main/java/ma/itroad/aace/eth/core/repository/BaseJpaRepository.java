package ma.itroad.aace.eth.core.repository;

import java.util.Date;
import java.util.List;

import ma.itroad.aace.eth.core.model.entity.AuditEntity;
import ma.itroad.aace.eth.core.model.enums.RequestOriginEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseJpaRepository<T extends AuditEntity> extends JpaRepository<T, Long>, JpaSpecificationExecutor<T> {

    T findOneById(Long id);
    List<T> findByCreatedOnOrUpdatedOn(Date createdOn, Date updatedOn);
    List<T> findByCreatedByOrUpdatedBy(String createdby, String updatedBy);
    List<T> findByRequestOrigin(RequestOriginEnum requestOrigin);
    Page<T> findAll(Specification<T> spec, Pageable pageable);
}

