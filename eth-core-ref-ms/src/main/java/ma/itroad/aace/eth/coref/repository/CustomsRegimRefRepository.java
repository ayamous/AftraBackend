package ma.itroad.aace.eth.coref.repository;

import ma.itroad.aace.eth.core.repository.BaseJpaRepository;
import ma.itroad.aace.eth.coref.model.entity.CustomsRegimRef;
import ma.itroad.aace.eth.coref.model.mapper.projections.CustomsRegimRefEntityRefLangProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource()
public interface CustomsRegimRefRepository extends BaseJpaRepository<CustomsRegimRef> {


    CustomsRegimRef findByCode(String code);

    Page<CustomsRegimRef> findByCodeIgnoreCaseContains(String code, Pageable pageable);

    @Query("select DISTINCT c from CustomsRegimRef c join c.sanitaryPhytosanitaryMeasuresRef cs where (upper(c.code)  like %:reference% or upper(cs.code) like %:reference%) ")

    Page<CustomsRegimRef> findCustomRegimMsp(String reference, Pageable pageable);


    @Query(value =" select DISTINCT * from eth.customs_regim_ref c " +
            "inner join eth.entity_ref_lang erl " +
            "on (erl.table_ref = 'CUSTOMSREGIM_REF' " +
            "and  c.id= erl.ref_id " +
            "and (erl.label like %:value%  or c.code like %:value%)" +
            "and erl.lang_id = :idLang )"
            ,nativeQuery = true)
    Page<CustomsRegimRef> filterByCodeOrLabel(String value, Long idLang, Pageable pageable);

    @Query(value = "select     " +
            "distinct on (cr.code)   " +
            "cr.id as id,     " +
            "cr.created_on as createdOn,     " +
            "cr.updated_on as updatedOn,     " +
            "cr.code as code,  " +
            "cr.regim_type as regimType, " +
            "erl.label as label,     " +
            "erl.description as description,     " +
            "l.code as lang     " +
            "from eth.customs_regim_ref cr     " +
            "left join eth.entity_ref_lang erl on cr.id = erl.ref_id and erl.table_ref = 'CUSTOMSREGIM_REF' and erl.lang_id = :lang  " +
            "left join eth.lang l on erl.lang_id = l.id     " +
            "where   " +
            "LOWER(cr.code) like %:value%   " +
            "OR   " +
            "LOWER(erl.label) like %:value%",nativeQuery = true)
    Page<CustomsRegimRefEntityRefLangProjection> filterByCodeOrLabelProjection(String value, Long lang, Pageable pageable);
}
