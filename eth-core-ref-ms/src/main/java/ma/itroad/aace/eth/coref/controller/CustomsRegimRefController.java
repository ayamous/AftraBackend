package ma.itroad.aace.eth.coref.controller;

import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.CustomsRegimRefBean;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.mapper.CustomsRegimRefMapper;
import ma.itroad.aace.eth.coref.service.ICustomsRegimRefService;
import ma.itroad.aace.eth.coref.service.helper.CustomsRegimRefEntityRefLang;
import ma.itroad.aace.eth.coref.service.helper.DeclarationTypeRefEntityRefLang;
import ma.itroad.aace.eth.coref.service.impl.ImportDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping(value = "/customsRegimRefs", consumes = {MediaType.ALL_VALUE}, produces = MediaTypes.HAL_JSON_VALUE)
@ResponseBody
public class CustomsRegimRefController {

    @Autowired
    ICustomsRegimRefService customsregimrefService;
    @Autowired
    CustomsRegimRefMapper customsregimrefMapper;

    @Autowired
    ImportDataService importDataService;

    @GetMapping("/download/{lang}")
    @ResponseBody
    public ResponseEntity<InputStreamResource> getFile(@PathVariable("lang") String lang) throws IOException {
        String filename = "customsregimrefs.xlsx";
        InputStreamResource file = new InputStreamResource(customsregimrefService.load(lang));
        MediaType excelMediaType = new MediaType("application", "vnd.ms-excel");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    @GetMapping("/all")
    ResponseEntity<Page<CustomsRegimRefBean>> getAll(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "10") int size,
                                                     @RequestParam(defaultValue = "DESC") String orderDirection) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CustomsRegimRefBean> response = customsregimrefService.getAll(pageable);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("all/{lang}")
    ResponseEntity<Page<CustomsRegimRefEntityRefLang>> getAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @PathVariable("lang") String lang,  @RequestParam(defaultValue = "DESC") String orderDirection) {
        Page<CustomsRegimRefEntityRefLang> response = customsregimrefService.getAll(page, size, lang, orderDirection);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/import")
    public ResponseEntity<String> importFile(@RequestParam("file") MultipartFile file) {
        importDataService.setService(customsregimrefService);
        return importDataService.importFile(file);
    }

    @DeleteMapping("delete/{id}")
    @ResponseBody
    public ErrorResponse deleteCustomsRegimRef(@PathVariable("id") Long id) {
         return customsregimrefService.delete(id);
    }

    @DeleteMapping("deleteInternationalisation/{id}/{lang}")
    @ResponseBody
    public ResponseEntity deleteInternationalisation(@PathVariable("id") Long id, @PathVariable("lang") String lang) {

        ErrorResponse response = customsregimrefService.deleteInternationalisation(lang, id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/add")
    public CustomsRegimRefEntityRefLang addCustomsRegimRef(@RequestBody CustomsRegimRefEntityRefLang customsRegimRefEntityRefLang) {
        customsregimrefService.addCustomsRegimRef(customsRegimRefEntityRefLang);
        return customsRegimRefEntityRefLang;
    }

    @GetMapping("/find/{lang}/{id}")
    public CustomsRegimRefEntityRefLang findCustomsRegimRef(@PathVariable("lang") String lang, @PathVariable("id") Long id) {
        return customsregimrefService.findCustomsRegimRef(id, lang);
    }

    @PostMapping("/addInternationalisation/{lang}")
    public EntityRefLang addInternationalisation(@PathVariable("lang") String lang, @RequestBody EntityRefLang entityRefLang) {
        customsregimrefService.addInternationalisation(entityRefLang,lang);
        return entityRefLang;
    }

    @GetMapping("/getAllByCodeOrLabel/{value}/{lang}")
    public ResponseEntity getAllByCodeOrLabel(@PathVariable("value") String value, @PathVariable("lang") String lang, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size)  {
        if (value.equals(" ") || value == null|| value.equals(""))
            return getAll(page, size, lang, "ASC");
        else
        return ResponseEntity.ok().body( customsregimrefService.filterByCodeOrLabelProjection(value , PageRequest.of(page,size) , lang));
    }

    @DeleteMapping("/deleteList")
    @ResponseBody
    public ResponseEntity deleteList(@RequestBody ListOfObject listOfObject) {
        ErrorResponse response = customsregimrefService.deleteList(listOfObject);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/import-valid")
    public ResponseEntity<?> importValidFile(@RequestParam("file") MultipartFile file) {
        return customsregimrefService.saveFromExcelWithReturn(file);
    }


}