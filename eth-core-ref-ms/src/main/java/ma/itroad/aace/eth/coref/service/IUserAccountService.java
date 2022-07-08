package ma.itroad.aace.eth.coref.service;

import ma.itroad.aace.eth.core.rsql.service.IRsqlService;
import ma.itroad.aace.eth.core.service.IBaseService;
import ma.itroad.aace.eth.core.service.ImportDataService;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.UserAccountBean;
import ma.itroad.aace.eth.coref.model.entity.UserAccount;
import org.springframework.data.domain.Page;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface IUserAccountService extends
        IBaseService<UserAccount, UserAccountBean>,
        IRsqlService<UserAccount, UserAccountBean>,
        ImportDataService {

    Page<UserAccountBean> getAll(final int page, final int size);

    List<UserAccount> excelToUserAccounts(InputStream is);

    ByteArrayInputStream userAccountsToExcel(List<UserAccount> userAccounts);

    Optional<UserAccount> getCurrentUser();

    Optional<UserAccount> findUserByReference(String reference);

    Set<UserAccount> findAllByOrganizationId(Long organizationId);

    ErrorResponse delete(Long id);
}
