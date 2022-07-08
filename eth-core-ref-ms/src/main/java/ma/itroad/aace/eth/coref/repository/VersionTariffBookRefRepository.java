package ma.itroad.aace.eth.coref.repository;

import ma.itroad.aace.eth.core.repository.BaseJpaRepository;
import ma.itroad.aace.eth.coref.model.entity.CountryRef;
import ma.itroad.aace.eth.coref.model.entity.TarifBookRef;
import ma.itroad.aace.eth.coref.model.entity.VersionRef;
import ma.itroad.aace.eth.coref.model.entity.VersionTariffBookRef;
import ma.itroad.aace.eth.coref.model.mapper.projections.AgreementTypeLangProjection;
import ma.itroad.aace.eth.coref.model.mapper.projections.TarifBookTaxationVMProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource()
public interface VersionTariffBookRefRepository extends BaseJpaRepository<VersionTariffBookRef> {

	@Query(value = "select * from eth.version_tariff_book_ref c inner JOIN eth.entity_ref_lang e on c.id = e.ref_id where e.table_ref = 'VERSION_TARIFF_BOOK' and e.lang_id = :idLang order by c.created_on desc", nativeQuery = true)
	Page<VersionTariffBookRef> findAllByEntityRefAndLang(@Param("idLang") Long idLang, Pageable pageable);

	VersionTariffBookRef findByTarifBookRefAndAndCountryRefAndVersionRef(TarifBookRef tarifBookRef,
			CountryRef countryRef, VersionRef versionRef);

	Page<VersionTariffBookRef> findByTarifBookRef_ReferenceIgnoreCaseContains(String reference, Pageable pageable);


}
