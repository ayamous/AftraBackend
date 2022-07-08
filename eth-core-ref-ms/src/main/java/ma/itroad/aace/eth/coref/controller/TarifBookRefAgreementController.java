package ma.itroad.aace.eth.coref.controller;

import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ListOfObjectTarifBook;
import ma.itroad.aace.eth.coref.model.bean.TariffBookRefBean;
import ma.itroad.aace.eth.coref.model.mapper.TarifBookAgreementVMProjection;
import ma.itroad.aace.eth.coref.model.view.TarifBookAgreementVM;
import ma.itroad.aace.eth.coref.model.view.TarifBookTaxationVM;
import ma.itroad.aace.eth.coref.service.ITariffBookRefAgreementJoinService;
import ma.itroad.aace.eth.coref.service.impl.ImportDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping(value = "/tarifBookAgreement", consumes = { MediaType.ALL_VALUE }, produces = MediaTypes.HAL_JSON_VALUE)
@ResponseBody
public class TarifBookRefAgreementController {
    private final static String fileName = "tarif-book-agreement-join.xlsx";


    @Autowired
    private ITariffBookRefAgreementJoinService iTariffBookRefAgreementJoinService;

    @Autowired
    private ImportDataService importDataService;



    @GetMapping("/all/{lang}")
    ResponseEntity<Page<TarifBookAgreementVM>> getAll(
            @PathVariable("lang") String lang,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "DESC") String orderDirection) {
        Page<TarifBookAgreementVM> response = iTariffBookRefAgreementJoinService.getAll(page, size, lang);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/save")
    public ResponseEntity<TarifBookAgreementVM> save(@RequestBody TarifBookAgreementVM tarifBookAgreementVM) {
        TarifBookAgreementVM response = iTariffBookRefAgreementJoinService.save(tarifBookAgreementVM);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/download")
    @ResponseBody
    public ResponseEntity<InputStreamResource> getFile() throws IOException {
        InputStreamResource file = new InputStreamResource(iTariffBookRefAgreementJoinService.load());
        MediaType excelMediaType = new MediaType("application", "vnd.ms-excel");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; fileName=" + fileName)
                .contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    @PostMapping("/import")
    public ResponseEntity<String> importFile(@RequestParam("file") MultipartFile file) {

        importDataService.setService(iTariffBookRefAgreementJoinService);
        return importDataService.importFile(file);
    }
    
    @PostMapping("/import-valid")
    public ResponseEntity<?> importValidFile(@RequestParam("file") MultipartFile file) {
        
        return iTariffBookRefAgreementJoinService.saveFromExcelAfterValidation(file);
    }


    @GetMapping("/searchByFilter")
    public ResponseEntity<Page<TarifBookAgreementVM>> findByFilter(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam String reference){
        Page<TarifBookAgreementVM> response = iTariffBookRefAgreementJoinService.findTarifTaxation(page,size, reference);
        return ResponseEntity.ok().body(response);
    }
/*
    @GetMapping("all/{lang}")
    ResponseEntity<Page<TarifBookAgreementVM>> getAll(@PathVariable("lang") String lang,
                                                     @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "DESC") String orderDirection) {
        Page<TarifBookAgreementVM> response = iTariffBookRefAgreementJoinService.getAll(lang, page, size, orderDirection);
        return ResponseEntity.ok().body(response);
    }

     */
    @GetMapping("/download/{lang}")
    @ResponseBody
    public ResponseEntity<InputStreamResource> getFile(@PathVariable("lang") String lang, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) throws IOException {
        InputStreamResource file = new InputStreamResource(iTariffBookRefAgreementJoinService.load(lang, page, size));
        MediaType excelMediaType = new MediaType("application", "vnd.ms-excel");
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; fileName=" + fileName)
                .contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }


    @GetMapping("/find/{id}/{lang}")
    public Page<TarifBookAgreementVM> findTarifBookTaxationVMByLang(@PathVariable("lang") String lang,
                                                                   @PathVariable("id") Long id, @RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "10") int size) {
        return iTariffBookRefAgreementJoinService.findTarifTaxationJoin(id, lang, page, size);
    }

    @DeleteMapping("/delete/{id}/{tarif_id}")
    @ResponseBody
    public ResponseEntity delete(@PathVariable("id") Long id, @PathVariable("tarif_id") Long tarif_id) {
        ErrorResponse response = iTariffBookRefAgreementJoinService.delete(id, tarif_id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @DeleteMapping("/deleteList")
    @ResponseBody
    public ResponseEntity deleteList(@RequestBody ListOfObjectTarifBook listOfObjectTarifBook) {
        ErrorResponse response = iTariffBookRefAgreementJoinService.deleteList(listOfObjectTarifBook);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
