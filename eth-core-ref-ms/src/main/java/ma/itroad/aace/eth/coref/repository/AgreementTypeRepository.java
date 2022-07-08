package ma.itroad.aace.eth.coref.repository;


import ma.itroad.aace.eth.core.repository.BaseJpaRepository;
import ma.itroad.aace.eth.coref.model.entity.AgreementType;
import ma.itroad.aace.eth.coref.model.mapper.projections.AgreementLangProjection;
import ma.itroad.aace.eth.coref.model.mapper.projections.AgreementTypeLangProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;


@RepositoryRestResource()
public interface AgreementTypeRepository extends BaseJpaRepository<AgreementType> {
    AgreementType  findByCode(String code) ;

    @Query(value =" select * from eth.agreement_type c " +
            " inner join eth.entity_ref_lang erl " +
            "on (erl.table_ref = 'AGREEMENT_TYPE' " +
            "and  c.id= erl.ref_id " +
            "and (erl.label like %:value%  or c.code like %:value%) " +
            "and erl.lang_id = :idLang)"
            ,nativeQuery = true)
    Page<AgreementType> filterByCodeOrLabel(String value, Long idLang, Pageable pageable);

    @Query(value ="select    " +
            "distinct on (cr.code)  " +
            "cr.id as id,    " +
            "cr.created_on as createdOn,    " +
            "cr.updated_on as updatedOn,   " +
            "cr.name as name,  " +
            "cr.code as code,    " +
            "erl.label as label,    " +
            "erl.description as description,    " +
            "l.code as lang    " +
            "from eth.agreement_type cr    " +
            "left join eth.entity_ref_lang erl on cr.id = erl.ref_id and erl.table_ref = 'AGREEMENT_TYPE' and erl.lang_id =  :lang  " +
            "left join eth.lang l on erl.lang_id = l.id   " +
            "where    " +
            "LOWER(cr.code) like %:value%    " +
            "OR    " +
            "LOWER(erl.label) like %:value%"
            ,nativeQuery = true)
    Page<AgreementTypeLangProjection> filterByCodeOrLabelProjection(String value, Pageable pageable, Long lang);
}
