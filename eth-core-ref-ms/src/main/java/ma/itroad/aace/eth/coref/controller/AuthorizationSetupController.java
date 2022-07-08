package ma.itroad.aace.eth.coref.controller;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import ma.itroad.aace.eth.coref.model.bean.AuthorizationSetupBean;
import ma.itroad.aace.eth.coref.model.mapper.AuthorizationSetupMapper;
import ma.itroad.aace.eth.coref.service.IAuthorizationSetupService;

@RepositoryRestController
@RequestMapping(value = "/authorizationsetups", consumes = { MediaType.ALL_VALUE }, produces = MediaTypes.HAL_JSON_VALUE)
@ResponseBody
public class AuthorizationSetupController {

    @Autowired
    IAuthorizationSetupService authorizationsetupService;
    @Autowired
    AuthorizationSetupMapper authorizationsetupMapper;

    @GetMapping
    public Page<AuthorizationSetupBean> getAllAuthorizationSetups(@RequestParam(defaultValue = "0") Integer page,
                                              @RequestParam(defaultValue = "10") Integer size){
        Sort.Order order = new Sort.Order(Sort.Direction.DESC, "id");
        return authorizationsetupService.getAll(PageRequest.of(page, size,  Sort.by(order)));
    }
   
}