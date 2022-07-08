package ma.itroad.aace.eth.coref.repository;
 
import ma.itroad.aace.eth.core.repository.BaseJpaRepository;
import ma.itroad.aace.eth.coref.model.entity.PersonalContact;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

@RepositoryRestResource()
public interface PersonalContactRepository extends BaseJpaRepository<PersonalContact> {
    Optional<PersonalContact> findByReference(String reference);
}
