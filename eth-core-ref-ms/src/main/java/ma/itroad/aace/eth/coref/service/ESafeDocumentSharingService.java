package ma.itroad.aace.eth.coref.service;

import ma.itroad.aace.eth.coref.model.bean.ESafeDocumentSharingRequest;
import ma.itroad.aace.eth.coref.model.bean.ESafeDocumentSharingResponse;

public interface ESafeDocumentSharingService {
    ESafeDocumentSharingResponse share(ESafeDocumentSharingRequest request);
}
