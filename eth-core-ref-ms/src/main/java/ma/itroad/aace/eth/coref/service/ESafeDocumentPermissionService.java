package ma.itroad.aace.eth.coref.service;

import ma.itroad.aace.eth.coref.model.bean.ESafeDocumentPermissionRequest;
import ma.itroad.aace.eth.coref.model.entity.ESafeDocument;
import ma.itroad.aace.eth.coref.model.enums.ESafeDocumentPermissionType;

public interface ESafeDocumentPermissionService {
    void addPermission(ESafeDocument eSafeDocument, ESafeDocumentPermissionRequest request, ESafeDocumentPermissionType permissionType);
    void addPermissionForAll(ESafeDocument eSafeDocument, ESafeDocumentPermissionType permissionType);
}
