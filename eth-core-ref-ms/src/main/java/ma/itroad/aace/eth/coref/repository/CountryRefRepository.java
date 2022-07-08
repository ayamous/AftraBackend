package ma.itroad.aace.eth.coref.repository;

import ma.itroad.aace.eth.core.repository.BaseJpaRepository;
import ma.itroad.aace.eth.coref.model.entity.CountryRef;
import ma.itroad.aace.eth.coref.model.mapper.projections.CountryRefEntityRefLangProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource()
public interface CountryRefRepository extends BaseJpaRepository<CountryRef> {

    CountryRef findByReference(String reference);
    CountryRef findByCodeIso(String codeIso);
    Page<CountryRef> findByReferenceIgnoreCaseContains(String reference, Pageable pageable);

    @Query(value =" select * from eth.country_ref c " +
            "inner join eth.entity_ref_lang erl " +
            "on (erl.table_ref = 'COUNTRY_REF' " +
            "and  c.id= erl.ref_id " +
            "and (erl.label like %:value%  or c.reference like %:value%) " +
            "and erl.lang_id = :idLang)"
            ,nativeQuery = true)
    Page<CountryRef> filterByReferenceOrLabel(String value, Long idLang ,Pageable pageable);

    @Query(value ="select " +
            "distinct on (cr.reference) " +
            "cr.id as id,    " +
            "cr.reference as reference,   " +
            "cr.code_iso as codeIso,   " +
            "erl.label as label,    " +
            "erl.table_ref, " +
            "erl.description as description,   " +
            "l.code as lang   " +
            "from eth.country_ref cr    " +
            "left join eth.entity_ref_lang erl on cr.id = erl.ref_id and erl.table_ref = 'COUNTRY_REF' and erl.lang_id = :lang " +
            "left join eth.lang l on erl.lang_id = l.id    " +
            "where " +
            "LOWER(cr.reference) like %:value% " +
            "OR " +
            "LOWER(erl.label) like  %:value% "
            ,nativeQuery = true)
    Page<CountryRefEntityRefLangProjection> filterByReferenceOrLabelProjection(String value, Long lang , Pageable pageable);


}
