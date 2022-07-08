package ma.itroad.aace.eth.coref.repository;

import ma.itroad.aace.eth.coref.model.entity.ESafeDocumentPermission;
import ma.itroad.aace.eth.coref.model.entity.ESafeDocumentPermissionId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ESafeDocumentPermissionRepository extends JpaRepository<ESafeDocumentPermission, ESafeDocumentPermissionId> {
    List<ESafeDocumentPermission> findAllByDocument_Id(Long documentId);
}
