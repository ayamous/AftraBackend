package ma.itroad.aace.eth.coref.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.itroad.aace.eth.core.service.impl.BaseServiceImpl;
import ma.itroad.aace.eth.coref.exception.FunctionalError;
import ma.itroad.aace.eth.coref.model.bean.ExchangeChannelBean;
import ma.itroad.aace.eth.coref.model.bean.ExchangeChannelRequest;
import ma.itroad.aace.eth.coref.model.entity.ExchangeChannel;
import ma.itroad.aace.eth.coref.model.entity.Organization;
import ma.itroad.aace.eth.coref.repository.ExchangeChannelRepository;
import ma.itroad.aace.eth.coref.repository.OrganizationRepository;
import ma.itroad.aace.eth.coref.service.ExchangeChannelService;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class ExchangeChannelServiceImpl extends BaseServiceImpl<ExchangeChannel, Long> implements ExchangeChannelService {

    private final ExchangeChannelRepository repository;
    private final OrganizationRepository organizationRepository;

    @Override
    public Set<ExchangeChannelBean> add(ExchangeChannelRequest request) {
        if (request == null || request.getOrganizationId() == null || request.getExchangeChannels() == null || request.getExchangeChannels().isEmpty()) {
            throw new FunctionalError("Invalid exchange channel request");
        }
        return doUpdate(request, 0);
    }

    @Override
    public Set<ExchangeChannelBean> update(ExchangeChannelRequest request) {
        if (request == null || request.getOrganizationId() == null) {
            throw new FunctionalError("Organization id not set");
        }
        if (request.getExchangeChannels() == null) {
            request.setExchangeChannels(new HashSet<>());
        }
        return doUpdate(request, 1);
    }

    @Override
    public void delete(ExchangeChannelRequest request) {
        if (request == null || request.getOrganizationId() == null) {
            throw new FunctionalError("Organization id not set");
        }
        if (request.getExchangeChannels() == null) {
            request.setExchangeChannels(new HashSet<>());
        }
        doUpdate(request, 2);
    }

    private Set<ExchangeChannelBean> doUpdate(ExchangeChannelRequest request, int op) {
        Organization organization = organizationRepository.findById(request.getOrganizationId()).orElseThrow(() -> new RuntimeException("No organization found with the given id"));
        log.info("Upserting {} exchange channel(s) for the organization {}", request.getExchangeChannels().size(), organization.getName());
        Set<ExchangeChannel> exchangeChannels = organization.getExchangeChannels();
        if (op == 2) {
            Set<Long> requestExchangeChannelIds = exchangeChannels.stream().map(ExchangeChannel::getId).collect(Collectors.toSet());
            if (request.getExchangeChannels().stream().anyMatch(e -> e.getId() == null || !requestExchangeChannelIds.contains(e.getId()))) {
                throw new FunctionalError("Invalid exchange channels");
            }
            request.getExchangeChannels().forEach(exchangeChannel -> repository.deleteById(exchangeChannel.getId()));
            return new HashSet<>();
        }
        if (op == 0 && request.getExchangeChannels().stream().anyMatch(e -> exchangeChannels.stream().anyMatch(e::sameAs))) {
            throw new FunctionalError("Exchange channel(s) already set for the organization " + organization.getName());
        }
            request.getExchangeChannels().forEach(exchangeChannel -> {
                exchangeChannel.setOrganization(organization);
                repository.save(exchangeChannel);
            });
        return request.getExchangeChannels().stream().map(ExchangeChannelBean::of).collect(Collectors.toSet());
    }

    @Override
    public List<ExchangeChannelBean> findAllByOrganizationId(Long organizationId) {
        if (organizationId == null) {
            throw new FunctionalError("Organization id not set");
        }
        organizationRepository.findById(organizationId).orElseThrow(() -> new FunctionalError("No organization found with the given id"));
        return repository.findAllByOrganization_Id(organizationId).parallelStream().map(ExchangeChannelBean::of).distinct().collect(Collectors.toList());
    }
}
