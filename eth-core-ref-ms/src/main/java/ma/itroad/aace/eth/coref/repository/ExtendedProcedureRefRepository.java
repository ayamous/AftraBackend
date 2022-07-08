package ma.itroad.aace.eth.coref.repository;

import ma.itroad.aace.eth.core.repository.BaseJpaRepository;
import ma.itroad.aace.eth.coref.model.entity.ExtendedProcedureRef;
import ma.itroad.aace.eth.coref.model.mapper.projections.ExtendedProcedureRefLangProjection;
import ma.itroad.aace.eth.coref.service.helper.ExtendedProcedureRefLang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource()
public interface ExtendedProcedureRefRepository extends BaseJpaRepository<ExtendedProcedureRef> {

    ExtendedProcedureRef findByCode(String code);

    Page<ExtendedProcedureRef> findByCodeIgnoreCaseContains(String code, Pageable pageable);
    
    @Query(value = "select * from eth.extended_procedure_ref c inner JOIN eth.entity_ref_lang e on c.id = e.ref_id where e.table_ref = 'EXTENDED_PROCEDURE_REF' and e.lang_id = :idLang order by c.created_on desc", nativeQuery = true)
	Page<ExtendedProcedureRef> findAllByEntityRefAndLang(@Param("idLang") Long idLang, Pageable pageable);


    @Query(value = "select     " +
            "distinct on (cr.code)   " +
            "cr.id as id,     " +
            "cr.created_on as createdOn,     " +
            "cr.updated_on as updatedOn,     " +
            "cr.code as code,  " +
            "erl.label as label,     " +
            "erl.description as description,     " +
            "l.code as lang     " +
            "from eth.extended_procedure_ref cr     " +
            "left join eth.entity_ref_lang erl on cr.id = erl.ref_id and erl.table_ref = 'EXTENDED_PROCEDURE_REF' and erl.lang_id = :lang  " +
            "left join eth.lang l on erl.lang_id = l.id     " +
            "where   " +
            "LOWER(cr.code) like %:value%   " +
            "OR   " +
            "LOWER(erl.label) like %:value%",nativeQuery = true)
    Page<ExtendedProcedureRefLangProjection> findByCodeOrLabelProjection(String value, Long lang, Pageable pageable);
}
