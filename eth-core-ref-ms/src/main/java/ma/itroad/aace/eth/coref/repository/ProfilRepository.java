package ma.itroad.aace.eth.coref.repository;

import ma.itroad.aace.eth.core.repository.BaseJpaRepository;
import ma.itroad.aace.eth.coref.model.entity.Profil;

import java.util.Optional;

public interface ProfilRepository extends BaseJpaRepository<Profil> {
    Optional<Profil> findByReference(String reference);
}
