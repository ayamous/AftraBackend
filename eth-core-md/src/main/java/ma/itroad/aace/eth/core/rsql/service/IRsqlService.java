package ma.itroad.aace.eth.core.rsql.service;

import ma.itroad.aace.eth.core.model.entity.AuditEntity;
import ma.itroad.aace.eth.core.model.mapper.GenericModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.io.Serializable;
import java.util.List;

public interface IRsqlService<T extends AuditEntity, B extends Serializable> {
    List<T> rsqlListSearch(String domain);
    Page<T> rsqlPageSearch(String domain, Integer page, Integer size, Sort.Order order);
    Page<B> rsqlPageBeanSearch(GenericModelMapper<T, B> modelMapper, String domain, Integer page, Integer size, Sort.Order order);
}
