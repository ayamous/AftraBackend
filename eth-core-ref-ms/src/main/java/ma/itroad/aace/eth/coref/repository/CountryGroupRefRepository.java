package ma.itroad.aace.eth.coref.repository;

import ma.itroad.aace.eth.core.repository.BaseJpaRepository;
import ma.itroad.aace.eth.coref.model.entity.CountryGroupRef;
import ma.itroad.aace.eth.coref.model.entity.Lang;
import ma.itroad.aace.eth.coref.model.mapper.projections.CountryGroupReferenceJoinProjection;
import ma.itroad.aace.eth.coref.model.mapper.projections.CountryGroupRefsEntityRefLangProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource()
public interface CountryGroupRefRepository extends BaseJpaRepository<CountryGroupRef> {

    CountryGroupRef findByCode(String code);

    CountryGroupRef findByReference(String reference);

    Page<CountryGroupRef> findByCodeIgnoreCaseContains(String code, Pageable pageable);

    Page<CountryGroupRef> findByReferenceIgnoreCaseContains(String reference, Pageable pageable);


    @Query("select Distinct c from CountryGroupRef c join  c.countryRefs cr where (upper(c.reference) like %:reference% or upper(cr.reference) like %:reference%) ")
    Page<CountryGroupRef> findCountryGroupRefByRef(String reference, Pageable pageable);


    @Query(value =" select * from eth.country_group_ref c " +
            " inner join eth.entity_ref_lang erl " +
            "on (erl.table_ref = 'COUNTRY_GROUP_REF' " +
            "and  c.id= erl.ref_id " +
            "and (erl.label like %:value%  or c.reference like %:value%) " +
            "and erl.lang_id = :idLang)"
            ,nativeQuery = true)
    Page<CountryGroupRef> filterByReferenceOrLabel(String value,Long idLang , Pageable pageable);


    @Query(value = "select     " +
            "distinct on (cr.code)   " +
            "cr.id as id,     " +
            "cr.created_on as createdOn,     " +
            "cr.updated_on as updatedOn,     " +
            "cr.code as code,  " +
            "cr.reference as reference, " +
            "erl.label as label,     " +
            "erl.description as description,     " +
            "l.code as lang     " +
            "from eth.country_group_ref cr     " +
            "left join eth.entity_ref_lang erl on cr.id = erl.ref_id and erl.table_ref = 'COUNTRY_GROUP_REF' and erl.lang_id = :lang  " +
            "left join eth.lang l on erl.lang_id = l.id     " +
            "where   " +
            "LOWER(cr.code) like %:value%   " +
            "OR   " +
            "LOWER(erl.label) like %:value%",nativeQuery = true)
    Page<CountryGroupRefsEntityRefLangProjection> filterByReferenceOrLabelProjection(String value, Lang lang, Pageable pageable);

    @Query(value = "select       " +
            "distinct on (cr.reference,cgr.reference)     " +
            "cr.reference as countryGroupReference,       " +
            "cgr.reference as countryReference " +
            "from eth.country_ref cr    " +
            "inner join eth.country_group_join_ref cgj on cr.id = cgj.country_ref_id  " +
            "inner join eth.country_group_ref cgr on cgr.id = cgj.country_group_ref_id " +
            "where     " +
            "LOWER(cr.reference) like %:reference%     " +
            "OR     " +
            "LOWER(cgr.reference) like %:reference%",nativeQuery = true)
    Page<CountryGroupReferenceJoinProjection> findByReferenceProjection(String reference,Pageable pageable);
}
