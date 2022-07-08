package ma.itroad.aace.eth.coref.controller;

import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.AgreementBean;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.entity.Agreement;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.mapper.AgreementMapper;
import ma.itroad.aace.eth.coref.model.mapper.projections.AgreementLangProjection;
import ma.itroad.aace.eth.coref.model.view.ProductInformationFinderFilterPayload;
import ma.itroad.aace.eth.coref.model.view.SubportalProductInformationFinderFilterPayload;
import ma.itroad.aace.eth.coref.model.view.TradeAgreementFilterPayload;
import ma.itroad.aace.eth.coref.repository.AgreementRepository;
import ma.itroad.aace.eth.coref.repository.AgreementTypeRepository;
import ma.itroad.aace.eth.coref.service.IAgreementService;
import ma.itroad.aace.eth.coref.service.helper.AgreementLang;
import ma.itroad.aace.eth.coref.service.helper.detailed.AgreementLangDetailed;
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
import ma.itroad.aace.eth.coref.repository.CountryGroupRefRepository;
import ma.itroad.aace.eth.coref.repository.CountryRefRepository;

import javax.validation.Valid;
import java.io.IOException;

@RestController
@RequestMapping(value = "/agreements", consumes = {MediaType.ALL_VALUE}, produces = MediaTypes.HAL_JSON_VALUE)
public class AgreementController {

    @Autowired
    IAgreementService agreementService;

    @Autowired
    AgreementRepository agreementRepository;

    @Autowired
    AgreementMapper agreementMapper;

    @Autowired
    ImportDataService importDataService;

    @Autowired
    CountryGroupRefRepository countryGroupRefRepository;

    @Autowired
    AgreementTypeRepository agreementTypeRepository;

    @Autowired
    CountryRefRepository countryRefRepository;


    @GetMapping("/all")
    ResponseEntity<Page<AgreementBean>> getAll(@RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size,
                                               @RequestParam(defaultValue = "DESC") String  orderDirection) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AgreementBean> response = agreementService.getAll(pageable);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("all/{lang}")
    ResponseEntity<Page<AgreementLang>> getAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @PathVariable("lang") String lang,@RequestParam(defaultValue = "DESC") String orderDirection) {
        Page<AgreementLang> response = agreementService.getAll(page, size, lang, orderDirection);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/import")
    public ResponseEntity<String> importFile(@RequestParam("file") MultipartFile file) {
        importDataService.setService(agreementService);
        return importDataService.importFile(file);
    }

    @GetMapping("/download/{lang}")
    @ResponseBody
    public ResponseEntity<InputStreamResource> getFile(@PathVariable("lang") String lang) throws IOException {
        String filename = "agreements.xlsx";
        InputStreamResource file = new InputStreamResource(agreementService.load(lang));
        MediaType excelMediaType = new MediaType("application", "vnd.ms-excel");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    @GetMapping("/download")
    @ResponseBody
    public ResponseEntity<InputStreamResource> getFile() throws IOException {
        String filename = "agreements.xlsx";
        InputStreamResource file = new InputStreamResource(agreementService.load());
        MediaType excelMediaType = new MediaType("application", "vnd.ms-excel");
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    @PostMapping("/productInformationFinderfilter/{lang}")
    public ResponseEntity<Page<AgreementLang>> filterForPortal(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "DESC") String orderDirection,
            @RequestBody ProductInformationFinderFilterPayload productInformationFinderFilterPayload,@PathVariable("lang") String lang) {
        Page<AgreementLang> response = agreementService
                .filterForPortal(productInformationFinderFilterPayload, page, size, lang, orderDirection);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/subPortal/productInformationFinderfilter/{lang}")
    public ResponseEntity<Page<AgreementLang>> subPortalProductInformationFinderfilter(@RequestParam(defaultValue = "0") int page,
                                                                                       @RequestParam(defaultValue = "10") int size,
                                                                                       @RequestParam(defaultValue = "DESC") String orderDirection,
                                                                                       @RequestBody SubportalProductInformationFinderFilterPayload payload,
                                                                                       @PathVariable("lang") String lang) {
        Page<AgreementLang> response = agreementService.subPortalProductInformationFinderfilter(page, size, payload, lang, orderDirection);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/tradeAgreementFilter/{lang}")
    public ResponseEntity<Page<AgreementLangProjection>> tradeAgreementFilter(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestBody TradeAgreementFilterPayload tradeAgreementFilterPayload, @PathVariable("lang") String lang) {
        Page<AgreementLangProjection> response = agreementService.tradeAgreementFilterProjection(tradeAgreementFilterPayload, page, size, lang);
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("delete/{id}")
    @ResponseBody
    public ResponseEntity deleteAgreement(@PathVariable("id") Long id) {

        ErrorResponse response = agreementService.delete(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @DeleteMapping("/deleteList")
    @ResponseBody
    public ResponseEntity deleteList(@RequestBody ListOfObject listOfObject) {
        ErrorResponse response = agreementService.deleteList(listOfObject);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/add")
    public AgreementLang addAgreement(@RequestBody AgreementLang agreementLang) {
        agreementService.addAgreement(agreementLang);
        return agreementLang;
    }

    @PostMapping("/addInternationalisation/{lang}")
    public EntityRefLang addInternationalisation(@PathVariable("lang") String lang,@RequestBody EntityRefLang entityRefLang) {
        agreementService.addInternationalisation(entityRefLang,lang);
        return entityRefLang;
    }


    @DeleteMapping("deleteInternationalisation/{id}/{lang}")
    @ResponseBody
    public ResponseEntity deleteInternationalisation(@PathVariable("id") Long id, @PathVariable("lang") String lang) {

        ErrorResponse response = agreementService.deleteInternationalisation(lang, id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }


    @PutMapping("/edit/{id}")
    public ResponseEntity<Agreement> updateAgreement(@PathVariable(value = "id") Long agreementId,
                                                     @Valid @RequestBody Agreement agreements) throws ResourceNotFoundException {
        Agreement agreement = agreementRepository.findById(agreementId)
                .orElseThrow(() -> new ResourceNotFoundException("Agreement not found for this id :: " + agreementId));

        agreement.setCode(agreements.getCode());
        agreement.setTitle(agreements.getTitle());
        agreement.setDateOfAgreement(agreements.getDateOfAgreement());
        agreement.setAgreementStatus(agreements.getAgreementStatus());
        agreement.setDescription(agreements.getDescription());

        agreement.setAgreementType(agreementTypeRepository.findByCode(agreements.getAgreementType().getCode()));
        agreement.setCountryGroupRef(countryGroupRefRepository.findByReference(agreements.getCountryGroupRef().getReference()));
        agreement.setCountryRef(countryRefRepository.findByReference(agreements.getCountryRef().getReference()));

        final Agreement updateAgreement = agreementService.save(agreement);
        return ResponseEntity.ok(updateAgreement);
    }

    @GetMapping("/find/{lang}/{id}")
    public AgreementLang findAgreement(@PathVariable("lang") String lang, @PathVariable("id") Long id) {
        return agreementService.findAgreement(id, lang);
    }

    @GetMapping("/getAllByCodeOrLabel/{value}/{lang}")
    public ResponseEntity getAllByCodeOrLabel(@PathVariable("value") String value, @PathVariable("lang") String lang, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        if (value.equals(" ") || value == null|| value.equals(""))
            return getAll(page, size, lang, "ASC");
        else
        return ResponseEntity.ok().body(agreementService.filterByCodeOrLabel(value, PageRequest.of(page, size), lang));
    }

     @GetMapping("/find/detail/{lang}/{id}")
    public AgreementLangDetailed findAgreementLangDetailed(@PathVariable("lang") String lang, @PathVariable("id") Long id) {
        return agreementService.findAgreementDetailed(agreementService.findAgreement(id, lang)) ;
    }

//    @GetMapping("/{id}")
//    public ResponseEntity getById(@PathVariable("id") Long id){
//        return ResponseEntity.ok().body(agreementService.findById(id));
//    }

    @PostMapping("/import-valid")
    public ResponseEntity<?> importValidFile(@RequestParam("file") MultipartFile file) {

        return agreementService.saveFromExcelWithReturn(file);
    }
}


guest_digital3
123+456