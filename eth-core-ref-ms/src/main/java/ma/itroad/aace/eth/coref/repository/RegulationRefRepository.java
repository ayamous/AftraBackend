package ma.itroad.aace.eth.coref.repository;

import ma.itroad.aace.eth.core.repository.BaseJpaRepository;
import ma.itroad.aace.eth.coref.model.entity.CountryRef;
import ma.itroad.aace.eth.coref.model.entity.RegulationRef;
import ma.itroad.aace.eth.coref.model.entity.SanitaryPhytosanitaryMeasuresRef;
import ma.itroad.aace.eth.coref.model.mapper.RegulationTariffBookRefVMProjection;
import ma.itroad.aace.eth.coref.model.mapper.projections.RegulationRefLangProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource()
public interface RegulationRefRepository extends BaseJpaRepository<RegulationRef> {

    RegulationRef findByCode(String code);

    @Query("select DISTINCT t from RegulationRef t join t.tarifBookRefs ct where (upper(ct.reference) like %:reference% or upper(t.code) like %:reference%) ")
    Page<RegulationRef> findRegulationTarifBookJoin(String reference, Pageable pageable);

    @Query(
            "select r from RegulationRef r where (:code is null  or r.code=:code)" +
                    "and (coalesce(:countries, null) is null or r.countryRef.reference in (:countries))"
    )
    Page<RegulationRef> findRegulationWithCountries(String code, List<String> countries, Pageable pageable);

    Page<RegulationRef> findByCodeIgnoreCaseContains(String code,Pageable pageable);

    @Query(value =" select DISTINCT * from eth.regulation_ref c " +
            " inner join eth.entity_ref_lang erl " +
            "on (erl.table_ref = 'REGULATION_REF' " +
            "and  c.id= erl.ref_id " +
            "and (erl.label like %:value%  or c.code like %:value%)" +
            "and erl.lang_id = :idLang )"
            ,nativeQuery = true)
    Page<RegulationRef> filterByCodeOrLabel(String value, Long idLang, Pageable pageable);


    @Query("select DISTINCT reg from RegulationRef reg join reg.customsRegimRefs c " +
            "where (:customRegimeCode is null or c.code = :customRegimeCode )" +
            " AND (:code is null or  reg.code like %:code% ) " +
            " AND (:countryReference is null or reg.countryRef.reference = :countryReference )" )
    Page<RegulationRef> findMspsWithCustomRegime(String countryReference, String code, String customRegimeCode, Pageable pageable);


    @Query("select DISTINCT reg from RegulationRef reg  " +
            " where (:code is null or  reg.code like %:code% ) " +
            " AND (:countryReference is null or reg.countryRef.reference = :countryReference )" )
    Page<RegulationRef> findMspsWithoutCustomRegime(String countryReference, String code, Pageable pageable);

    @Query("select DISTINCT reg from RegulationRef reg join reg.tarifBookRefs t" +
            " where ( :tarifBookReference is null or t.reference=:tarifBookReference ) " +
            " and (coalesce(:countries, null) is null or reg.countryRef.reference in (:countries)) ")
    Page<RegulationRef> findByFilter(String tarifBookReference, @Param("countries") List <String> countries, Pageable pageable);


    @Query("select DISTINCT reg from RegulationRef reg  where  reg.countryRef in (:countries) ")
    Page<RegulationRef> findByFilterWithoutTarifBook(List<CountryRef> countries, Pageable pageable);

    @Query("select DISTINCT reg from RegulationRef reg  " +
            " where (:code is null or  reg.code like %:code% ) " +
            " AND ( coalesce(:countries, null) is null or reg.countryRef.reference in (:countries) )")
    Page<RegulationRef> findMspsWithCountries(List<String> countries, String code, Pageable pageable);

    @Query("select DISTINCT reg from RegulationRef reg join reg.customsRegimRefs c join reg.tarifBookRefs t " +
            " where ( c.code = :customRegimeCode or :customRegimeCode is null )   " +
            " and ( :tarifBookReference is null or  t.reference like %:tarifBookReference% )   " +
            " and ( :countryReference is null or reg.countryRef.reference = :countryReference )   " )
    Page<RegulationRef> subportalProductInformationFinderFilter(String countryReference, String customRegimeCode,String tarifBookReference, Pageable pageable);


    @Query(value = "select " +
            "rg.id as regulationId, " +
            "rg.code as regulationReference, " +
            "tb.id as tarifBookId, " +
            "tb.reference as tarifBookReference " +
            "from eth.REGULATION_TARIFF_BOOK_JOIN rtb " +
            "inner join eth.regulation_ref rg ON rg.id = rtb.regulation_REF_ID " +
            "inner join eth.tarif_book_ref tb ON tb.id = rtb.tarif_book_REF_ID" ,
            nativeQuery = true)
    Page<RegulationTariffBookRefVMProjection> getListOfRegulationTariffBookRefVMs(Pageable pageable);


    @Query(value = "select     " +
            "distinct on (cr.code)   " +
            "cr.id as id,     " +
            "cr.created_on as createdOn,     " +
            "cr.updated_on as updatedOn,     " +
            "cr.code as code, " +
            "ct.reference as countryRef, " +
            "og.reference as organization, " +
            "erl.label as label,     " +
            "erl.general_description as generalDescription,     " +
            "l.code as lang     " +
            "from eth.Regulation_Ref cr     " +
            "left join eth.entity_ref_lang erl on cr.id = erl.ref_id and erl.table_ref = 'CUSTOMSOFFICE_REF' and erl.lang_id = :lang   " +
            "left join eth.lang l on erl.lang_id = l.id    " +
            "left join eth.country_ref ct on ct.id = cr.country_ref_id " +
            "left join eth.organization og on og.id = cr.organization_id " +
            "where   " +
            "LOWER(cr.code) like %:value% " +
            "OR   " +
            "LOWER(erl.label) like  %:value%",nativeQuery = true)
    Page<RegulationRefLangProjection> filterByCodeOrLabelProjection(String value, Long lang, Pageable pageable);
}
