package ma.itroad.aace.eth.coref.controller;

import ma.itroad.aace.eth.core.model.enums.TableRef;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.LangBean;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.entity.Lang;
import ma.itroad.aace.eth.coref.model.mapper.LangMapper;
import ma.itroad.aace.eth.coref.repository.LangRepository;
import ma.itroad.aace.eth.coref.service.ILangService;
import ma.itroad.aace.eth.coref.service.helper.LangEntityRefLang;
import ma.itroad.aace.eth.coref.service.impl.ImportDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(value = "/langs", consumes = {MediaType.ALL_VALUE}, produces = MediaTypes.HAL_JSON_VALUE)
@ResponseBody
public class LangController {

    @Autowired
    ILangService iLangService;

    @Autowired
    LangMapper langMapper;

    @Autowired
    ImportDataService importDataService;

    @Autowired
    private LangRepository langRepository;


    @GetMapping("/all")
    ResponseEntity<Page<LangBean>> getAll(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size,
                                          @RequestParam(defaultValue = "DESC") String  orderDirection) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LangBean> response = iLangService.getAll(pageable);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("all/{lang}")
    ResponseEntity<Page<LangEntityRefLang>> getAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @PathVariable("lang") String lang,  @RequestParam(defaultValue = "DESC") String orderDirection) {
        Page<LangEntityRefLang> response = iLangService.getAll(page, size, lang, orderDirection);
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("delete/{id}")
    @ResponseBody
    public ResponseEntity deleteLang(@PathVariable("id") Long id) {
        ErrorResponse response = iLangService.delete(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @DeleteMapping("deleteInternationalisation/{id}/{lang}")
    @ResponseBody
    public ResponseEntity deleteInternationalisation(@PathVariable("id") Long id, @PathVariable("lang") String lang) {

        ErrorResponse response = iLangService.deleteInternationalisation(lang, id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @DeleteMapping("/deleteList")
    @ResponseBody
    public ResponseEntity deleteList(@RequestBody ListOfObject listOfObject) {
        ErrorResponse response = iLangService.deleteList(listOfObject);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/import")
    public ResponseEntity<String> importFile(@RequestParam("file") MultipartFile file) {

        importDataService.setService(iLangService);
        return importDataService.importFile(file);
    }
    
    @PostMapping("/import-valid")
    public ResponseEntity<?> importValidFile(@RequestParam("file") MultipartFile file) {
        
        return iLangService.saveFromExcelAfterValidation(file);
    }


    @GetMapping("/download/{lang}")
    @ResponseBody
    public ResponseEntity<InputStreamResource> getFile(@PathVariable("lang") String lang) throws IOException {
        String filename = "langs.xlsx";
        InputStreamResource file = new InputStreamResource(iLangService.load(lang));
        MediaType excelMediaType = new MediaType("application", "vnd.ms-excel");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

        @PostMapping("/add")
        public LangEntityRefLang addLang(@RequestBody LangEntityRefLang langEntityRefLang) {
            iLangService.addLang(langEntityRefLang);
            return langEntityRefLang;
        }

        @GetMapping("/find/{lang}/{id}")
        public LangEntityRefLang findLang(@PathVariable("lang") String lang, @PathVariable("id") Long id) {
            return iLangService.findLang(id, lang);
        }

    @PostMapping("/addInternationalisation/{lang}")
    public EntityRefLang addInternationalisation(@PathVariable("lang") String lang, @RequestBody EntityRefLang entityRefLang) {
        iLangService.addInternationalisation(entityRefLang,lang);
        return entityRefLang;
    }

    @GetMapping("/listAllLang/{tableRef}/{id}")
    public List<EntityRefLang> findByTableRefAndRefId(@PathVariable("tableRef") TableRef tableRef, @PathVariable("id") Long id){
        return iLangService.findByTableRefAndRefId(tableRef,id);
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<Lang> updateLang(@PathVariable(value = "id") Long langId,
                                               @Valid @RequestBody Lang langs) throws ResourceNotFoundException {
        Lang lang = langRepository.findById(langId)
                .orElseThrow(() -> new ResourceNotFoundException("lang not found for this id :: " + langId));

        lang.setCode(langs.getCode());
        lang.setName(langs.getName());
        lang.setDef(langs.getDef());
        final Lang updateLang = langRepository.save(lang);
        return ResponseEntity.ok(updateLang);
    }

    @GetMapping("/getAllByCodeOrLabel/{value}/{lang}")
    public ResponseEntity getAllByCodeOrLabel(@PathVariable("value") String value, @PathVariable("lang") String lang, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size)  {
        if (value.equals(" ") || value == null|| value.equals(""))
            return getAll(page, size, lang, "ASC");
        else
        return ResponseEntity.ok().body( iLangService.filterByCodeOrLabelProjection(value , PageRequest.of(page,size) , lang));
    }

//    @GetMapping("/{id}")
//    public ResponseEntity getById(@PathVariable("id") Long id){
//        return ResponseEntity.ok().body(iLangService.findById(id));
//    }

}
