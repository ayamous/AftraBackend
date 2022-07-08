package ma.itroad.aace.eth.coref.repository;

import ma.itroad.aace.eth.core.repository.BaseJpaRepository;
import ma.itroad.aace.eth.coref.model.entity.VersionRef;
import ma.itroad.aace.eth.coref.model.mapper.projections.VersionRefEntityRefLangProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource()
public interface VersionRefRepository extends BaseJpaRepository<VersionRef> {

    VersionRef findByVersion(String version);

    Page<VersionRef> findByVersionIgnoreCaseContains(String version, Pageable pageable);

    @Query(value =" select DISTINCT * from eth.version_ref c " +
            " inner join eth.entity_ref_lang erl " +
            "on (erl.table_ref = 'VERSION_REF' " +
            "and  c.id= erl.ref_id " +
            "and (erl.label like %:value%  or c.version like %:value%)" +
            "and erl.lang_id = :idLang )"
            ,nativeQuery = true)
    Page<VersionRef> filterByVersionOrLabel(String value, Long idLang, Pageable pageable);


    @Query(value = "select     " +
            "distinct on (cr.id)   " +
            "cr.id as id,     " +
            "cr.created_on as createdOn,     " +
            "cr.updated_on as updatedOn,     " +
            "cr.applicated_on as applicatedOn, " +
            "cr.status_version as statusVersion, " +
            "cr.enabled as enabled, " +
            "cr.version as version, " +
            "cr.validated_on as validatedOn, " +
            "cr.archived_on as archivedOn, " +
            "erl.label as label,     " +
            "erl.description as description,     " +
            "l.code as lang     " +
            "from eth.version_ref cr     " +
            "left join eth.entity_ref_lang erl on cr.id = erl.ref_id and erl.table_ref = 'VERSION_REF' and erl.lang_id = :lang   " +
            "left join eth.lang l on erl.lang_id = l.id     " +
            "where   " +
            "LOWER(cr.version) like %:value%   " +
            "OR   " +
            "LOWER(erl.label) like %:value%",
            nativeQuery = true
    )
    Page<VersionRefEntityRefLangProjection> filterByVersionOrLabelProjection(String value, Long lang, Pageable pageable);
}
