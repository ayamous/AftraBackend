package ma.itroad.aace.eth.coref.controller;

import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.bean.ListOfObjectTarifBook;
import ma.itroad.aace.eth.coref.model.mapper.RegulationTariffBookRefVMProjection;
import ma.itroad.aace.eth.coref.model.view.RegulationTariffBookRefVM;
import ma.itroad.aace.eth.coref.service.IRegulationTariffBookJoinService;
import ma.itroad.aace.eth.coref.service.impl.ImportDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/regulationTariffBookJoin")
public class RegulationTariffBookJoinController {
    private final static String fileName = "regulation-position-tarifaire.xlsx";

    @Autowired
    private ImportDataService importDataService;

    @Autowired
    private IRegulationTariffBookJoinService iregulationTariffBookJoinService;

    @GetMapping("/all/{lang}")
    ResponseEntity<Page<RegulationTariffBookRefVM>> getAll(
            @PathVariable("lang") String lang,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "DESC") String orderDirection) {
        Page<RegulationTariffBookRefVM> response = iregulationTariffBookJoinService.getAll(page, size, lang);
        return ResponseEntity.ok().body(response);
    }
    @PostMapping("/save")
    public ResponseEntity<RegulationTariffBookRefVM> save(@RequestBody RegulationTariffBookRefVM regulationTariffBookRefVM) {
        RegulationTariffBookRefVM response = iregulationTariffBookJoinService.save(regulationTariffBookRefVM);
        return ResponseEntity.ok().body(response);
    }
    @PostMapping("/import")
    public ResponseEntity<String> importFile(@RequestParam("file") MultipartFile file) {
        importDataService.setService(iregulationTariffBookJoinService);
        return importDataService.importFile(file);
    }
    
    @PostMapping("/import-valid")
    public ResponseEntity<?> importValidFile(@RequestParam("file") MultipartFile file) {
        
        return iregulationTariffBookJoinService.saveFromExcelAfterValidation(file);
    }

    @GetMapping("/download")
    @ResponseBody
    public ResponseEntity<InputStreamResource> getFile() throws IOException {
        InputStreamResource file = new InputStreamResource(iregulationTariffBookJoinService.load());
        MediaType excelMediaType = new MediaType("application", "vnd.ms-excel");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; fileName=" + fileName)
                .contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    @GetMapping("/searchByFilter")
    public ResponseEntity<Page<RegulationTariffBookRefVM>> findByFilter(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam String reference){
        Page<RegulationTariffBookRefVM> response = iregulationTariffBookJoinService.findRegulationTarifBookJoin(page,size, reference);
        return ResponseEntity.ok().body(response);
    }

    /*
    @GetMapping("all/{lang}")
    ResponseEntity<Page<RegulationTariffBookRefVM>> getAll(@PathVariable("lang") String lang,
                                                    @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "DESC") String orderDirection) {
        Page<RegulationTariffBookRefVM> response = iregulationTariffBookJoinService.getAll(lang, page, size, orderDirection);
        return ResponseEntity.ok().body(response);
    }*/

    @GetMapping("/download/{lang}")
    @ResponseBody
    public ResponseEntity<InputStreamResource> getFile(@PathVariable("lang") String lang) throws IOException {
        InputStreamResource file = new InputStreamResource(iregulationTariffBookJoinService.load(lang));
        MediaType excelMediaType = new MediaType("application", "vnd.ms-excel");
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; fileName=" + fileName)
                .contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    @GetMapping("/find/{id}/{lang}")
    public Page<RegulationTariffBookRefVM> findByLang(@PathVariable("lang") String lang,
                                                                @PathVariable("id") Long id, @RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "10") int size) {
        return iregulationTariffBookJoinService.findRegulationTarifBookJoin(id, lang, page, size);
    }

    @DeleteMapping("/delete/{id}/{tarif_id}")
    @ResponseBody
    public ResponseEntity delete(@PathVariable("id") Long id, @PathVariable("tarif_id") Long tarif_id) {
        ErrorResponse response = iregulationTariffBookJoinService.delete(id, tarif_id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @DeleteMapping("/deleteList")
    @ResponseBody
    public ResponseEntity deleteList(@RequestBody ListOfObjectTarifBook listOfObjectTarifBook) {
        ErrorResponse response = iregulationTariffBookJoinService.deleteList(listOfObjectTarifBook);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
