package ma.itroad.aace.eth.coref.repository;

import ma.itroad.aace.eth.core.model.enums.TableRef;
import ma.itroad.aace.eth.core.repository.BaseJpaRepository;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.entity.Lang;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(exported=false)
public interface EntityRefLangRepository extends BaseJpaRepository<EntityRefLang>{

    EntityRefLang findByTableRefAndLang_IdAndRefId (TableRef tableRef, Long langId, Long refId);

    @Query("select m from EntityRefLang m")
    EntityRefLang enEfAndLang();

    List<EntityRefLang> findByTableRefAndRefId(TableRef tableRef,  Long refId);

   // List<EntityRefLang> findByRefId(Long refId);

   // List<EntityRefLang> findByTableRefAndRefId(String tableRef,  Long refId);
}


