package ma.itroad.aace.eth.coref.repository;

import ma.itroad.aace.eth.core.repository.BaseJpaRepository;
import ma.itroad.aace.eth.coref.model.entity.RefPackaging;
import ma.itroad.aace.eth.coref.model.mapper.projections.RefPackagingEntityRefLangProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource()
public interface RefPackagingRepository extends BaseJpaRepository<RefPackaging> {

    RefPackaging findByCode(String code);

    Page<RefPackaging> findByCodeIgnoreCaseContains(String code, Pageable pageable);

    @Query(value =" select DISTINCT * from eth.ref_packaging c " +
            "inner join eth.entity_ref_lang erl " +
            "on (erl.table_ref = 'PACKAGING_REF' " +
            "and  c.id= erl.ref_id " +
            "and (erl.label like %:value%  or c.code like %:value%)" +
            "and erl.lang_id = :idLang )"
            ,nativeQuery = true)
    Page<RefPackaging> filterByCodeOrLabel(String value, Long idLang, Pageable pageable);

    @Query(value = "select     " +
            "distinct on (cr.code)   " +
            "cr.id as id,     " +
            "cr.created_on as createdOn,     " +
            "cr.updated_on as updatedOn,     " +
            "cr.code as code,  " +
            "erl.label as label,     " +
            "erl.description as description,     " +
            "l.code as lang     " +
            "from eth.ref_packaging cr     " +
            "left join eth.entity_ref_lang erl on cr.id = erl.ref_id and erl.table_ref = 'PACKAGING_REF' and erl.lang_id = :lang   " +
            "left join eth.lang l on erl.lang_id = l.id     " +
            "where   " +
            "LOWER(cr.code) like %:value%   " +
            "OR   " +
            "LOWER(erl.label) like %:value%",nativeQuery = true)
    Page<RefPackagingEntityRefLangProjection> filterByCodeOrLabelProjection(String value, Long lang, Pageable pageable);
}
