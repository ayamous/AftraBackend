package ma.itroad.aace.eth.coref.controller;

import ma.itroad.aace.eth.core.helper.ExcelHelper;
import ma.itroad.aace.eth.coref.exception.AdventureNotFoundException;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.LangBean;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.bean.SectionRefBean;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.entity.Lang;
import ma.itroad.aace.eth.coref.model.entity.UnitRef;
import ma.itroad.aace.eth.coref.model.mapper.projections.UnitRefEntityRefLangProjection;
import ma.itroad.aace.eth.coref.service.helper.*;
import ma.itroad.aace.eth.coref.service.impl.ImportDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ma.itroad.aace.eth.coref.model.bean.UnitRefBean;
import ma.itroad.aace.eth.coref.model.mapper.UnitRefMapper;
import ma.itroad.aace.eth.coref.service.IUnitRefService;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(value = "/unitRefs", consumes = {MediaType.ALL_VALUE}, produces = MediaTypes.HAL_JSON_VALUE)
@ResponseBody
public class UnitRefController {

    @Autowired
    IUnitRefService unitrefService;

    @Autowired
    UnitRefMapper unitrefMapper;

    @Autowired
    ImportDataService importDataService;

    /*
        @GetMapping("/unitrefs")
        public Page<UnitRefBean> getAllUnitRefs(@RequestParam(defaultValue = "0") Integer page,
                                                  @RequestParam(defaultValue = "10") Integer size){
            Sort.Order order = new Sort.Order(Sort.Direction.DESC, "id");
            return unitrefService.getAll(PageRequest.of(page, size,  Sort.by(order)));
        }*/

    @GetMapping("/all")
    ResponseEntity<Page<UnitRefBean>> getAll(@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "DESC") String orderDirection) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UnitRefBean> response = unitrefService.getAll(pageable);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("all/{lang}")
    ResponseEntity<Page<UnitRefEntityRefLang>> getAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @PathVariable("lang") String lang, @RequestParam(defaultValue = "DESC") String orderDirection) {
        Page<UnitRefEntityRefLang> response = unitrefService.getAll(page, size, lang, orderDirection);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/import")
    public ResponseEntity<String> importFile(@RequestParam("file") MultipartFile file) {
        importDataService.setService(unitrefService);
        return importDataService.importFile(file);
    }
    
    @PostMapping("/import-valid")
    public ResponseEntity<?> importValidFile(@RequestParam("file") MultipartFile file) {
        
        return unitrefService.saveFromAfterValidation(file);
    }

    @GetMapping("/download/{lang}")
    @ResponseBody
    public ResponseEntity<InputStreamResource> getFile(@PathVariable("lang") String lang) throws IOException {
        String filename = "unitrefs.xlsx";
        InputStreamResource file = new InputStreamResource(unitrefService.load(lang));
        MediaType excelMediaType = new MediaType("application", "vnd.ms-excel");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    @DeleteMapping("delete/{id}")
    @ResponseBody
    public ResponseEntity deleteUnitRef(@PathVariable("id") Long id) {

        ErrorResponse response = unitrefService.delete(id);
        return ResponseEntity.status(response.getStatus()).body(response);

    }

    @DeleteMapping("deleteInternationalisation/{id}/{lang}")
    @ResponseBody
    public ResponseEntity deleteInternationalisation(@PathVariable("id") Long id, @PathVariable("lang") String lang) {

        ErrorResponse response = unitrefService.deleteInternationalisation(lang, id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/add")
    public UnitRefEntityRefLang addUnitRef(@RequestBody UnitRefEntityRefLang unitRefEntityRefLang) {
        unitrefService.addUnitRef(unitRefEntityRefLang);
        return unitRefEntityRefLang;
    }

    @PostMapping("/addInternationalisation/{lang}")
    public EntityRefLang addInternationalisation(@PathVariable("lang") String lang,@RequestBody EntityRefLang entityRefLang) {
        unitrefService.addInternationalisation(entityRefLang,lang);
        return entityRefLang;
    }

    @GetMapping("/find/{lang}/{id}")
    public UnitRefEntityRefLang findUnitRef(@PathVariable("lang") String lang, @PathVariable("id") Long id) {
        return unitrefService.findUnitRef(id, lang);
    }

    @GetMapping("/getAllByCodeOrLabel/{value}/{lang}")
    public ResponseEntity getAllByCodeOrLabel(@PathVariable("value") String value, @PathVariable("lang") String lang, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size)  {
        if (value.equals(" ") || value == null|| value.equals(""))
            return getAll(page, size, lang, "ASC");
        else
        return ResponseEntity.ok().body( unitrefService.filterByCodeOrLabelProjection(value , PageRequest.of(page,size) , lang));
    }

    @DeleteMapping("/deleteList")
    @ResponseBody
    public ResponseEntity deleteList(@RequestBody ListOfObject listOfObject) {
        ErrorResponse response = unitrefService.deleteList(listOfObject);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

//    @GetMapping("/{id}")
//    public ResponseEntity getById(@PathVariable("id") Long id){
//        return ResponseEntity.ok().body(unitrefService.findById(id));
//    }
}
