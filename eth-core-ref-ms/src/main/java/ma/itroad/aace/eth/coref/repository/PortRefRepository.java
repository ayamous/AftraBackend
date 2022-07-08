package ma.itroad.aace.eth.coref.repository;

import ma.itroad.aace.eth.core.repository.BaseJpaRepository;
import ma.itroad.aace.eth.coref.model.entity.CustomsRegimRef;
import ma.itroad.aace.eth.coref.model.entity.PortRef;
import ma.itroad.aace.eth.coref.model.mapper.projections.PortRefLangProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource()
public interface PortRefRepository extends BaseJpaRepository<PortRef> {

    PortRef findByCode(String code);

    Page<PortRef> findByCodeIgnoreCaseContains(String code, Pageable pageable);
    
	@Query(value = "select * from eth.port_ref p inner JOIN eth.entity_ref_lang e on p.id = e.ref_id where e.table_ref = 'PORT_REF' and e.lang_id = :idLang order by p.created_on desc", nativeQuery = true)
	Page<PortRef> findAllByEntityRefAndLang(@Param("idLang") Long idLang, Pageable pageable);

    @Query(value =" select DISTINCT * from eth.port_ref c " +
            "inner join eth.entity_ref_lang erl " +
            "on (erl.table_ref = 'PORT_REF' " +
            "and  c.id= erl.ref_id " +
            "and (erl.label like %:value%  or c.code like %:value%)" +
            "and erl.lang_id = :idLang )"
            ,nativeQuery = true)
    Page<PortRef> filterByCodeOrLabel(String value, Long idLang, Pageable pageable);

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
            "from eth.port_ref cr     " +
            "left join eth.entity_ref_lang erl on cr.id = erl.ref_id and erl.table_ref = 'PORT_REF' and erl.lang_id = :lang  " +
            "left join eth.lang l on erl.lang_id = l.id     " +
            "left join eth.country_ref ct on ct.id = cr.country_ref_id " +
            "where   " +
            "LOWER(cr.code) like %:value%   " +
            "OR   " +
            "LOWER(erl.label) like %:value%",nativeQuery = true)
    Page<PortRefLangProjection> filterByCodeOrLabelProjection(String value, Long lang, Pageable pageable);
}
