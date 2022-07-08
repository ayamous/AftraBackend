package ma.itroad.aace.eth.coref.repository;

import ma.itroad.aace.eth.core.repository.BaseJpaRepository;
import ma.itroad.aace.eth.coref.model.entity.*;
import ma.itroad.aace.eth.coref.model.mapper.projections.TaxationRefLangProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource()
public interface TaxationRepository extends BaseJpaRepository<Taxation> {

	Taxation findByReference(String reference);

	Page<Taxation> findByReferenceIgnoreCaseContains(String reference, Pageable pageable);

	Optional<Taxation> findByReferenceAndCountryRefAndCustomsRegimRefAndUnitRefAndTaxRef(String reference,
			CountryRef countryRef, CustomsRegimRef customsRegimRef, UnitRef unitRef, TaxRef taxRef);
	@Query("select DISTINCT ta from Taxation ta  where  ta.countryRef in (:countries)  ")
	public Page<Taxation> findByFilterWithoutTarifBook(List<CountryRef> countries, Pageable pageable);

	/*@Query("select DISTINCT ta from Taxation ta  join ta.tarifBookRefs  t where t.reference  like %:tarifBookReference% "
			+ "AND  ta.countryRef.reference in (:countries)  ")
	Page<> findByFilter(String tarifBookReference, List<String> countries, Pageable pageable);

	 */


	/**
	@Query(value ="select  * from eth.taxation  p  " +
			"inner JOIN eth.TARIF_BOOK_REF_TAXATIONS_REF_JOIN  nt ON p.id = nt.TAXATIONS_REF_ID " +
			"inner JOIN eth.tarif_book_ref t ON nt.fk_tarif_book_ref_id = t.id and t.reference =:tarifBookReference " +
			" JOIN eth.country_ref c on c.reference in (:countries) and c.id = p.country_id  ", nativeQuery = true)
	 **/
	@Query("select DISTINCT tax from Taxation tax join tax.tarifBookRefs t" +
			" where ( :tarifBookReference is null or t.reference=:tarifBookReference ) " +
			" and (coalesce(:countries, null) is null or tax.countryRef.reference in (:countries)) ")
	Page<Taxation> findByFilter(String tarifBookReference,@Param("countries") List <String> countries, Pageable pageable);

	@Query(value = "select * from eth.taxation c inner JOIN eth.entity_ref_lang e on c.id = e.ref_id where e.table_ref = 'TAXATION_REF' and e.lang_id = :idLang order by c.created_on desc", nativeQuery = true)
	Page<Taxation> findAllByEntityRefAndLang(@Param("idLang") Long idLang, Pageable pageable);

	@Query("select DISTINCT ta from Taxation ta  join ta.tarifBookRefs t "
			+ " where ( :tarifBookReference is null or t.reference  like %:tarifBookReference% ) "
			+ "AND ( :countryReference is null or ta.countryRef.reference = :countryReference ) "
			+ "AND ( :customRegimeCode is null or ta.customsRegimRef.code = :customRegimeCode ) ")
	Page<Taxation> subportalProductInformationFinderFilter(String countryReference, String customRegimeCode,
			String tarifBookReference, Pageable pageable);

	@Query(value =" select DISTINCT * from eth.taxation c " +
			" inner join eth.entity_ref_lang erl " +
			"on (erl.table_ref = 'TAXATION_REF' " +
			"and  c.id= erl.ref_id " +
			"and (erl.label like %:value%  or c.reference like %:value%)" +
			"and erl.lang_id = :idLang )"
			,nativeQuery = true)
	Page<Taxation> filterByReferenceOrLabel(String value, Long idLang, Pageable pageable);

	@Query(value = "select  " +
			"cr.id as id, " +
			"cr.created_on as createdOn, " +
			"cr.updated_on as updatedOn, " +
			"cr.version_nbr as version_nbr, " +
			"cr.request_origin as requestOrigin, " +
			"cr.reference as reference, " +
			"cr.value as value, " +
			"cr.rate as rate, " +
			"erl.label as label, " +
			"erl.general_description as generalDescription, " +
			"ct.reference as countryRef, " +
			"l.code as lang, " +
			"crr.code as customsRegimRef, " +
			"ur.code as unitRef, " +
			"txr.code as taxRef " +
			"from eth.taxation cr " +
			"inner join eth.tax_ref tx on tx.id = cr.tax_ref_id " +
			"inner join eth.tax_ref txr on txr.id = cr. tax_ref_id " +
			"inner join eth.unit_ref ur on ur.id = cr.unit_ref_id " +
			"left join eth.country_ref ct on ct.id = cr.country_id  " +
			"left join eth.customs_regim_ref crr ON crr.id = cr.customs_regim_id " +
			"left join eth.entity_ref_lang erl on cr.id = erl.ref_id and erl.table_ref = 'TAXATION_REF' and erl.lang_id = :lang " +
			"left join eth.lang l on erl.lang_id = l.id " +
			"where   " +
			"LOWER(cr.reference) like %:value% " +
			"OR   " +
			"LOWER(erl.label) like  %:value%"
			,nativeQuery = true)
	Page<TaxationRefLangProjection> filterByReferenceOrLabelProjection(String value, Long lang, Pageable pageable);
}
