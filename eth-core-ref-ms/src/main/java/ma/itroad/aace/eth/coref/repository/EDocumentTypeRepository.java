package ma.itroad.aace.eth.coref.repository;

import ma.itroad.aace.eth.core.repository.BaseJpaRepository;
import ma.itroad.aace.eth.coref.model.entity.CountryRef;
import ma.itroad.aace.eth.coref.model.entity.EDocumentType;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.mapper.projections.EDocTypeRefEntityRefLangProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EDocumentTypeRepository extends BaseJpaRepository<EDocumentType> {

    EDocumentType findByCode(String code);



    @Query(value = "select  " +
            "cr.id as id,  " +
            "cr.created_on as createdOn,  " +
            "cr.updated_on as updatedOn,  " +
            "cr.code as code,  " +
            "cr.doc_type as doc_type, " +
            "erl.label as label,  " +
            "erl.description as description,  " +
            "l.code as lang  " +
            "from eth.e_document_type cr  " +
            "left join eth.entity_ref_lang erl on erl.ref_id = cr.id  " +
            "left join eth.lang l on erl.lang_id = l.id  " +
            "where   " +
            "erl.table_ref = 'EDOCUMENT_TYPE'  " +
            "and  " +
            "erl.lang_id = (select l.id from eth.lang l where l.code = :lang ) " +
            "and " +
            "( " +
            " erl.ref_id in (select r.id from eth.e_document_type r where r.code like %:value% ) " +
            " OR  " +
            " erl.label like %:value% )",nativeQuery = true)
    Page<EDocTypeRefEntityRefLangProjection> filterByReferenceOrLabelProjection(String value, String lang, Pageable pageable);
}
