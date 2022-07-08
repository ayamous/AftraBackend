package ma.itroad.aace.eth.coref.repository;

import ma.itroad.aace.eth.core.repository.BaseJpaRepository;
import ma.itroad.aace.eth.coref.model.entity.TaxRef;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;


@RepositoryRestResource()
public interface TaxRefRepository extends BaseJpaRepository<TaxRef> {

    @Query("SELECT u FROM TaxRef u WHERE lower(u.code) LIKE %:code% ")
    Page<TaxRef> findAllByCode(String code,Pageable pageable);

    TaxRef findByCode(String code);

    Page<TaxRef> findByCodeIgnoreCaseContains(String code, Pageable pageable);
}
