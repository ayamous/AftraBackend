package ma.itroad.aace.eth.coref.repository;

import ma.itroad.aace.eth.core.repository.BaseJpaRepository;
import ma.itroad.aace.eth.coref.model.entity.CustomsOfficeRef;
import ma.itroad.aace.eth.coref.model.mapper.projections.CustomsOfficeRefLangProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource()
public interface CustomsOfficeRefRepository extends BaseJpaRepository<CustomsOfficeRef> {

    CustomsOfficeRef findByCode(String code);

    Page<CustomsOfficeRef> findByCodeIgnoreCaseContains(String code, Pageable pageable);
    
	@Query(value = "select * from eth.customs_office_ref c inner JOIN eth.entity_ref_lang e on c.id = e.ref_id where e.table_ref = 'CUSTOMSOFFICE_REF' and e.lang_id = :idLang order by c.created_on desc", nativeQuery = true)
	Page<CustomsOfficeRef> findAllByEntityRefAndLang(@Param("idLang") Long idLang, Pageable pageable);

    @Query(value =" select * from eth.customs_office_ref c " +
            " inner join eth.entity_ref_lang erl " +
            "on (erl.table_ref = 'CUSTOMSOFFICE_REF' " +
            "and  c.id= erl.ref_id " +
            "and (erl.label like %:value%  or c.code like %:value%) " +
            "and erl.lang_id = :idLang)"
            ,nativeQuery = true)
    Page<CustomsOfficeRef> filterByCodeOrLabel(String value, Long idLang, Pageable pageable);

    @Query("select cstRef from CustomsOfficeRef cstRef where cstRef.code like %:value%")
    Page<CustomsOfficeRef> findBycode(String value, Pageable pageable);


    @Query(value = "select     " +
            "distinct on (cr.code)   " +
            "cr.id as id,     " +
            "cr.created_on as createdOn,     " +
            "cr.updated_on as updatedOn,     " +
            "cr.code as code,  " +
            "erl.label as label,     " +
            "erl.description as description,     " +
            "ct.reference as countryRef, "+
            "l.code as lang     " +
            "from eth.customs_office_ref cr     " +
            "left join eth.entity_ref_lang erl on cr.id = erl.ref_id and erl.table_ref = 'CUSTOMSOFFICE_REF' and erl.lang_id = :lang  " +
            "left join eth.lang l on erl.lang_id = l.id     " +
            "left join eth.country_ref ct on ct.id = cr.country_ref_id " +
            "where   " +
            "LOWER(cr.code) like %:value%   " +
            "OR   " +
            "LOWER(erl.label) like %:value%",nativeQuery = true)
    Page<CustomsOfficeRefLangProjection> filterByCodeOrLabelProjection(String value, Long lang, Pageable pageable);
}
