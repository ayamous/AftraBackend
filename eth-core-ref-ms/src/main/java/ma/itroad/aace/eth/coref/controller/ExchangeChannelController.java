package ma.itroad.aace.eth.coref.controller;

import lombok.AllArgsConstructor;
import ma.itroad.aace.eth.coref.model.bean.ExchangeChannelBean;
import ma.itroad.aace.eth.coref.model.bean.ExchangeChannelRequest;
import ma.itroad.aace.eth.coref.model.enums.ExchangeChannelMode;
import ma.itroad.aace.eth.coref.model.enums.ExchangeChannelType;
import ma.itroad.aace.eth.coref.service.ExchangeChannelService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/exchange-channels")
public class ExchangeChannelController {

    private final ExchangeChannelService exchangeChannelService;

    @GetMapping("/types")
    public ExchangeChannelType[] getTypes() {
        return ExchangeChannelType.values();
    }

    @GetMapping("/modes")
    public ExchangeChannelMode[] getModes() {
        return ExchangeChannelMode.values();
    }

    @GetMapping("/organization/{id}")
    public List<ExchangeChannelBean> getAllByOrganizationId(@PathVariable("id") Long id) {
        return exchangeChannelService.findAllByOrganizationId(id);
    }

    @PostMapping
    public Set<ExchangeChannelBean> addChannels(@RequestBody ExchangeChannelRequest request) {
        return exchangeChannelService.add(request);
    }

    @PutMapping
    public Set<ExchangeChannelBean> updateChannels(@RequestBody ExchangeChannelRequest request) {
        return exchangeChannelService.update(request);
    }

    @DeleteMapping
    public void deleteChannels(@RequestBody ExchangeChannelRequest request) {
        exchangeChannelService.delete(request);
    }


}
