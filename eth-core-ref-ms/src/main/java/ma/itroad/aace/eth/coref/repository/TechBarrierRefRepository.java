package ma.itroad.aace.eth.coref.repository;

import ma.itroad.aace.eth.core.repository.BaseJpaRepository;
import ma.itroad.aace.eth.coref.model.entity.*;
import ma.itroad.aace.eth.coref.model.mapper.projections.TechnicalBarrierRefLangProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource()
public interface TechBarrierRefRepository extends BaseJpaRepository<TechBarrierRef> {

    TechBarrierRef findByCode(String code);

    Page<TechBarrierRef> findByCodeIgnoreCaseContains(String code, Pageable pageable);


    @Query("select DISTINCT t from TechBarrierRef t  " +
            " where (:code is null or  t.code like %:code% ) " +
            " AND (:countryReference is null or t.countryRef.reference  = :countryReference )   " +
            " AND (:organizationReference is null or t.organization.reference = :organizationReference ) " +
            " AND (:customRegimCode is null or t.customsRegimRef.code= :customRegimCode ) ")
    Page<TechBarrierRef> techBarFilter2(String countryReference, String customRegimCode, String code, String organizationReference, Pageable pageable);



    @Query("select DISTINCT t from TechBarrierRef t " +
            " where (:code is null or  t.code like %:code% ) " +
            " AND  (coalesce(:countries, null) is null or t.countryRef.reference in (:countries))    " +
            " AND (:organizationReference is null or t.organization.reference = :organizationReference ) ")
    public Page<TechBarrierRef> findTechBarrsWithCountries(List<String> countries, String code, String organizationReference, Pageable pageable);

    @Query("select DISTINCT ta from TechBarrierRef ta  join ta.tariffBookRefs  t" +
            " where ( :tarifBookReference is null or t.reference  like %:tarifBookReference% )" +
            " AND ( :countryReference is null or ta.countryRef.reference = :countryReference ) " +
            " AND ( :customRegimeCode is null or ta.customsRegimRef.code = :customRegimeCode ) ")
    Page<TechBarrierRef>  subportalProductInformationFinderFilter(String countryReference, String customRegimeCode, String tarifBookReference, Pageable pageable);

    @Query(value = "select * from eth.tech_barrier_ref c inner JOIN eth.entity_ref_lang e on c.id = e.ref_id where e.table_ref = 'TECH_BARRIER_REF' and e.lang_id = :idLang order by c.created_on desc", nativeQuery = true)
	Page<TechBarrierRef> findAllByEntityRefAndLang(@Param("idLang") Long idLang, Pageable pageable);

    @Query("select DISTINCT t from TechBarrierRef t join t.tariffBookRefs ct where (upper(ct.reference) like %:reference% or upper(t.code) like %:reference%) ")
    Page<TechBarrierRef> findTechBarrierTarifBook(String reference, Pageable pageable);


    @Query("select DISTINCT t from TechBarrierRef t where ( t.countryRef IN  (:countryReferences)) " )
    public Page<TechBarrierRef> findByCountryRefInList(List<CountryRef> countryReferences, Pageable pageable);

    @Query("select DISTINCT tech from TechBarrierRef tech join tech.tariffBookRefs t" +
            " where ( :tarifBookReference is null or t.reference=:tarifBookReference ) " +
            " and (coalesce(:countries, null) is null or tech.countryRef.reference in (:countries)) ")
    Page<TechBarrierRef> techBarFilter(String tarifBookReference, @Param("countries") List <String> countries, Pageable pageable);

    @Query(value =" select DISTINCT * from eth.tech_barrier_ref c " +
            " inner join eth.entity_ref_lang erl " +
            "on (erl.table_ref = 'TECH_BARRIER_REF' " +
            "and  c.id= erl.ref_id " +
            "and (erl.label like %:value%  or c.code like %:value%)" +
            "and erl.lang_id = :idLang )"
            ,nativeQuery = true)
    Page<TechBarrierRef> filterByCodeOrLabel(String value, Long idLang, Pageable pageable);


    @Query(value = "select  " +
            "cr.id as id,   " +
            "cr.created_on as createdOn,   " +
            "cr.updated_on as updatedOn,   " +
            "cr.created_by as createdBy,   " +
            "cr.updated_by as updatedBy,   " +
            "cr.version_nbr as version_nbr,   " +
            "cr.request_origin as requestOrigin,   " +
            "cr.code as code,   " +
            "erl.label as label,   " +
            "erl.general_description as generalDescription,   " +
            "ct.reference as countryRef,   " +
            "l.code as lang,  " +
            "crr.code as customsRegimRef  " +
            "from eth.tech_barrier_ref cr   " +
            "left join eth.organization og on og.id = cr.organization_id   " +
            "left join eth.country_ref ct on ct.id = cr.country_ref_id    " +
            "left join eth.customs_regim_ref crr ON crr.id = cr.custom_regim_ref_id " +
            "left join eth.entity_ref_lang erl on cr.id = erl.ref_id and erl.table_ref = 'TECH_BARRIER_REF' and erl.lang_id = :lang " +
            "left join eth.lang l on erl.lang_id = l.id " +
            "where   " +
            "LOWER(cr.code) like %:value% " +
            "OR   " +
            "LOWER(erl.label) like  %:value%",nativeQuery = true)
    Page<TechnicalBarrierRefLangProjection> filterTechByCodeOrLabelProjection(String value, Long lang, Pageable pageable);
}

