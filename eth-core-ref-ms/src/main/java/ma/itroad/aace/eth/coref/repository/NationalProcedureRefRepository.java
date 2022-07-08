package ma.itroad.aace.eth.coref.repository;

import ma.itroad.aace.eth.core.repository.BaseJpaRepository;
import ma.itroad.aace.eth.coref.model.entity.CountryRef;
import ma.itroad.aace.eth.coref.model.entity.NationalProcedureRef;
import ma.itroad.aace.eth.coref.model.entity.Taxation;
import ma.itroad.aace.eth.coref.model.mapper.projections.NationalProcedureRefLangProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource()
public interface NationalProcedureRefRepository extends BaseJpaRepository<NationalProcedureRef> {

    NationalProcedureRef findByCode(String code);

    Page<NationalProcedureRef> findByCodeIgnoreCaseContains(String code,Pageable pageable);

    @Query("select DISTINCT p from NationalProcedureRef p join p.tarifBookRefs t" +
            " where (:customRegimCode is null or p.customsRegimRef.code=:customRegimCode) " +
            " and (:countryRef is null or p.countryRef.reference=:countryRef) " +
            " and (:tarifBookReference is null or  t.reference like %:tarifBookReference% ) ")
    Page<NationalProcedureRef> procedureSubportalProductInformationFinderFilter(String tarifBookReference,String customRegimCode, String countryRef, Pageable pageable);

    //@Query("select DISTINCT p from NationalProcedureRef p ")
    @Query("select DISTINCT na from NationalProcedureRef na " +
            " where ( :customRegimCode is null or na.customsRegimRef.code = :customRegimCode  )" +
            " AND (:procedureCode is null or  na.code like %:procedureCode% ) " +
            " AND (:countryReference is null or na.countryRef.reference = :countryReference )   " )
    Page<NationalProcedureRef> nationalProcedureFilter(String procedureCode, String customRegimCode, String countryReference, Pageable pageable);

   //" AND (:organizationReference is null or na.organization.reference = :organizationReference ) "
    @Query("select DISTINCT n from NationalProcedureRef n  " +
            " where (:code is null or  n.code like %:code% ) " +
            " AND  (coalesce(:countries, null) is null or n.countryRef.reference in (:countries))  ")
    Page<NationalProcedureRef> findProcedureWithCountries(List<String> countries, String code, Pageable pageable);

    @Query("select DISTINCT na from NationalProcedureRef na join na.tarifBookRefs t" +
            " where ( :tarifBookReference is null or t.reference=:tarifBookReference ) " +
            " and (coalesce(:countries, null) is null or na.countryRef.reference in (:countries)) ")
    Page<NationalProcedureRef> findByFilter(String tarifBookReference, List<String> countries, Pageable pageable);

    @Query("select DISTINCT na from NationalProcedureRef na join na.tarifBookRefs t " +
            " where t.reference =:tarifBookReference " +
            " and na.countryRef.reference in (:countries) " )
    Page<NationalProcedureRef> findByFilterWithTarifBook(String tarifBookReference,@Param("countries") List <String> countries, Pageable pageable);


    @Query("select DISTINCT na from NationalProcedureRef na  where  na.countryRef in (:countries)  ")
    Page<NationalProcedureRef> findByFilterWithoutTarifBook(List<CountryRef> countries, Pageable pageable);


    @Query(value =" select * from eth.national_procedure_ref c " +
            " inner join eth.entity_ref_lang erl " +
            "on (erl.table_ref = 'NATIONAL_PROCEDURE_REF' " +
            "and  c.id= erl.ref_id " +
            "and (erl.label like %:value%  or c.code like %:value%) " +
            "and erl.lang_id = :idLang)"
            ,nativeQuery = true)
    Page<NationalProcedureRef> filterByCodeOrLabel(String value, Long idLang, Pageable pageable);

    @Query(value ="select   " +
            "cr.id as id,   " +
            "cr.created_on as createdOn,  " +
            "cr.updated_on as updatedOn,  " +
            "cr.version_nbr as version_nbr,  " +
            "cr.request_origin as requestOrigin,  " +
            "cr.code as code,  " +
            "erl.label as label,  " +
            "erl.general_description as generalDescription,     " +
            "ct.reference as countryRef,  " +
            "l.code as lang,  " +
            "crr.code as customsRegimRef,  " +
            "og.reference as organization   " +
            "from eth.national_procedure_ref cr   " +
            "left join eth.country_ref ct on ct.id = cr.country_ref_id    " +
            "left join eth.customs_regim_ref crr ON crr.id = cr.customs_regim_ref_id   " +
            "left join eth.organization og on og.id = cr.organization_id    " +
            "left join eth.entity_ref_lang erl on cr.id = erl.ref_id and erl.table_ref = 'NATIONAL_PROCEDURE_REF' and erl.lang_id = :lang " +
            "left join eth.lang l on erl.lang_id = l.id " +
            "where   " +
            "LOWER(cr.code) like %:value% " +
            "OR   " +
            "LOWER(erl.label) like  %:value%"
            ,nativeQuery = true)
    Page<NationalProcedureRefLangProjection> filterByCodeOrLabelProjection(String value, Long lang, Pageable pageable);

}
