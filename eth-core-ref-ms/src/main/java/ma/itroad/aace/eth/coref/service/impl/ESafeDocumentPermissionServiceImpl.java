package ma.itroad.aace.eth.coref.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.itroad.aace.eth.core.common.EthClient;
import ma.itroad.aace.eth.core.common.api.messaging.domain.AppMessage;
import ma.itroad.aace.eth.core.common.api.messaging.enums.MessageType;
import ma.itroad.aace.eth.coref.model.bean.ESafeDocumentPermissionRequest;
import ma.itroad.aace.eth.coref.model.entity.ESafeDocument;
import ma.itroad.aace.eth.coref.model.entity.ESafeDocumentPermission;
import ma.itroad.aace.eth.coref.model.entity.ESafeDocumentPermissionId;
import ma.itroad.aace.eth.coref.model.entity.UserAccount;
import ma.itroad.aace.eth.coref.model.enums.ESafeDocumentPermissionType;
import ma.itroad.aace.eth.coref.projection.OrganizationProjectionId;
import ma.itroad.aace.eth.coref.repository.ESafeDocumentPermissionRepository;
import ma.itroad.aace.eth.coref.repository.OrganizationRepository;
import ma.itroad.aace.eth.coref.service.ESafeDocumentPermissionService;
import ma.itroad.aace.eth.coref.service.IUserAccountService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class ESafeDocumentPermissionServiceImpl implements ESafeDocumentPermissionService {

    private final ESafeDocumentPermissionRepository repository;
    private final IUserAccountService userAccountService;
    private final OrganizationRepository organizationRepository;
    private final EthClient ethClient;

    @Override
    public void addPermission(ESafeDocument eSafeDocument, ESafeDocumentPermissionRequest request, ESafeDocumentPermissionType permissionType) {
        if (request == null) {
            return;
        }
        if (CollectionUtils.isNotEmpty(request.getCountries())) {
            request.getCountries().forEach(countryId -> {
                List<OrganizationProjectionId> organizationProjectionIds = organizationRepository.findAllByCountryRef_Id(countryId);
                request.addOrganizations(organizationProjectionIds.stream().map(OrganizationProjectionId::getId).collect(Collectors.toSet()));
            });
        }
        log.info("Adding {} permission to {} esafe document for {} organization(s)", permissionType, eSafeDocument.getFileName(), request.getOrganizations().size());
        addPermissionAndSendNotification(request.getOrganizations(), eSafeDocument, permissionType);
    }

    @Override
    public void addPermissionForAll(ESafeDocument eSafeDocument, ESafeDocumentPermissionType permissionType) {
        log.info("Adding {} permission to {} esafe document for all organizations", permissionType, eSafeDocument.getFileName());
        addPermissionAndSendNotification(organizationRepository.findAllByIdNotNull().stream().map(OrganizationProjectionId::getId).collect(Collectors.toSet()), eSafeDocument, permissionType);
    }

    private void addPermissionAndSendNotification(Set<Long> organizationIds, ESafeDocument eSafeDocument, ESafeDocumentPermissionType permissionType) {
        List<AppMessage> appMessages = new ArrayList<>();
        organizationIds.forEach(organizationId ->
                userAccountService.findAllByOrganizationId(organizationId).forEach(userAccount -> CollectionUtils.addIgnoreNull(appMessages, createESafePermission(eSafeDocument, userAccount, permissionType))));
        log.info("ESafe document {} {} permission was added successfully for {} organization(s)", eSafeDocument.getFileName(), permissionType, organizationIds.size());
        //ethClient.getMessaging().sendMailAsync(appMessages);
    }

    private AppMessage createESafePermission(ESafeDocument eSafeDocument, UserAccount userAccount, ESafeDocumentPermissionType permissionType) {
        ESafeDocumentPermissionId id = new ESafeDocumentPermissionId();
        id.setUserAccountId(userAccount.getId());
        id.setDocumentId(eSafeDocument.getId());
        ESafeDocumentPermission permission = repository.findById(id).orElse(new ESafeDocumentPermission());
        Map<String, Object> params = new HashMap<>();
        String verb = "partagé avec vous";
        if (permission.getId() == null) {
            permission.createId(id, eSafeDocument, userAccount);
            permission.setSharable(permissionType.isSharing());
            permission.setVisible(permissionType.isView());
        } else {
            permission.setDocument(eSafeDocument);
            permission.setUserAccount(userAccount);
            if (permissionType.isSharing()) {
                permission.setSharable(true);
            }
            if (permissionType.isView()) {
                permission.setVisible(true);
                verb = permission.isSharable() ? "partagé avec vous et vous a mis visible" : "vous a mis visible";
            }
        }
        permission.setEnabled(true);
        repository.save(permission);
        if (userAccount.getEmail() == null) {
            log.warn("Email notification cannot be sent to user {}. User email must be provided.", userAccount.getLogin());
            return null;
        }
        AppMessage appMessage = new AppMessage();
        appMessage.setMessageId(UUID.randomUUID().toString());
        appMessage.setRecipient(userAccount.getEmail());
        appMessage.setType(MessageType.EMAIL_HTML);
        String documentType = eSafeDocument.getIsFolder() ? "dossier" : "document";
        params.put("verb", verb);
        params.put("userFullName", getFullName(eSafeDocument.getOwner()));
        params.put("fullName", getFullName(userAccount));
        params.put("documentType", documentType);
        params.put("documentName", eSafeDocument.getFileName());
        appMessage.setSubject(StringUtils.capitalize(documentType).concat(" partagé avec vous : ").concat(eSafeDocument.getFileName()));
        appMessage.addProperty(AppMessage.TEMPLATE_KEY_NAME, "sharing");
        appMessage.addProperty(AppMessage.TEMPLATE_PARAMS_KEY_NAME, params);
        return appMessage;
    }

    private String getFullName(UserAccount account) {
        return account.getFirstName().concat(" ").concat(account.getLastName());
    }
}
