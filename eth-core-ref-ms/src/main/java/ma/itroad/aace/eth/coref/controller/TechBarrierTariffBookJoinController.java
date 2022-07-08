package ma.itroad.aace.eth.coref.controller;

import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ListOfObjectTarifBook;
import ma.itroad.aace.eth.coref.model.view.CountryGroupReferenceVM;
import ma.itroad.aace.eth.coref.model.view.TechBarrierTariffBookVM;
import ma.itroad.aace.eth.coref.service.ITechBarrierTariffBookJoinService;
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
@RequestMapping(value = "/techBarrierTariffBookRefJoin", consumes = {MediaType.ALL_VALUE}, produces = MediaTypes.HAL_JSON_VALUE)
public class TechBarrierTariffBookJoinController {

    private final static String fileName = "techBarrierTariffBookRefJoin.xlsx";


    @Autowired
    ITechBarrierTariffBookJoinService service;

    @Autowired
    ImportDataService importDataService;

    @PostMapping("/save")
    public TechBarrierTariffBookVM save(@RequestBody TechBarrierTariffBookVM techBarrierTariffBookVM) {
         return service.save(techBarrierTariffBookVM);
    }

    @GetMapping("all/{lang}")
    ResponseEntity<Page<TechBarrierTariffBookVM>> getAll(
            @PathVariable("lang") String lang,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Page<TechBarrierTariffBookVM> response = service.getAll(page, size, lang);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("download")
    @ResponseBody
    public ResponseEntity<InputStreamResource> getFile() throws IOException {
        InputStreamResource file = new InputStreamResource(service.load());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; fileName=" + fileName)
                .contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    @PostMapping("import")
    public ResponseEntity<String> importFile(@RequestParam("file") MultipartFile file) {

        importDataService.setService(service);
        return importDataService.importFile(file);
    }
    
    @PostMapping("/import-valid")
    public ResponseEntity<?> importValidFile(@RequestParam("file") MultipartFile file) {
        
        return service.saveFromExcelAfterValidation(file);
    }

    @GetMapping("/searchByFilter")
    public ResponseEntity<Page<TechBarrierTariffBookVM>> findByFilter(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam String reference){
        Page<TechBarrierTariffBookVM> response = service.findTechBarTarifBookJoin(page,size, reference);
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/delete/{id}/{tarif_id}")
    @ResponseBody
    public ResponseEntity delete(@PathVariable("id") Long id, @PathVariable("tarif_id") Long tarif_id) {
        ErrorResponse response = service.delete(id, tarif_id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @DeleteMapping("/deleteList")
    @ResponseBody
    public ResponseEntity deleteList(@RequestBody ListOfObjectTarifBook listOfObjectTarifBook) {
        ErrorResponse response = service.deleteList(listOfObjectTarifBook);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

}
