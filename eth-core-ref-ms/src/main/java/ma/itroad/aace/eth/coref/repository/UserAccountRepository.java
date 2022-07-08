package ma.itroad.aace.eth.coref.repository;

import ma.itroad.aace.eth.core.repository.BaseJpaRepository;
import ma.itroad.aace.eth.coref.model.entity.UserAccount;

import java.util.Optional;
import java.util.Set;

public interface UserAccountRepository extends BaseJpaRepository<UserAccount> {
    Optional<UserAccount> findByReference(String reference);
    Set<UserAccount> findAllByOrganization_Id(Long organizationId);
}
