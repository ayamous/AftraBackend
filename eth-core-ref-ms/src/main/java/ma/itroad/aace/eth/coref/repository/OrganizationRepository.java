package ma.itroad.aace.eth.coref.repository;

import ma.itroad.aace.eth.core.repository.BaseJpaRepository;
import ma.itroad.aace.eth.coref.model.entity.*;
import ma.itroad.aace.eth.coref.projection.OrganizationProjectionId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource()
public interface OrganizationRepository extends BaseJpaRepository<Organization> {

    Optional<Organization> findByReferenceAndParentAndCityRefAndCategoryRef(String reference, Organization parent, CityRef cityRef, CategoryRef categoryRef);

    //Optional<Organization> findByReference(String reference);

    Organization findByReference(String code);

    List<OrganizationProjectionId> findAllByCountryRef_Id(Long id);

    List<OrganizationProjectionId> findAllByIdNotNull();

    Page<Organization> findByReferenceIgnoreCaseContains(String reference, Pageable pageable);

    @Query(value = " SELECT DISTINCT * FROM eth.organization o " +
            "WHERE (o.reference like %:value%  or o.name like %:value%)", nativeQuery = true)
    Page<Organization> filterByReferenceOrName(String value, Pageable pageable);


}


