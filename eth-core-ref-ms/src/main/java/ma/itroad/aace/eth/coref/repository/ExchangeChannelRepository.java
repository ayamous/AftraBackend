package ma.itroad.aace.eth.coref.repository;

import ma.itroad.aace.eth.core.repository.BaseJpaRepository;
import ma.itroad.aace.eth.coref.model.entity.ExchangeChannel;

import java.util.List;

public interface ExchangeChannelRepository extends BaseJpaRepository<ExchangeChannel> {
    List<ExchangeChannel> findAllByOrganization_Id(Long organizationId);
}
