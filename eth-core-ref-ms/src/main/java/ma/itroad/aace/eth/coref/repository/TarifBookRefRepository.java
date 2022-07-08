package ma.itroad.aace.eth.coref.repository;

import ma.itroad.aace.eth.core.repository.BaseJpaRepository;
import ma.itroad.aace.eth.coref.model.entity.TarifBookRef;

import ma.itroad.aace.eth.coref.model.mapper.TarifBookAgreementVMProjection;
import ma.itroad.aace.eth.coref.model.mapper.TarifBookTechBarrierVMProjection;
import ma.itroad.aace.eth.coref.model.mapper.projections.TarifBookNationalProcedureVMProjection;
import ma.itroad.aace.eth.coref.model.mapper.projections.TarifBookTaxationVMProjection;
import ma.itroad.aace.eth.coref.model.mapper.projections.TariffBookRefLangProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource()
public interface TarifBookRefRepository extends BaseJpaRepository<TarifBookRef> {

    TarifBookRef findByReference(String reference);

    TarifBookRef findByHsCode(String reference);

    List<TarifBookRef> findByReferenceIgnoreCaseContains(String reference);




    @Query(value = "select * from eth.tarif_book_ref c inner JOIN eth.entity_ref_lang e on c.id = e.ref_id where e.table_ref = 'TARIFF_BOOK_REF' and e.lang_id = :idLang order by c.created_on desc", nativeQuery = true)
	Page<TarifBookRef> findAllByEntityRefAndLang(@Param("idLang") Long idLang, Pageable pageable);

    @Query(value =" select * from eth.tarif_book_ref c " +
            " inner join eth.entity_ref_lang erl " +
            "on (erl.table_ref = 'TARIFF_BOOK_REF' " +
            "and  c.id= erl.ref_id " +
            "and (erl.label like %:value%  or c.reference like %:value%) " +
            "and erl.lang_id = :idLang)"
            ,nativeQuery = true)
    Page<TarifBookRef> filterByReferenceOrLabel(String value,Long idLang, Pageable pageable);


    // Select two Entity have @ManyToMany relationship using Data Jpa Projection
    @Query(value = "select " +
            "tx.id as taxationId, " +
            "tx.reference as taxationReference, " +
            "tb.id as tarifBookId, " +
            "tb.reference as tarifBookReference " +
            "from eth.TARIF_BOOK_REF_TAXATIONS_REF_JOIN tbr " +
            "inner join eth.Taxation tx ON tx.id = tbr.taxations_ref_id " +
            "inner join eth.tarif_book_ref tb ON tb.id = tbr.fk_tarif_book_ref_id ",
            nativeQuery = true)
    Page<TarifBookTaxationVMProjection> getTarifBookTaxationData(Pageable pageable);


    // Select two Entity have @ManyToMany relationship using Data Jpa Projection
    @Query(value = "select " +
            "ag.id as techBarrierId, " +
            "ag.code as techBarrierReference, " +
            "tb.id as tarifBookId, " +
            "tb.reference as tarifBookReference " +
            "from eth.tech_barrier_ref_tariff_book_ref_join tbr " +
            "inner join eth.tech_barrier_ref ag ON ag.id = tbr.tech_barrier_ref_id " +
            "inner join eth.tarif_book_ref tb ON tb.id = tbr.TARIFF_BOOK_REF_ID ",
            nativeQuery = true)
    Page<TarifBookTechBarrierVMProjection> getTarifBookTechBarrierData(Pageable pageable);

    // Select two Entity have @ManyToMany relationship using Data Jpa Projection
    @Query(value = "select " +
            "ag.id as agreementId, " +
            "ag.code as agreementReference, " +
            "tb.id as tarifBookId, " +
            "tb.reference as tarifBookReference " +
            "from eth.TARIF_BOOK_REF_AGREEMENTS_REF_JOIN tbr " +
            "inner join eth.agreement ag ON ag.id = tbr.AGREEMENTS_REF_ID " +
            "inner join eth.tarif_book_ref tb ON tb.id = tbr.FK_TARIF_BOOK_REF_ID ",
            nativeQuery = true)
    Page<TarifBookAgreementVMProjection> getTarifBookAgreementData(Pageable pageable);

    // Select two Entity have @ManyToMany relationship using Data Jpa Projection
    @Query(value = "select " +
            "np.id as nationalProcedureRefId, " +
            "np.code as nationalProcedureCode, " +
            "tb.id as tarifBookId, " +
            "tb.reference as tarifBookReference " +
            "from eth.TARIF_BOOK_REF_National_Procedure_REF_JOIN tbr " +
            "inner join eth.national_procedure_ref np ON np.id = tbr.NATIONAL_PROCEDURE_REF_ID " +
            "inner join eth.tarif_book_ref tb ON tb.id = tbr.FK_TARIF_BOOK_REF_ID ",
            nativeQuery = true)
    Page<TarifBookNationalProcedureVMProjection> getListOfTarifBookNationalProcedure(Pageable pageable);



    @Query(value = "select     " +
            "cr.id as id,     " +
            "cr.created_on as createdOn,     " +
            "cr.updated_on as updatedOn,     " +
            "cr.hs_code as hsCode ,   " +
            "cr.product_year as productYear ,   " +
            "cr.reference as reference,     " +
            "chr.code as chapterRef,   " +
            "tbp.reference as parent,   " +
            "ur.code as unitRef,   " +
            "erl.label as label,     " +
            "erl.description as description,     " +
            "l.code as lang   " +
            "from eth.tarif_book_ref cr    " +
            "left join eth.chapter_ref chr on chr.id = cr.chapter_ref_id   " +
            "left join eth.unit_ref ur on ur.id = cr.unit_ref_id     " +
            "left join eth.tarif_book_ref tbp on tbp.id = cr.parent_id    " +
            "left join eth.entity_ref_lang erl on cr.id = erl.ref_id and erl.table_ref = 'TARIFF_BOOK_REF' and erl.lang_id = :lang " +
            "left join eth.lang l on erl.lang_id = l.id " +
            "where   " +
            "LOWER(cr.reference) like %:value% " +
            "OR   " +
            "LOWER(erl.label) like  %:value%",nativeQuery = true)
    Page<TariffBookRefLangProjection> filterByReferenceOrLabelProjecton(String value, Long lang, Pageable pageable);


    @Query(value = "select " +
            "tx.id as taxationId, " +
            "tx.reference as taxationReference, " +
            "tb.id as tarifBookId, " +
            "tb.reference as tarifBookReference " +
            "from eth.TARIF_BOOK_REF_TAXATIONS_REF_JOIN tbr " +
            "inner join eth.Taxation tx ON tx.id = tbr.taxations_ref_id " +
            "inner join eth.tarif_book_ref tb ON tb.id = tbr.fk_tarif_book_ref_id " +
            "WHERE tb.reference like %:reference% ", nativeQuery = true)
    Page<TarifBookTaxationVMProjection> findByReferenceProjection(String reference,Pageable pageable);

}
