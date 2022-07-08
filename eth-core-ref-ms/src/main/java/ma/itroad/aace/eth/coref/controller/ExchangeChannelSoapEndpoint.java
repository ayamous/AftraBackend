package ma.itroad.aace.eth.coref.controller;

import lombok.AllArgsConstructor;
import ma.itroad.aace.eth.core.common.utils.CollectionUtils;
import ma.itroad.aace.eth.coref.exception.FunctionalError;
import ma.itroad.aace.eth.coref.model.bean.ESafeDocumentPermissionRequest;
import ma.itroad.aace.eth.coref.model.bean.ExchangeChannelBean;
import ma.itroad.aace.eth.coref.model.entity.ESafeDocument;
import ma.itroad.aace.eth.coref.model.entity.ESafeDocumentPermission;
import ma.itroad.aace.eth.coref.model.entity.ExchangeChannel;
import ma.itroad.aace.eth.coref.model.entity.UserAccount;
import ma.itroad.aace.eth.coref.model.enums.ExchangeChannelMode;
import ma.itroad.aace.eth.coref.model.enums.ExchangeChannelType;
import ma.itroad.aace.eth.coref.model.view.ESafeDocumentVM;
import ma.itroad.aace.eth.coref.repository.ESafeDocumentPermissionRepository;
import ma.itroad.aace.eth.coref.repository.ESafeDocumentRepository;
import ma.itroad.aace.eth.coref.security.keycloak.utils.SecurityUtils;
import ma.itroad.aace.eth.coref.service.ExchangeChannelService;
import ma.itroad.aace.eth.coref.service.IESafeDocumentService;
import ma.itroad.aace.eth.coref.service.IUserAccountService;
import ma.itroad.aace.eth.coref.service.converter.BASE64DecodedMultipartFile;
import ma.itroad.ethub.*;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Endpoint
@AllArgsConstructor
public class ExchangeChannelSoapEndpoint {

    private final IESafeDocumentService eSafeDocumentService;
    private final ESafeDocumentRepository eSafeDocumentRepository;
    private final ESafeDocumentPermissionRepository eSafeDocumentPermissionRepository;
    private final IUserAccountService userAccountService;
    private final ExchangeChannelService exchangeChannelService;

    @PayloadRoot(localPart = "sendDocumentRequest", namespace = "http://itroad.ma/ethub")
    @ResponsePayload
    public void sendESafeDocument(@RequestPayload SendDocumentRequest sendDocumentRequest) {
        validateAuthorization();
        ESafeDocumentVM eSafeDocumentVM = new ESafeDocumentVM();
        eSafeDocumentVM.setFileName(sendDocumentRequest.getFileName());
        eSafeDocumentVM.setVisibilityPermission(new ESafeDocumentPermissionRequest());
        eSafeDocumentVM.setSharingPermission(new ESafeDocumentPermissionRequest());
        eSafeDocumentVM.getVisibilityPermission().addOrganization(sendDocumentRequest.getOrganizationId());
        eSafeDocumentVM.getSharingPermission().addOrganization(sendDocumentRequest.getOrganizationId());
        eSafeDocumentVM.setChannelType(ExchangeChannelType.SOAP);
        MultipartFile multipartFile = new BASE64DecodedMultipartFile(sendDocumentRequest.getFile());
        eSafeDocumentService.save(multipartFile, eSafeDocumentVM);
    }

    @PayloadRoot(localPart = "getAllDocumentRequest", namespace = "http://itroad.ma/ethub")
    @ResponsePayload
    public GetAllDocumentResponse getAllESafeDocument(@RequestPayload GetAllDocumentRequest getAllDocumentRequest) {
        UserAccount userAccount = validateAuthorization();
        GetAllDocumentResponse response = new GetAllDocumentResponse();
        Set<Document> documents = eSafeDocumentRepository.findAllByChannelType(ExchangeChannelType.SOAP).stream().filter(
                e -> {
                    List<ESafeDocumentPermission> permissions = eSafeDocumentPermissionRepository.findAllByDocument_Id(e.getId());
                    return CollectionUtils.isNotEmpty(permissions) && permissions.stream().filter(eSafeDocumentPermission ->
                            eSafeDocumentPermission.getUserAccount() != null && userAccount.getId().equals(eSafeDocumentPermission.getUserAccount().getId())).findFirst().orElse(null) != null;
                }).map(eSafeDocument -> {
                    {
                        Document document = new Document();
                        document.setFileId(eSafeDocument.getId());
                        document.setFileName(eSafeDocument.getFileName());
                        return document;
                    }
        }).collect(Collectors.toSet());
        response.getDocuments().addAll(new ArrayList<>(documents));
        return response;
    }

    @PayloadRoot(localPart = "getDocumentRequest", namespace = "http://itroad.ma/ethub")
    @ResponsePayload
    public GetDocumentResponse getESafeDocument(@RequestPayload GetDocumentRequest getDocumentRequest) {
        validateAuthorization();
        ESafeDocument eSafeDocument = eSafeDocumentService.findOneById(getDocumentRequest.getFileId());
        if (eSafeDocument == null) {
            throw new FunctionalError("ESafe document not found");
        }
        ByteArrayResource file = eSafeDocumentService.download(eSafeDocument.getEdmStorld());
        GetDocumentResponse response = new GetDocumentResponse();
        response.setFileName(eSafeDocument.getFileName());
        response.setFile(file.getByteArray());
        return response;
    }

    private UserAccount validateAuthorization() {
        UserAccount keycloakUser = SecurityUtils.getCurrentUser().orElseThrow(() -> new FunctionalError("Unauthorized attempt"));
        UserAccount userAccount = userAccountService.findUserByReference(keycloakUser.getReference()).orElseThrow(() -> new FunctionalError("Unauthorized attempt"));
        if (userAccount.getOrganization() == null) {
            throw new FunctionalError("User organization is not set");
        }
        List<ExchangeChannelBean> exchangeChannels = exchangeChannelService.findAllByOrganizationId(userAccount.getOrganization().getId());
        if (CollectionUtils.isNullOrEmpty(exchangeChannels)) {
            throw new FunctionalError("User organization exchange channels are not set");
        }
        if (exchangeChannels.stream().filter(e -> ExchangeChannelType.SOAP.equals(e.getType()) && !ExchangeChannelMode.IN.equals(e.getMode())).findFirst().orElse(null) != null) {
            throw new FunctionalError("The user's organization is not authorized");
        }
        return userAccount;
    }
}
