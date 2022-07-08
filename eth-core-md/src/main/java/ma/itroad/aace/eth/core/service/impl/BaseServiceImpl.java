package ma.itroad.aace.eth.core.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ma.itroad.aace.eth.core.model.entity.AuditEntity;
import ma.itroad.aace.eth.core.model.enums.RequestOriginEnum;
import ma.itroad.aace.eth.core.model.mapper.GenericModelMapper;
import ma.itroad.aace.eth.core.repository.BaseJpaRepository;
import ma.itroad.aace.eth.core.rsql.helper.CustomRsqlVisitor;
import ma.itroad.aace.eth.core.rsql.service.IRsqlService;
import ma.itroad.aace.eth.core.service.IBaseService;
import ma.itroad.aace.eth.core.service.LoggerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.Node;


public class BaseServiceImpl<E extends AuditEntity, B extends Serializable>
	   extends LoggerSupport
       implements IBaseService<E,B>, IRsqlService<E,B> {
	
	@Autowired
	BaseJpaRepository<E> baseJpaRepository;
	
	GenericModelMapper<E, B> mapper;

	@Override
	public E save(E entity) {
		return baseJpaRepository.save(entity);
	}
	
	@Override
	public E findOneById(Long id) {
		return baseJpaRepository.findOneById(id);
	}

	@Override
	public List<E> findByCreatedOnOrUpdatedOn(Date date) {
		return baseJpaRepository.findByCreatedOnOrUpdatedOn(date, date);
	}

	@Override
	public List<E> findByCreatedByOrUpdatedBy(String username) {
		return baseJpaRepository.findByCreatedByOrUpdatedBy(username, username);
	}

	@Override
	public List<E> findByRequestOrigin(RequestOriginEnum requestOrigin) {
		return baseJpaRepository.findByRequestOrigin(requestOrigin);
	}

	@Override
	public Page<E> findAll(Specification<E> spec, Pageable pageable) {
		return baseJpaRepository.findAll(spec, pageable);
	}

	@Override
	public Page<B> getAll(Pageable pageable) {
		Page<E> pageE = baseJpaRepository.findAll(pageable);
		Page<B> pageB = pageE.map(mapper::entityToBean);
		return pageB;
	}

	@Override
	public Page<E> rsqlPageSearch(String domain, Integer page, Integer size, Order order) {
		Node rootNode = new RSQLParser().parse(domain);
		Specification<E> entitySpec = rootNode.accept(new CustomRsqlVisitor<E>());
		return baseJpaRepository.findAll(entitySpec, PageRequest.of(page, size, Sort.by(order))); 
	}

	@Override
	public List<E> rsqlListSearch(String domain) {
		Node rootNode = new RSQLParser().parse(domain);
		Specification<E> entitySpec = rootNode.accept(new CustomRsqlVisitor<E>());
		return baseJpaRepository.findAll(entitySpec); 
	}

	@Override
	public Page<B> rsqlPageBeanSearch(GenericModelMapper<E,B> modelMapper, String domain, Integer page, Integer size, Order order) {
		List<E> entities = rsqlListSearch(domain);
		List<B> beans = new ArrayList<B>();
		entities.forEach(e -> { beans.add(modelMapper.entityToBean(e));}) ;
		return new PageImpl<B>(beans, PageRequest.of(page, size, Sort.by(new Sort.Order(Sort.Direction.DESC, "id"))), beans.size());
	}
}
