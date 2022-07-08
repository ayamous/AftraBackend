package ma.itroad.aace.eth.coref.repository;

import ma.itroad.aace.eth.core.repository.BaseJpaRepository;
import ma.itroad.aace.eth.coref.model.entity.SanitaryPhytosanitaryMeasuresRef;
import ma.itroad.aace.eth.coref.model.mapper.projections.MSPTariffBookRefVMProjection;
import ma.itroad.aace.eth.coref.model.mapper.projections.SanitaryPhytosanitaryMeasuresRefLangProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource()
public interface SanitaryPhytosanitaryMeasuresRefRepository extends BaseJpaRepository<SanitaryPhytosanitaryMeasuresRef> {



    /**@Query(value ="select  * from eth.sanitary_phytosanitary_measures_ref  p  " +
            "inner JOIN eth.msp_tariff_book_join  nt ON p.id = nt.sanitary_phytosanitary_measures_ref_id " +
            "inner JOIN eth.tarif_book_ref t ON nt.tarif_book_ref_id = t.id and t.reference =:tarifBookReference " +
            " JOIN eth.country_ref c on c.reference in (:countries) and c.id = p.country_ref_id  ", nativeQuery = true)**/

    @Query("select DISTINCT msp from SanitaryPhytosanitaryMeasuresRef msp join msp.tarifBookRefs t" +
            " where ( :tarifBookReference is null or t.reference=:tarifBookReference ) " +
            " and (coalesce(:countries, null) is null or msp.countryRef.reference in (:countries)) ")
    Page<SanitaryPhytosanitaryMeasuresRef> findByFilter(String tarifBookReference,@Param("countries") List <String> countries, Pageable pageable);

    @Query("select DISTINCT msp from SanitaryPhytosanitaryMeasuresRef msp  where  msp.countryRef.reference in (:countries)  ")
    public Page<SanitaryPhytosanitaryMeasuresRef> findByFilterWithoutTarifBook(List<String> countries, Pageable pageable);

    @Query("select DISTINCT msp from SanitaryPhytosanitaryMeasuresRef msp join msp.customsRegimRefs c " +
            "where c.code = :customRegimeCode  " +
            " AND (:code is null or  msp.code like %:code% ) " +
            " AND (:countryReference is null or msp.countryRef.reference = :countryReference )   " +
            " AND (:organizationReference is null or msp.organization.reference = :organizationReference ) ")
    public Page<SanitaryPhytosanitaryMeasuresRef> findMspsWithCustomRegime(String countryReference, String code, String organizationReference, String customRegimeCode, Pageable pageable);

    @Query("select DISTINCT msp from SanitaryPhytosanitaryMeasuresRef msp " +
            "  join msp.customsRegimRefs c join msp.tarifBookRefs t  " +
            "  where (c.code = :customRegimeCode or :customRegimeCode is null)   " +
            "  and (:tarifBookReference is null or  t.reference like %:tarifBookReference% )   " +
            " and (:countryReference is null or msp.countryRef.reference = :countryReference )   " )
    public Page<SanitaryPhytosanitaryMeasuresRef> subportalProductInformationFinderFilter(String countryReference, String customRegimeCode,String tarifBookReference, Pageable pageable);

    @Query("select DISTINCT msp from SanitaryPhytosanitaryMeasuresRef msp  " +
            " where (:code is null or  msp.code like %:code% ) " +
            " AND (:countryReference is null or msp.countryRef.reference = :countryReference )   " +
            " AND (:organizationReference is null or msp.organization.reference = :organizationReference ) ")
    public Page<SanitaryPhytosanitaryMeasuresRef> findMspsWithoutCustomRegime(String countryReference, String code, String organizationReference, Pageable pageable);

    @Query("select DISTINCT msp from SanitaryPhytosanitaryMeasuresRef msp  " +
            " where (:code is null or  msp.code like %:code% ) " +
            " AND  (coalesce(:countries, null) is null or msp.countryRef.reference in (:countries)) "+
            " AND (:organizationReference is null or msp.organization.reference = :organizationReference) ")
    public Page<SanitaryPhytosanitaryMeasuresRef> findMspsWithCountries(List<String> countries, String code, String organizationReference, Pageable pageable);

    Page<SanitaryPhytosanitaryMeasuresRef> findByCodeIgnoreCaseContains(String code, Pageable pageable);

    public SanitaryPhytosanitaryMeasuresRef findByCode(String code);

    @Query("select DISTINCT s from SanitaryPhytosanitaryMeasuresRef s join s.tarifBookRefs t " +
            "where (upper(s.code)  like %:reference% or upper(t.reference) like %:reference%) ")

    Page<SanitaryPhytosanitaryMeasuresRef> findMspTarifBookJoin(String reference, Pageable pageable);

    @Query(value = "select * from eth.sanitary_phytosanitary_measures_ref c inner JOIN eth.entity_ref_lang e on c.id = e.ref_id where e.table_ref = 'SANITARY_PHYTOSANITARY_MEASURES_REF' and e.lang_id = :idLang order by c.created_on desc", nativeQuery = true)
    Page<SanitaryPhytosanitaryMeasuresRef> findAllByEntityRefAndLang(@Param("idLang") Long idLang, Pageable pageable);

    @Query(value =" select DISTINCT * from eth.sanitary_phytosanitary_measures_ref c " +
            " inner join eth.entity_ref_lang erl " +
            "on (erl.table_ref = 'SANITARY_PHYTOSANITARY_MEASURES_REF' " +
            "and  c.id= erl.ref_id " +
            "and (erl.label like %:value%  or c.code like %:value%)" +
            "and erl.lang_id = :idLang )"
            ,nativeQuery = true)
    Page<SanitaryPhytosanitaryMeasuresRef> filterByCodeOrLabel(String value, Long idLang, Pageable pageable);

    // Select two Entity have @ManyToMany relationship using Data Jpa Projection
    @Query(value = "select " +
            "spm.id as mspId, " +
            "spm.code as mspReference, " +
            "tb.id as tarifBookId, " +
            "tb.reference as tarifBookReference " +
            "from eth.MSP_TARIFF_BOOK_JOIN mtbj " +
            "inner join eth.sanitary_phytosanitary_measures_ref spm ON spm.id = mtbj.sanitary_phytosanitary_measures_REF_ID " +
            "inner join eth.tarif_book_ref tb ON tb.id = mtbj.tarif_book_REF_ID",
            nativeQuery = true)
    Page<MSPTariffBookRefVMProjection> getListOfMSPTariffBookRefVMs(Pageable pageable);



    @Query(value = "select    " +
            "cr.id as id,    " +
            "cr.created_on as createdOn,    " +
            "cr.updated_on as updatedOn,    " +
            "cr.created_by as createdBy,    " +
            "cr.updated_by as updatedBy,    " +
            "cr.version_nbr as version_nbr,    " +
            "cr.request_origin as requestOrigin,    " +
            "cr.code as code,    " +
            "erl.label as label,    " +
            "erl.general_description as generalDescription,     " +
            "ct.reference as countryRef,   " +
            "og.reference as organization,  " +
            "l.code as lang  " +
            "from eth.sanitary_phytosanitary_measures_ref cr   " +
            "left join eth.organization og on og.id = cr.organization_id  " +
            "left join eth.country_ref ct on ct.id = cr.country_ref_id  " +
            "left join eth.entity_ref_lang erl on cr.id = erl.ref_id and erl.table_ref = 'SANITARY_PHYTOSANITARY_MEASURES_REF' and erl.lang_id = :lang " +
            "left join eth.lang l on erl.lang_id = l.id " +
            "where   " +
            "LOWER(cr.code) like %:value% " +
            "OR   " +
            "LOWER(erl.label) like  %:value%",nativeQuery = true)
    Page<SanitaryPhytosanitaryMeasuresRefLangProjection> filterByCodeOrLabelProjection(String value, Long lang, Pageable pageable);
}