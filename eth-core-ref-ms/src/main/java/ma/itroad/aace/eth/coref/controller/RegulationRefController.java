package ma.itroad.aace.eth.coref.controller;
 
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.view.*;
import ma.itroad.aace.eth.coref.service.helper.RegulationRefLang;
import ma.itroad.aace.eth.coref.service.helper.detailed.AgreementLangDetailed;
import ma.itroad.aace.eth.coref.service.helper.detailed.RegulationRefLangDetailed;
import ma.itroad.aace.eth.coref.service.impl.ImportDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ma.itroad.aace.eth.coref.model.bean.RegulationRefBean;
import ma.itroad.aace.eth.coref.model.mapper.RegulationRefMapper;
import ma.itroad.aace.eth.coref.service.IRegulationRefService;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping(value = "/regulationrefs", consumes = { MediaType.ALL_VALUE }, produces = MediaTypes.HAL_JSON_VALUE)
@ResponseBody
public class RegulationRefController {

    @Autowired
    IRegulationRefService regulationrefService;

    @Autowired
    RegulationRefMapper regulationrefMapper;

    @Autowired
    ImportDataService importDataService;

    @GetMapping("all")
    ResponseEntity<Page<RegulationRefBean>> getAll(@RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "DESC") String orderDirection) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RegulationRefBean> response = regulationrefService.getAll(pageable);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("all/{lang}")
    ResponseEntity<Page<RegulationRefLang>> getAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @PathVariable("lang") String lang, @RequestParam(defaultValue = "DESC") String orderDirection) {
        Page<RegulationRefLang> response = regulationrefService.getAll(page, size, lang, orderDirection);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/import")
    public ResponseEntity<String> importFile(@RequestParam("file") MultipartFile file) {

        importDataService.setService(regulationrefService);
        return importDataService.importFile(file);
    }
    
    @PostMapping("/import-valid")
    public ResponseEntity<?> importValidFile(@RequestParam("file") MultipartFile file) {
        
        return regulationrefService.saveFromExcelAfterValidation(file);
    }

    @GetMapping("/download")
    @ResponseBody
    public ResponseEntity<InputStreamResource> getFile() throws IOException {
        String filename = "RegulationRef.xlsx";
        InputStreamResource file = new InputStreamResource(regulationrefService.load());
        MediaType excelMediaType = new MediaType("application", "vnd.ms-excel");
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);

    }

    @GetMapping("/download/{lang}")
    @ResponseBody
    public ResponseEntity<InputStreamResource> getFile(@PathVariable("lang") String lang) throws IOException {
        String filename = "RegulationRef.xlsx";
        InputStreamResource file = new InputStreamResource(regulationrefService.load(lang));
        MediaType excelMediaType = new MediaType("application", "vnd.ms-excel");
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    @GetMapping("/find/{lang}/{id}")
    public RegulationRefLang findRegulationRef(@PathVariable("lang") String lang, @PathVariable("id") Long id) {
        return regulationrefService.findRegulationRef(id, lang);
    }

    @PostMapping("/addInternationalisation/{lang}")
    public EntityRefLang addInternationalisation(@PathVariable("lang") String lang, @RequestBody EntityRefLang entityRefLang) {
        regulationrefService.addInternationalisation(entityRefLang,lang);
        return entityRefLang;
    }

    @PostMapping("/add")
    public RegulationRefLang addRegulationRef(@RequestBody RegulationRefLang regulationRefLang) {
        regulationrefService.addRegulationRef(regulationRefLang);
        return regulationRefLang;
    }

    @DeleteMapping("delete/{id}")
    @ResponseBody
    public ResponseEntity deleteRegulationRef(@PathVariable("id") Long id) {
        ErrorResponse response = regulationrefService.delete(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @DeleteMapping("/deleteList")
    @ResponseBody
    public ResponseEntity deleteList(@RequestBody ListOfObject listOfObject) {
        ErrorResponse response = regulationrefService.deleteList(listOfObject);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @DeleteMapping("deleteInternationalisation/{id}/{lang}")
    @ResponseBody
    public ResponseEntity deleteInternationalisation(@PathVariable("id") Long id, @PathVariable("lang") String lang) {
        ErrorResponse response = regulationrefService.deleteInternationalisation(lang, id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/getAllByCodeOrLabel/{value}/{lang}")
    public ResponseEntity getAllByCodeOrLabel(@PathVariable("value") String value, @PathVariable("lang") String lang, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size)  {
        if (value.equals(" ") || value == null|| value.equals(""))
            return getAll(page, size, lang, "ASC");
        else
        return ResponseEntity.ok().body( regulationrefService.filterByCodeOrLabelProjection(value , PageRequest.of(page,size) , lang));
    }

    @GetMapping
    public Page<RegulationRefBean> getAllRegulationRefs(@RequestParam(defaultValue = "0") Integer page,
                                              @RequestParam(defaultValue = "10") Integer size){
        Sort.Order order = new Sort.Order(Sort.Direction.DESC, "id");
        return regulationrefService.getAll(PageRequest.of(page, size,  Sort.by(order)));
    }

    @PostMapping("/filter/{lang}")
    public ResponseEntity<Page<RegulationRefLang>> filterForPortal(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "DESC") String orderDirection,
            @RequestBody MspAndBarriersFilterPayload mspAndBarriersFilterPayload,
            @PathVariable("lang") String lang) {
        Page<RegulationRefLang> response = regulationrefService.filterForPortal(mspAndBarriersFilterPayload, page, size, lang, orderDirection);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/productInformationFinderfilter/{lang}")
    public ResponseEntity<Page<RegulationRefLang>> productInformationFinder(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "DESC") String orderDirection,
            @RequestBody ProductInformationFinderFilterPayload productInformationFinderFilterPayload, @PathVariable("lang") String lang) {
        Page<RegulationRefLang> response = regulationrefService.filterForProductInformationFinder(productInformationFinderFilterPayload, page, size, lang, orderDirection);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/subPortal/filter/{lang}")
    public ResponseEntity<Page<RegulationRefLang>> filterForSubPortal(
        @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "DESC") String orderDirection,
        @RequestBody MspAndBarriersFilterPayload subPortalMspAndBarrsFilterPayload,
        @PathVariable("lang") String lang) {
            Page<RegulationRefLang> response = regulationrefService.filterForPortal(subPortalMspAndBarrsFilterPayload, page, size, lang, orderDirection);
            return ResponseEntity.ok().body(response);
        }

    @PostMapping("/subPortal/productInformationFinderfilter/{lang}")
    public ResponseEntity<Page<RegulationRefLang>> subPortalProductInformationFinderfilter(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "DESC") String orderDirection,
            @RequestBody SubportalProductInformationFinderFilterPayload payload,
            @PathVariable("lang") String lang) {
        Page<RegulationRefLang> response = regulationrefService.subPortalProductInformationFinderfilter(payload, page, size, lang, orderDirection);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/find/detail/{lang}/{id}")
    public RegulationRefLangDetailed findRegulationDetailed(@PathVariable("lang") String lang, @PathVariable("id") Long id) {
        return regulationrefService.findRegulationDetailed(regulationrefService.findRegulationRef(id, lang)) ;
    }

//    @GetMapping("/{id}")
//    public ResponseEntity getById(@PathVariable("id") Long id){
//        return ResponseEntity.ok().body(regulationrefService.findById(id));
//    }
}