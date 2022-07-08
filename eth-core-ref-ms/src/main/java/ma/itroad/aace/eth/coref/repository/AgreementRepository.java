package ma.itroad.aace.eth.coref.repository;

import ma.itroad.aace.eth.core.repository.BaseJpaRepository;
import ma.itroad.aace.eth.coref.model.entity.*;
import ma.itroad.aace.eth.coref.model.enums.AgreementStatus;
import ma.itroad.aace.eth.coref.model.mapper.projections.AgreementLangProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.time.LocalDate;
import java.util.List;

@RepositoryRestResource()
public interface AgreementRepository extends BaseJpaRepository<Agreement> {


    Agreement findByCode(String code);

    Page<Agreement> findAgreementByCountryRef_Reference(String countryRef, Pageable pageable);

    @Query("select DISTINCT a from Agreement a  where  ( a.countryRef IN  (:countryReferences)) " )
    Page<Agreement> findByCountryRefInList(List<CountryRef> countryReferences, Pageable pageable);


    @Query("select DISTINCT a from Agreement a  where (:title is null or a.title  like %:title% ) " +
            "AND (:countryRefCode is null or a.countryRef.reference = :countryRefCode)  " +
            "AND (:agreementStatus is null  or a.agreementStatus = :agreementStatus)" +
            "AND (:agreementType is null or a.agreementType.code = :agreementType)" +
            "AND (CAST(:dateOfAgreement AS date) is null or a.dateOfAgreement >= :dateOfAgreement)" )
    Page<Agreement> findByFilter( String title ,String countryRefCode , String agreementType , AgreementStatus agreementStatus  , LocalDate dateOfAgreement , Pageable pageable);


    @Query(value = "select     " +
            "cr.id as id,     " +
            "cr.created_on as createdOn,     " +
            "cr.updated_on as updatedOn,     " +
            "cr.code as code,     " +
            "cr.version_nbr as version_nbr,      " +
            "cr.request_origin as requestOrigin,      " +
            "cr.title as title,    " +
            "cr.agreement_status as agreementStatus,    " +
            "agt.code as agreementType,    " +
            "cr.country_group_ref_id as countryGroupRef,    " +
            "cr.description as description,    " +
            "cr.date_of_agreement as dateOfAgreement,    " +
            "ctr.reference as countryRef,       " +
            "erl.label as label,     " +
            "erl.general_description as generalDescription,     " +
            "l.code as lang     " +
            "from eth.agreement cr     " +
            "left join eth.entity_ref_lang erl on erl.ref_id = cr.id     " +
            "left join eth.lang l on erl.lang_id = l.id     " +
            "left join eth.country_ref ctr on ctr.id = cr.country_ref_id    " +
            "left join eth.country_group_ref cgr on cgr.id = cr.country_group_ref_id    " +
            "left join eth.agreement_type agt on agt.id = cr.agreement_type_id   " +
            "where   " +
            "erl.table_ref = 'AGREEMENT'     " +
            "and  " +
            "erl.lang_id = (select l.id from eth.lang l where l.code = :lang)    " +
            "and " +
            "( " +
            " cr.code like %:code% " +
            " AND " +
            " cr.agreement_status = :agreementStatus " +
            " AND " +
            " ctr.reference like %:countryRefCode% " +
            " AND " +
            " agt.code like %:agreementType%  " +
            " AND " +
            " cr.date_of_agreement = :dateOfAgreement )",nativeQuery = true)
    Page<AgreementLangProjection> findByFilterProjection(String code , String countryRefCode , String agreementType , int agreementStatus  , LocalDate dateOfAgreement ,String lang, Pageable pageable);

    @Query("select DISTINCT agr from Agreement agr join agr.tarifBookRefs t" +
            " where ( :tarifBookReference is null or t.reference=:tarifBookReference ) " +
            " and (coalesce(:countries, null) is null or agr.countryRef.reference in (:countries)) ")
    Page<Agreement> findByFilter(String tarifBookReference,List <String> countries, Pageable pageable);

    @Query("select DISTINCT agr from Agreement agr join agr.tarifBookRefs t " +
            " where (:countryRef is null or agr.countryRef.reference=:countryRef) " +
            " and (:tarifBookReference is null or t.reference like %:tarifBookReference%)")
    Page<Agreement> SubportalProductInformationFinderFilter(String tarifBookReference, String countryRef, Pageable pageable);


    Page<Agreement> findByCodeIgnoreCaseContains(String code, Pageable pageable);


    @Query(value =" select * from eth.agreement c " +
            " inner join eth.entity_ref_lang erl " +
            "on (erl.table_ref = 'AGREEMENT' " +
            "and  c.id= erl.ref_id " +
            "and (erl.label like %:value%  or c.code like %:value%) " +
            "and erl.lang_id = :idLang)"
            ,nativeQuery = true)
    Page<Agreement> filterByCodeOrLabel(String value,Long idLang, Pageable pageable);



    @Query(value = "select     " +
            "cr.id as id,     " +
            "cr.created_on as createdOn,     " +
            "cr.updated_on as updatedOn,     " +
            "cr.code as code,     " +
            "cr.version_nbr as version_nbr,      " +
            "cr.request_origin as requestOrigin,      " +
            "cr.title as title,    " +
            "cr.general_description as generalDescription,    " +
            "cr.agreement_status as agreementStatus,    " +
            "agt.code as agreementType,    " +
            "cgr.code as countryGroupRef,    " +
            "cr.description as description,    " +
            "cr.date_of_agreement as dateOfAgreement,    " +
            "ctr.reference as countryRef,      " +
            "erl.label as label,     " +
            "erl.description as langDescription,     " +
            "l.code as lang     " +
            "from eth.agreement cr        " +
            "left join eth.country_ref ctr on ctr.id = cr.country_ref_id    " +
            "left join eth.country_group_ref cgr on cgr.id = cr.country_group_ref_id    " +
            "left join eth.agreement_type agt on agt.id = cr.agreement_type_id  " +
            "left join eth.entity_ref_lang erl on cr.id = erl.ref_id and erl.table_ref = 'AGREEMENT' and erl.lang_id = :lang " +
            "left join eth.lang l on erl.lang_id = l.id " +
            "where " +
            "LOWER(cr.code) like %:value% " +
            "OR " +
            "LOWER(erl.label) like %:value%"
            ,nativeQuery = true)
    Page<AgreementLangProjection> filterByCodeOrLabelProjection(String value,Long lang, Pageable pageable);
}
