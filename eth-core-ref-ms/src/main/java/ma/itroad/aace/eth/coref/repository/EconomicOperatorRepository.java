package ma.itroad.aace.eth.coref.repository;

import ma.itroad.aace.eth.core.repository.BaseJpaRepository;
import ma.itroad.aace.eth.coref.model.entity.EconomicOperator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource()
public interface EconomicOperatorRepository extends BaseJpaRepository<EconomicOperator> {

    EconomicOperator findByCode(String code);

    Page<EconomicOperator> findByCodeIgnoreCaseContains(String code, Pageable pageable);
}
