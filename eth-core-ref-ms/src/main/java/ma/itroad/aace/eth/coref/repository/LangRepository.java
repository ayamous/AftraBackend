package ma.itroad.aace.eth.coref.repository;

import ma.itroad.aace.eth.core.repository.BaseJpaRepository;
import ma.itroad.aace.eth.coref.model.entity.Lang;
import ma.itroad.aace.eth.coref.model.mapper.projections.LangEntityRefLangProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Date;

@RepositoryRestResource()
public interface LangRepository extends BaseJpaRepository<Lang> {

    Lang findByCode(String code);

    Page<Lang> findByCodeIgnoreCaseContains(String code, Pageable pageable);


    @Query(value =" select DISTINCT * from eth.lang c " +
            " inner join eth.entity_ref_lang erl " +
            "on (erl.table_ref = 'LANG' " +
            "and  c.id= erl.ref_id " +
            "and (erl.label like %:value%  or c.code like %:value%) " +
            "and erl.lang_id = :idLang)"
            ,nativeQuery = true)
    Page<Lang> filterByCodeOrLabel(String value, Long idLang ,Pageable pageable);

    @Query(value = "select     " +
            "distinct on (cr.code)   " +
            "cr.id as id,     " +
            "cr.created_on as createdOn,     " +
            "cr.updated_on as updatedOn,     " +
            "cr.code as code,  " +
            "cr.name as name,  " +
            "cr.def as def,  " +
            "erl.label as label,     " +
            "erl.description as description,     " +
            "l.code as lang     " +
            "from eth.lang cr     " +
            "left join eth.entity_ref_lang erl on cr.id = erl.ref_id and erl.table_ref = 'LANG' and erl.lang_id = :lang  " +
            "left join eth.lang l on erl.lang_id = l.id     " +
            "where   " +
            "LOWER(cr.code) like %:value%   " +
            "OR   " +
            "LOWER(erl.label) like %:value%",nativeQuery = true)
    Page<LangEntityRefLangProjection> filterByCodeOrLabelProjection(String value, Long lang, Pageable pageable);


    //Page<Lang>  findByNameOrderByUpdatedOn(String name);

}
