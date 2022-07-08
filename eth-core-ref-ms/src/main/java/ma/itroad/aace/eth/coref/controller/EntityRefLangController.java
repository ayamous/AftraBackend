package ma.itroad.aace.eth.coref.controller;


import ma.itroad.aace.eth.coref.model.bean.EntityRefLangBean;
import ma.itroad.aace.eth.coref.service.IEntityRefLangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;

@RestController
@RequestMapping(value = "/entity-ref-lang", consumes = {MediaType.ALL_VALUE})

public class EntityRefLangController {

    @Autowired
    IEntityRefLangService service;


    @RequestMapping(method = RequestMethod.POST, consumes = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody
    public ResponseEntity<EntityRefLangBean> save(@RequestBody EntityRefLangBean bean) throws URISyntaxException {

        EntityRefLangBean result = service.save(bean);

        return ResponseEntity
                .created(new URI("/api/coref/entity-ref-lang/" + result.getId()))
                .body(result);
    }
}
