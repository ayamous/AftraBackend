package ma.itroad.aace.eth.coref.repository;

import ma.itroad.aace.eth.core.repository.BaseJpaRepository;
import ma.itroad.aace.eth.coref.model.entity.CityRef;
import ma.itroad.aace.eth.coref.model.mapper.projections.CityRefEntityRefLangProjection;
import ma.itroad.aace.eth.coref.service.helper.CityRefEntityRefLang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource()
public interface CityRefRepository extends BaseJpaRepository<CityRef> {
    CityRef findByReference(String reference);

    Page<CityRef> findByReferenceIgnoreCaseContains(String reference, Pageable pageable);

    @Query(value =" select * from eth.city_ref c " +
            " inner join eth.entity_ref_lang erl " +
            "on (erl.table_ref = 'CITY_REF' " +
            "and  c.id= erl.ref_id " +
            "and (erl.label like %:value%  or c.reference like %:value%)" +
            "and erl.lang_id = :idLang )"
            ,nativeQuery = true)
    Page<CityRef> filterByReferenceOrLabel(String value,Long idLang , Pageable pageable);

    @Query(value = "select   " +
            "distinct on (cr.reference) " +
            "cr.id as id,   " +
            "cr.created_on as createdOn,   " +
            "cr.updated_on as updatedOn,   " +
            "cr.reference as reference,   " +
            "erl.label as label,   " +
            "erl.description as description,   " +
            "ct.reference as countryRef,   " +
            "l.code as lang   " +
            "from eth.city_ref cr   " +
            "inner join eth.country_ref ct on ct.id = cr.country_ref_id    " +
            "left join eth.entity_ref_lang erl on cr.id = erl.ref_id and erl.table_ref = 'CITY_REF' and erl.lang_id = :lang " +
            "left join eth.lang l on erl.lang_id = l.id   " +
            "where " +
            "LOWER(cr.reference) like %:value% " +
            "OR " +
            "LOWER(erl.label) like %:value%"
            ,nativeQuery = true)
    Page<CityRefEntityRefLangProjection>  filCityRefsProjection(String value, Long lang , Pageable pageable);

}
