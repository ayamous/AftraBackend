package ma.itroad.aace.eth.coref.controller;


import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.CountryRefBean;
import ma.itroad.aace.eth.coref.model.bean.EDocumentTypeBean;
import ma.itroad.aace.eth.coref.model.bean.ESafeDocumentBean;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.mapper.projections.EDocTypeRefEntityRefLangProjection;
import ma.itroad.aace.eth.coref.service.IEDocumentTypeService;
import ma.itroad.aace.eth.coref.service.helper.CountryRefEntityRefLang;
import ma.itroad.aace.eth.coref.service.helper.EDocTypeRefEntityRefLang;
import ma.itroad.aace.eth.coref.service.helper.detailed.TaxationRefLangDetailed;
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
@RequestMapping(value = "/edocumenttype", consumes = { MediaType.ALL_VALUE }, produces = MediaTypes.HAL_JSON_VALUE)
@ResponseBody
public class EDocumentTypeController {

    @Autowired
    private IEDocumentTypeService eDocumentTypeService;

    @Autowired
    private ImportDataService importDataService;

    @GetMapping("/all")
    ResponseEntity<Page<EDocumentTypeBean>> getAll(@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size,
                                                @RequestParam(defaultValue = "DESC") String orderDirection) {
        Pageable pageable = PageRequest.of(page, size);
        Page<EDocumentTypeBean> response = eDocumentTypeService.getAll(pageable);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("all/{lang}")
    ResponseEntity<Page<EDocTypeRefEntityRefLang>> getAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @PathVariable("lang") String lang, @RequestParam(defaultValue = "DESC") String orderDirection) {
        Page<EDocTypeRefEntityRefLang> response = eDocumentTypeService.getAll(page, size, lang, orderDirection);
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("deleteList")
    @ResponseBody
    public ResponseEntity deleteList(@RequestBody ListOfObject listOfObject) {
        ErrorResponse response = eDocumentTypeService.deleteList(listOfObject);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @DeleteMapping("deleteInternationalisation/{id}/{lang}")
    @ResponseBody
    public ResponseEntity deleteInternationalisation(@PathVariable("id") Long id, @PathVariable("lang") String lang) {
        ErrorResponse response = eDocumentTypeService.deleteInternationalisation(lang, id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/add")
    public EDocTypeRefEntityRefLang addCountryRef(@RequestBody EDocTypeRefEntityRefLang eDocTypeRefEntityRefLang) {
        eDocumentTypeService.addEDocType(eDocTypeRefEntityRefLang);
        return eDocTypeRefEntityRefLang;
    }

    @PostMapping("/addInternationalisation/{lang}")
    public EntityRefLang addInternationalisation(@PathVariable("lang") String lang, @RequestBody EntityRefLang entityRefLang) {
        eDocumentTypeService.addInternationalisation(entityRefLang,lang);
        return entityRefLang;
    }

    @DeleteMapping("delete/{id}")
    @ResponseBody
    public ResponseEntity deleteCountryRef(@PathVariable("id") Long id) {
        ErrorResponse response = eDocumentTypeService.delete(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }


    @GetMapping("/find/{lang}/{id}")
    public EDocTypeRefEntityRefLang findCountry(@PathVariable("lang") String lang, @PathVariable("id") Long id) {
        return eDocumentTypeService.findEDocumentTypeRefLang(id, lang);
    }

    @PostMapping("/import")
    public ResponseEntity<String> importFile(@RequestParam("file") MultipartFile file) {

        importDataService.setService(eDocumentTypeService);
        return importDataService.importFile(file);
    }


    @GetMapping("/download/{lang}")
    @ResponseBody
    public ResponseEntity<InputStreamResource> getFile(@PathVariable("lang") String lang) throws IOException {
        String filename = "edocumentType.xlsx";
        InputStreamResource file = new InputStreamResource(eDocumentTypeService.load(lang));
        MediaType excelMediaType = new MediaType("application", "vnd.ms-excel");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }


    @GetMapping("/getAllByReferenceOrLabel/{value}/{lang}")
    public ResponseEntity< Page<EDocTypeRefEntityRefLangProjection> > getAllByReferenceOrLabel(@PathVariable("value") String value, @PathVariable("lang") String lang, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size)  {
        return ResponseEntity.ok().body( eDocumentTypeService.filterByReferenceOrLabel(value , PageRequest.of(page,size) , lang));
    }

    @GetMapping("find/detail/{lang}/{id}")
    public EDocTypeRefEntityRefLang findTaxationDetailedDetailed(@PathVariable("lang") String lang, @PathVariable("id") Long id) {
        return eDocumentTypeService.findeDocumentType(id, lang);
    }

}
