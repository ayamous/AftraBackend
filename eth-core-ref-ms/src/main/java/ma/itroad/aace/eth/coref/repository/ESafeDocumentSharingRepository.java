package ma.itroad.aace.eth.coref.repository;

import ma.itroad.aace.eth.core.repository.BaseJpaRepository;
import ma.itroad.aace.eth.coref.model.entity.ESafeDocumentSharing;

public interface ESafeDocumentSharingRepository extends BaseJpaRepository<ESafeDocumentSharing> {
    ESafeDocumentSharing findByDocument_IdAndUserAccount_Id(Long documentId, Long userAccountId);
}
