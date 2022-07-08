package ma.itroad.aace.eth.coref.service;

import ma.itroad.aace.eth.core.rsql.service.IRsqlService;
import ma.itroad.aace.eth.core.service.IBaseService;
import ma.itroad.aace.eth.coref.model.bean.ESafeDocumentBean;
import ma.itroad.aace.eth.coref.model.entity.ESafeDocument;
import ma.itroad.aace.eth.coref.model.enums.ExchangeChannelType;
import ma.itroad.aace.eth.coref.model.view.ESafeDocumentVM;
import ma.itroad.aace.eth.coref.model.view.EsafeDocumentFilterPayload;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

public interface IESafeDocumentService extends
        IBaseService<ESafeDocument, ESafeDocumentBean>,
        IRsqlService<ESafeDocument, ESafeDocumentBean> {
    ESafeDocumentBean save(MultipartFile file, ESafeDocumentVM eSafeDocumentVM);

    ESafeDocumentBean newFolder(ESafeDocumentVM eSafeDocumentVM);

    ByteArrayResource download(Long edmStorld);

    Set<ESafeDocumentVM> findESafeDocumentsByParentEdmStoreId(Long parentEdemStoreId);

    Page<ESafeDocumentBean> findDetailedESafeDocumentsByParentEdmStoreId(Long parentEdemStoreId , Pageable pageable);

    Set<ESafeDocumentVM> findAllESafeDocumentsByParentEdmStoreId(Long parentEdemStoreId);

    List<ESafeDocumentBean> findAllESafeDocumentBeansByParentEdmStoreId(Long parentEdemStoreId);

    void modify(ESafeDocumentVM eSafeDocumentVM);

    void modifyFile(MultipartFile file, ESafeDocumentVM eSafeDocumentVM);

    void deleteESafeDocumentsByEdmStoreId(Long edemStoreId);

    Page<ESafeDocumentBean> findAllOwnedByCurrentUser(Pageable pageable);

    Page<ESafeDocumentBean> findAllOwnedByAndSharedWithCurrentUser(Pageable pageable);

    Page<ESafeDocumentBean> findAllSharedByCurrentUser(Pageable pageable);

    Page<ESafeDocumentBean> findAllSharedWithCurrentUser(Pageable pageable);

    Page<ESafeDocumentBean> findAllVisibleWithCurrentUser(Pageable pageable);

    Page<ESafeDocumentBean> ownedByCurrentUserFilter(EsafeDocumentFilterPayload esafeDocumentFilterPayload, Pageable pageable);

    Page<ESafeDocumentBean> visibleWithCurrentUserFilter(EsafeDocumentFilterPayload esafeDocumentFilterPayload, Pageable pageable);

    Page<ESafeDocumentBean> sharedWithCurrentUserFilter(EsafeDocumentFilterPayload esafeDocumentFilterPayload, Pageable pageable);

    Page<ESafeDocumentBean> sharedByCurrentUserFilter(EsafeDocumentFilterPayload esafeDocumentFilterPayload, Pageable pageable);

    Set<ESafeDocumentVM> findAllByChannelType(ExchangeChannelType channelType);


    ESafeDocumentBean update(MultipartFile file, ESafeDocumentVM eSafeDocumentVM, Long id);

    ESafeDocumentBean getFileById(Long id);
}
