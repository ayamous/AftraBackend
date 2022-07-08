package ma.itroad.aace.eth.coref.controller;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;


import ma.itroad.aace.eth.coref.model.bean.ChapterRefBean;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.bean.NationalProcedureRefBean;
import ma.itroad.aace.eth.coref.model.bean.TechBarrierRefBean;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.mapper.TechBarrierRefMapper;
import ma.itroad.aace.eth.coref.model.mapper.projections.TechnicalBarrierRefLangProjection;
import ma.itroad.aace.eth.coref.model.view.*;
import ma.itroad.aace.eth.coref.repository.TechBarrierRefRepository;
import ma.itroad.aace.eth.coref.service.ITechBarrierRefService;
import ma.itroad.aace.eth.coref.service.helper.AgreementLang;
import ma.itroad.aace.eth.coref.service.helper.NationalProcedureRefLang;
import ma.itroad.aace.eth.coref.service.helper.PortRefLang;
import ma.itroad.aace.eth.coref.service.helper.TechnicalBarrierRefLang;
import ma.itroad.aace.eth.coref.service.helper.detailed.RegulationRefLangDetailed;
import ma.itroad.aace.eth.coref.service.helper.detailed.TechnicalBarrierRefLangDetailed;
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
@RequestMapping(value = "/techBarrierRefs", consumes = {MediaType.ALL_VALUE}, produces = MediaTypes.HAL_JSON_VALUE)
public class TechBarrierRefController {

    @Autowired
    ITechBarrierRefService service;

    @Autowired
    TechBarrierRefRepository techBarrierRefRepository ;

    @Autowired
    TechBarrierRefMapper techBarrierRefMapper ;

    @Autowired
    ImportDataService importDataService;


    @GetMapping("/findById/{id}")
    public ResponseEntity<TechBarrierRefBean> findById(@PathVariable Long id) {
        return ResponseEntity.ok()
                .body(techBarrierRefMapper.entityToBean(techBarrierRefRepository.findOneById(id)));
    }

    @GetMapping("all")
    ResponseEntity<Page<TechBarrierRefBean>> getAll(@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "DESC") String orderDirection) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TechBarrierRefBean> response = service.getAll(pageable);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("all/{lang}")
    ResponseEntity<Page<TechnicalBarrierRefLang>> getAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @PathVariable("lang") String lang, @RequestParam(defaultValue = "DESC") String orderDirection) {
        Page<TechnicalBarrierRefLang> response = service.getAll(page, size, lang, orderDirection);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/download")
    @ResponseBody
    public ResponseEntity<InputStreamResource> getFile() throws IOException {
        String filename = "techBarrierRefs.xlsx";
        InputStreamResource file = new InputStreamResource(service.load());
        MediaType excelMediaType = new MediaType("application", "vnd.ms-excel");
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    @GetMapping("/download/{lang}")
    @ResponseBody
    public ResponseEntity<InputStreamResource> getFile(@PathVariable("lang") String lang) throws IOException {
        String filename = "techBarrierRefs.xlsx";
        InputStreamResource file = new InputStreamResource(service.load(lang));
        MediaType excelMediaType = new MediaType("application", "vnd.ms-excel");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }


    @PostMapping("/import")
    public ResponseEntity<String> importFile(@RequestParam("file") MultipartFile file) {

        importDataService.setService(service);
        return importDataService.importFile(file);
    }

    @PostMapping("/productInformationFinderfilter/{lang}")
    public ResponseEntity<Page<TechnicalBarrierRefLang>> techBarFilterByTarifBook(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "DESC") String orderDirection,
            @RequestBody ProductInformationFinderFilterPayload productInformationFinderFilterPayload,@PathVariable("lang") String lang) {
        Page<TechnicalBarrierRefLang> response = service
                .techBarFilterByTarifBook(productInformationFinderFilterPayload, page, size, lang, orderDirection);
        return ResponseEntity.ok().body(response);
    }


    @PostMapping("/filter/{lang}")
    public ResponseEntity<Page<TechnicalBarrierRefLang>> techBarFilter(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size ,
                                                                       @RequestParam(defaultValue = "DESC") String orderDirection,
                                                                       @RequestBody MspAndBarriersFilterPayload mspAndBarriersFilterPayload,
                                                                       @PathVariable("lang") String lang) {
        Page<TechnicalBarrierRefLang> response  = service.techBarFilter( mspAndBarriersFilterPayload , page , size, lang, orderDirection);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/subPortal/filter/{lang}")
    public ResponseEntity<Page<TechnicalBarrierRefLang>> filterForSubPortal(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "DESC") String orderDirection,
            @RequestParam(defaultValue = "10") int size ,
            @RequestBody MspAndBarriersFilterPayload subPortalMspAndBarrsFilterPayload,
                                                                            @PathVariable String lang) {
        Page<TechnicalBarrierRefLang> response  = service.techBarFilter(subPortalMspAndBarrsFilterPayload , page , size, lang, orderDirection) ;
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("delete/{id}")
    @ResponseBody
    public ResponseEntity deleteTechBarrierRef(@PathVariable("id") Long id) {
        ErrorResponse response = service.delete(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @DeleteMapping("/deleteList")
    @ResponseBody
    public ResponseEntity deleteList(@RequestBody ListOfObject listOfObject) {
        ErrorResponse response = service.deleteList(listOfObject);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/subPortal/productInformationFinderfilter/{lang}")
    public ResponseEntity<Page<TechnicalBarrierRefLang>> subPortalProductInformationFinderfilter(@RequestParam(defaultValue = "0") int page,
                                                                                                 @RequestParam(defaultValue = "10") int size,
                                                                                                 @RequestParam(defaultValue = "DESC") String orderDirection,
                                                                                                 @RequestBody SubportalProductInformationFinderFilterPayload payload,
                                                                                            @PathVariable String lang){
        Page<TechnicalBarrierRefLang> response=service.subPortalProductInformationFinderfilter(payload, page,size, lang, orderDirection);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/add")
    public TechnicalBarrierRefLang addTechnicalBarrier(@RequestBody TechnicalBarrierRefLang technicalBarrierRefLang) {
        service.addTechnicalBarrier(technicalBarrierRefLang);
        return technicalBarrierRefLang;
    }

    @GetMapping("/find/{lang}/{id}")
    public TechnicalBarrierRefLang findTechnicalBarrier(@PathVariable("lang") String lang, @PathVariable("id") Long id) {
        return service.findTechnicalBarrier(id, lang);
    }

    @PostMapping("/addInternationalisation/{lang}")
    public EntityRefLang addInternationalisation(@PathVariable("lang") String lang,@RequestBody EntityRefLang entityRefLang) {
        service.addInternationalisation(entityRefLang,lang);
        return entityRefLang;
    }


    @DeleteMapping("deleteInternationalisation/{id}/{lang}")
    @ResponseBody
    public ResponseEntity deleteInternationalisation(@PathVariable("id") Long id, @PathVariable("lang") String lang) {
        ErrorResponse response = service.deleteInternationalisation(lang, id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/getAllByCodeOrLabel/{value}/{lang}")
    public ResponseEntity< ? > getAllByCodeOrLabel(@PathVariable("value") String value, @PathVariable("lang") String lang, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size)  {
//        return ResponseEntity.ok().body( service.filterByCodeOrLabel(value , PageRequest.of(page,size) , lang));
        if (value.equals(" ") || value == null|| value.equals(""))
            return getAll(page, size, lang, "ASC");
        else
        return ResponseEntity.ok().body( service.filterByCodeOrLabelProjection(value , lang,PageRequest.of(page,size)));

    }

    @GetMapping("/find/detail/{lang}/{id}")
    public TechnicalBarrierRefLangDetailed findTechnicalBarrierDetailedDetailed(@PathVariable("lang") String lang, @PathVariable("id") Long id) {
        return service.findTechnicalBarrierDetailedDetailed(service.findTechnicalBarrier(id, lang)) ;
    }

//    @GetMapping("/{id}")
//    public ResponseEntity getById(@PathVariable("id") Long id){
//        return ResponseEntity.ok().body(service.findById(id));
//    }
}
