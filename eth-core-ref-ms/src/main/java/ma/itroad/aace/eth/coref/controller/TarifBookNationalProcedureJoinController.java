package ma.itroad.aace.eth.coref.controller;

import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ListOfObjectTarifBook;
import ma.itroad.aace.eth.coref.model.bean.TariffBookRefBean;
import ma.itroad.aace.eth.coref.model.mapper.projections.TarifBookNationalProcedureVMProjection;
import ma.itroad.aace.eth.coref.model.view.TarifBookNationalProcedureVM;
import ma.itroad.aace.eth.coref.service.ITarifBookNationalProcedureJoinService;
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
@RequestMapping("/tarifBookNationalProcedure")
public class TarifBookNationalProcedureJoinController {

    @Autowired
    private ITarifBookNationalProcedureJoinService iTarifBookNationalProcedureJoinService;

    @Autowired
    private ImportDataService importDataService;

    @GetMapping("/download/{lang}")
    @ResponseBody
    public ResponseEntity<InputStreamResource> getFile(@PathVariable("lang") String lang, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) throws IOException {
        String filename = "tarifBookRefs_NationalProcedure.xlsx";
        InputStreamResource file = new InputStreamResource(iTarifBookNationalProcedureJoinService.load(lang, page, size));
        MediaType excelMediaType = new MediaType("application", "vnd.ms-excel");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    @GetMapping("/download")
    @ResponseBody
    public ResponseEntity<InputStreamResource> getFile() throws IOException {
        String filename = "tarifBookRefs_NationalProcedure.xlsx";
        InputStreamResource file = new InputStreamResource(iTarifBookNationalProcedureJoinService.load());
        MediaType excelMediaType = new MediaType("application", "vnd.ms-excel");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; fileName=" + filename)
                .contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    @GetMapping("/all/{lang}")
    ResponseEntity<Page<TarifBookNationalProcedureVM>> getAll(
            @PathVariable("lang") String lang,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "DESC") String orderDirection) {
        Page<TarifBookNationalProcedureVM> response = iTarifBookNationalProcedureJoinService.getAll(page, size, lang);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/import")
    public ResponseEntity<String> importFile(@RequestParam("file") MultipartFile file) {

        importDataService.setService(iTarifBookNationalProcedureJoinService);
        return importDataService.importFile(file);
    }
    
    @PostMapping("/import-valid")
    public ResponseEntity<?> importValidFile(@RequestParam("file") MultipartFile file) {
        
        return iTarifBookNationalProcedureJoinService.saveFromExcelAfterValidation(file);
    }

    /*
    @GetMapping("all/{lang}")
    ResponseEntity<Page<TarifBookNationalProcedureVM>> getAll(@PathVariable("lang") String lang,
                                                     @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "DESC") String orderDirection) {
        Page<TarifBookNationalProcedureVM> response = iTarifBookNationalProcedureJoinService.getAll(lang, page, size, orderDirection);
        return ResponseEntity.ok().body(response);
    }
    */

    @GetMapping("/find/{id}/{lang}")
    public Page<TarifBookNationalProcedureVM> findTarifBookNationalProcedureVMByLang(@PathVariable("lang") String lang,
                                                                   @PathVariable("id") Long id, @RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "10") int size) {
        return iTarifBookNationalProcedureJoinService.findTarifBookNationalProcedureVMByLang(id, lang, page, size);
    }

    @GetMapping("/searchByFilter")
    public ResponseEntity<Page<TarifBookNationalProcedureVM>> findTarifBookNationalProcedure(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam String reference){
        Page<TarifBookNationalProcedureVM> response = iTarifBookNationalProcedureJoinService.findTarifBookNationalProcedure(page,size, reference);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/save")
    public ResponseEntity<TariffBookRefBean> save(@RequestBody TarifBookNationalProcedureVM tarifBookNationalProcedureVM) {
        TariffBookRefBean response = iTarifBookNationalProcedureJoinService.save(tarifBookNationalProcedureVM);
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/delete/{id}/{tarif_id}")
    @ResponseBody
    public ResponseEntity delete(@PathVariable("id") Long id, @PathVariable("tarif_id") Long tarif_id) {
        ErrorResponse response = iTarifBookNationalProcedureJoinService.delete(id, tarif_id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @DeleteMapping("/deleteList")
    @ResponseBody
    public ResponseEntity deleteList(@RequestBody ListOfObjectTarifBook listOfObjectTarifBook) {
        ErrorResponse response = iTarifBookNationalProcedureJoinService.deleteList(listOfObjectTarifBook);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
