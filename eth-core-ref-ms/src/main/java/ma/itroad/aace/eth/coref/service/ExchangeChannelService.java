package ma.itroad.aace.eth.coref.service;

import ma.itroad.aace.eth.core.service.IBaseService;
import ma.itroad.aace.eth.coref.model.bean.ExchangeChannelBean;
import ma.itroad.aace.eth.coref.model.bean.ExchangeChannelRequest;
import ma.itroad.aace.eth.coref.model.entity.ExchangeChannel;

import java.util.List;
import java.util.Set;

public interface ExchangeChannelService extends IBaseService<ExchangeChannel, Long> {
    Set<ExchangeChannelBean> add(ExchangeChannelRequest request);
    Set<ExchangeChannelBean> update(ExchangeChannelRequest request);
    void delete(ExchangeChannelRequest request);
    List<ExchangeChannelBean> findAllByOrganizationId(Long organizationId);
}
