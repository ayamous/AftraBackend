package ma.itroad.aace.eth.coref.repository;

import ma.itroad.aace.eth.core.repository.BaseJpaRepository;
import ma.itroad.aace.eth.coref.model.entity.RefCurrency;
import ma.itroad.aace.eth.coref.model.mapper.projections.RefCurrencyRefEntityRefLangProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource()
public interface RefCurrencyRepository extends BaseJpaRepository<RefCurrency> {

    RefCurrency findByCode(String code);

    Page<RefCurrency> findByCodeIgnoreCaseContains(String code, Pageable pageable);

    @Query(value =" select DISTINCT * from eth.ref_currency c " +
            " inner join eth.entity_ref_lang erl " +
            "on (erl.table_ref = 'CURRENCY_REF' " +
            "and  c.id= erl.ref_id " +
            "and (erl.label like %:value%  or c.code like %:value%)" +
            "and erl.lang_id = :idLang )"
            ,nativeQuery = true)
    Page<RefCurrency> filterByCodeOrLabel(String value, Long idLang, Pageable pageable);

    @Query(value = "select     " +
            "distinct on (cr.code)   " +
            "cr.id as id,     " +
            "cr.created_on as createdOn,     " +
            "cr.updated_on as updatedOn,     " +
            "cr.code as code,  " +
            "erl.label as label,     " +
            "erl.description as description,     " +
            "l.code as lang     " +
            "from eth.ref_currency cr     " +
            "left join eth.entity_ref_lang erl on cr.id = erl.ref_id and erl.table_ref = 'CURRENCY_REF' and erl.lang_id = :lang   " +
            "left join eth.lang l on erl.lang_id = l.id     " +
            "where   " +
            "LOWER(cr.code) like %:value%   " +
            "OR   " +
            "LOWER(erl.label) like %:value%",nativeQuery = true)
    Page<RefCurrencyRefEntityRefLangProjection> filterByCodeOrLabelProjection(String value, Long lang, Pageable pageable);
}
