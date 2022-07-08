package ma.itroad.aace.eth.coref.service.impl;

import io.jsonwebtoken.lang.Assert;
import lombok.AllArgsConstructor;
import ma.itroad.aace.eth.coref.model.bean.ESafeDocumentSharingRequest;
import ma.itroad.aace.eth.coref.model.bean.ESafeDocumentSharingResponse;
import ma.itroad.aace.eth.coref.model.entity.ESafeDocument;
import ma.itroad.aace.eth.coref.model.entity.ESafeDocumentSharing;
import ma.itroad.aace.eth.coref.model.entity.UserAccount;
import ma.itroad.aace.eth.coref.model.enums.ESafeDocumentSharingMode;
import ma.itroad.aace.eth.coref.model.enums.ESafeDocumentSharingState;
import ma.itroad.aace.eth.coref.repository.ESafeDocumentSharingRepository;
import ma.itroad.aace.eth.coref.service.ESafeDocumentSharingService;
import ma.itroad.aace.eth.coref.service.IESafeDocumentService;
import ma.itroad.aace.eth.coref.service.IUserAccountService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ESafeDocumentSharingServiceImpl implements ESafeDocumentSharingService {

    private final ESafeDocumentSharingRepository repository;
    private final IESafeDocumentService eSafeDocumentService;
    private final IUserAccountService userAccountService;

    @Override
    public ESafeDocumentSharingResponse share(ESafeDocumentSharingRequest request) {
        Assert.notNull(request, "E-Safe document sharing request must not be null");
        ESafeDocument eSafeDocument = eSafeDocumentService.findOneById(request.getDocumentId());
        Assert.notNull(eSafeDocument, "E-Safe document must not be null");
        ESafeDocumentSharingResponse response = new ESafeDocumentSharingResponse();
        if (!CollectionUtils.isEmpty(request.getOrganizations())) {
            request.getOrganizations().forEach(organizationId -> {
                Set<UserAccount> userAccounts = userAccountService.findAllByOrganizationId(organizationId);
                if (!CollectionUtils.isEmpty(userAccounts)) {
                    request.addUserIds(userAccounts.stream().map(UserAccount::getId).collect(Collectors.toSet()));
                }
            });
        }
        request.getUsers().forEach(userId -> {
            UserAccount userAccount = userAccountService.findOneById(userId);
            if (userAccount != null) {
                if (repository.findByDocument_IdAndUserAccount_Id(request.getDocumentId(), userId) != null) {
                    response.addItem(request.getDocumentId(), userId, null, ESafeDocumentSharingState.ALREADY_SHARED, "E-safe document already shared");
                    return;
                }
                if (eSafeDocument.getOwner() != null && userId.equals(eSafeDocument.getOwner().getId())) {
                    response.addItem(request.getDocumentId(), userId, null, ESafeDocumentSharingState.NOT_SHARED, "E-safe document belong to this user");
                    return;
                }
                ESafeDocumentSharing entity = new ESafeDocumentSharing();
                entity.setDocument(eSafeDocument);
                entity.setEnabled(true);
                entity.setMode(ESafeDocumentSharingMode.WRITE);
                entity.setUserAccount(userAccount);
                repository.save(entity);
                response.addSharedItem(request.getDocumentId(), userId, userAccount.getOrganization() == null ? null : userAccount.getOrganization().getId());
            } else {
                response.addItem(request.getDocumentId(), userId, null, ESafeDocumentSharingState.NOT_SHARED, "User not found");
            }
        });
        return response;
    }
}
