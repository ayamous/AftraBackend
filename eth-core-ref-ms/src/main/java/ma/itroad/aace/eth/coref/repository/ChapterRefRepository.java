package ma.itroad.aace.eth.coref.repository;

import ma.itroad.aace.eth.core.repository.BaseJpaRepository;
import ma.itroad.aace.eth.coref.model.entity.ChapterRef;
import ma.itroad.aace.eth.coref.model.mapper.projections.ChapterRefLangProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource()
public interface ChapterRefRepository extends BaseJpaRepository<ChapterRef> {
	 ChapterRef findByCode(String code);

	Page<ChapterRef> findByCodeIgnoreCaseContains(String code, Pageable pageable);

	@Query(value = "select * from eth.chapter_ref c inner JOIN eth.entity_ref_lang e on c.id = e.ref_id where e.table_ref = 'CHAPTER_REF' and e.lang_id = :idLang order by c.created_on desc", nativeQuery = true)
	Page<ChapterRef> findAllByEntityRefAndLang(@Param("idLang") Long idLang, Pageable pageable);

	@Query(value =" select * from eth.chapter_ref c " +
			" inner join eth.entity_ref_lang erl " +
			"on (erl.table_ref = 'CHAPTER_REF' " +
			"and  c.id= erl.ref_id " +
			"and (erl.label like %:value%  or c.code like %:value%) " +
			"and erl.lang_id = :idLang)"
			,nativeQuery = true)
	Page<ChapterRef> filterByCodeOrLabel(String value, Long idLang, Pageable pageable);

	@Query(value ="select   " +
			"distinct on (cr.code) " +
			"cr.id as id,   " +
			"cr.created_on as createdOn,   " +
			"cr.updated_on as updatedOn,  " +
			"sr.code as sectionRef, " +
			"cr.code as code,   " +
			"erl.label as label,   " +
			"erl.description as description,   " +
			"l.code as lang   " +
			"from eth.chapter_ref cr   " +
			"inner join eth.section_ref sr on sr.id = cr.section_ref_id  " +
			"left join eth.entity_ref_lang erl on cr.id = erl.ref_id and erl.table_ref = 'CHAPTER_REF' and erl.lang_id =  :lang " +
			"left join eth.lang l on erl.lang_id = l.id   " +
			"where   " +
			"LOWER(cr.code) like %:value%   " +
			"OR   " +
			"LOWER(erl.label) like %:value%"
			,nativeQuery = true)
	Page<ChapterRefLangProjection> filterByCodeOrLabelProjection(String value, Long lang, Pageable pageable);
}
