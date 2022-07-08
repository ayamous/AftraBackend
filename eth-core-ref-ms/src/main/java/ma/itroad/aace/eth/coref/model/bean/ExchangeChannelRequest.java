package ma.itroad.aace.eth.coref.model.bean;

import lombok.Data;
import ma.itroad.aace.eth.coref.model.entity.ExchangeChannel;

import java.util.Set;
import java.util.stream.Collectors;

@Data
public class ExchangeChannelRequest {
    private Set<ExchangeChannel> exchangeChannels;
    private Long organizationId;
}