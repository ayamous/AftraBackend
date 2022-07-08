package ma.itroad.aace.eth.coref.service.impl;

import ma.itroad.aace.eth.core.model.enums.RequestOriginEnum;
import ma.itroad.aace.eth.coref.model.bean.EntityRefLangBean;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.mapper.EntityRefLangMapper;
import ma.itroad.aace.eth.coref.repository.EntityRefLangRepository;
import ma.itroad.aace.eth.coref.service.IEntityRefLangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class EntityRefLangServiceImpl implements IEntityRefLangService {

    @Autowired
    EntityRefLangRepository repository;

    @Autowired
    EntityRefLangMapper mapper;

    @Override
    public EntityRefLang save(EntityRefLang entity) {
        return repository.save(entity);
    }

    @Override
    public EntityRefLang findOneById(Long id) {
        return null;
    }

    @Override
    public List<EntityRefLang> findByCreatedOnOrUpdatedOn(Date date) {
        return null;
    }

    @Override
    public List<EntityRefLang> findByCreatedByOrUpdatedBy(String username) {
        return null;
    }

    @Override
    public List<EntityRefLang> findByRequestOrigin(RequestOriginEnum requestOrigin) {
        return null;
    }

    @Override
    public Page<EntityRefLang> findAll(Specification<EntityRefLang> spec, Pageable pageable) {
        return null;
    }

    @Override
    public Page<EntityRefLangBean> getAll(Pageable pageable) {
        return null;
    }

    @Override
    public EntityRefLangBean save(EntityRefLangBean bean) {

        EntityRefLang entity = mapper.beanToEntity(bean);
        EntityRefLangBean result = mapper.entityToBean(repository.save(entity));
        return result;
    }
}
