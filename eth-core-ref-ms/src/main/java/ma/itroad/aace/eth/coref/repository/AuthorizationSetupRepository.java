package ma.itroad.aace.eth.coref.repository;
 
import ma.itroad.aace.eth.core.repository.BaseJpaRepository;
import ma.itroad.aace.eth.coref.model.entity.AuthorizationSetup;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
 
@RepositoryRestResource()
public interface AuthorizationSetupRepository extends BaseJpaRepository<AuthorizationSetup> {
}
