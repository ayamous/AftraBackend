package ma.itroad.aace.eth.coref.repository;
 
import ma.itroad.aace.eth.core.repository.BaseJpaRepository;
import ma.itroad.aace.eth.coref.model.entity.ESafeDocument;
import ma.itroad.aace.eth.coref.model.enums.ExchangeChannelType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@RepositoryRestResource()
public interface ESafeDocumentRepository extends BaseJpaRepository<ESafeDocument> {
    List<ESafeDocument> findAllByIsFolder(Boolean isFolder) ;
    ESafeDocument findByEdmStorld(Long edmStorld);
    Page <ESafeDocument> findByParentEdmStorldAndOwner_Id(Long  edmStorld ,long id , Pageable pageable) ;
    Set<ESafeDocument> findByParentEdmStorld(Long  edmStorld) ;
    Set<ESafeDocument> findByParentNull() ;
    Set<ESafeDocument> findAllByChannelType(ExchangeChannelType channelType) ;
    Page<ESafeDocument> findByParentNullAndOwner_Id(long id , Pageable pageable) ;
    Boolean deleteByEdmStorld(Long edmStorId) ;
}
