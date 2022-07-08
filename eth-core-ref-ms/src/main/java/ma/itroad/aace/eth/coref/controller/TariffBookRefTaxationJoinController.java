package ma.itroad.aace.eth.coref.controller;


import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ListOfObjectTarifBook;
import ma.itroad.aace.eth.coref.model.bean.TariffBookRefBean;
import ma.itroad.aace.eth.coref.model.mapper.projections.TarifBookTaxationVMProjection;
import ma.itroad.aace.eth.coref.model.view.TarifBookTaxationVM;
import ma.itroad.aace.eth.coref.service.ITariffBookRefTaxationJoinService;
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
@RequestMapping("/tarifBookTaxation")
public class TariffBookRefTaxationJoinController {

    private final static String fileName = "tarifBookRefs_Taxations.xlsx";


    @Autowired
    private ITariffBookRefTaxationJoinService iTariffBookRefTaxationJoinService;

    @Autowired
    private ImportDataService importDataService;



    @GetMapping("/all/{lang}")
    ResponseEntity<Page<TarifBookTaxationVM>> getAll(
            @PathVariable("lang") String lang,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "DESC") String orderDirection) {
        Page<TarifBookTaxationVM> response = iTariffBookRefTaxationJoinService.getAll(page, size, lang);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/save")
    public ResponseEntity<TariffBookRefBean> save(@RequestBody TarifBookTaxationVM tarifBookTaxationVM) {
        TariffBookRefBean response = iTariffBookRefTaxationJoinService.save(tarifBookTaxationVM);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/download")
    @ResponseBody
    public ResponseEntity<InputStreamResource> getFile() throws IOException {
        InputStreamResource file = new InputStreamResource(iTariffBookRefTaxationJoinService.load());
        MediaType excelMediaType = new MediaType("application", "vnd.ms-excel");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; fileName=" + fileName)
                .contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    @PostMapping("/import")
    public ResponseEntity<String> importFile(@RequestParam("file") MultipartFile file) {

        importDataService.setService(iTariffBookRefTaxationJoinService);
        return importDataService.importFile(file);
    }
    
    @PostMapping("/import-valid")
    public ResponseEntity<?> importValidFile(@RequestParam("file") MultipartFile file) {
        
        return iTariffBookRefTaxationJoinService.saveFromExcelAfterValidation(file);
    }

    @GetMapping("/searchByFilter")
    public ResponseEntity<Page<TarifBookTaxationVMProjection>> findByFilter(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam String reference){
        Page<TarifBookTaxationVMProjection> response = iTariffBookRefTaxationJoinService.findTarifTaxation(page,size, reference);
        return ResponseEntity.ok().body(response);
    }

    /*
	@GetMapping("all/{lang}")
	ResponseEntity<Page<TarifBookTaxationVM>> getAll(@PathVariable("lang") String lang,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "DESC") String orderDirection) {
		Page<TarifBookTaxationVM> response = iTariffBookRefTaxationJoinService.getAll(lang, page, size, orderDirection);
		return ResponseEntity.ok().body(response);
	}
	*/


	@GetMapping("/download/{lang}")
	@ResponseBody
	public ResponseEntity<InputStreamResource> getFile(@PathVariable("lang") String lang, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) throws IOException {
		InputStreamResource file = new InputStreamResource(iTariffBookRefTaxationJoinService.load(lang, page, size));
		MediaType excelMediaType = new MediaType("application", "vnd.ms-excel");
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; fileName=" + fileName)
				.contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(file);
	}

	@GetMapping("/find/{id}/{lang}")
	public Page<TarifBookTaxationVM> findTarifBookTaxationVMByLang(@PathVariable("lang") String lang,
			@PathVariable("id") Long id, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		return iTariffBookRefTaxationJoinService.findTarifTaxationJoin(id, lang, page, size);
	}

    @DeleteMapping("/delete/{id}/{tarif_id}")
    @ResponseBody
    public ResponseEntity delete(@PathVariable("id") Long id, @PathVariable("tarif_id") Long tarif_id) {
        ErrorResponse response = iTariffBookRefTaxationJoinService.delete(id, tarif_id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @DeleteMapping("/deleteList")
    @ResponseBody
    public ResponseEntity deleteList(@RequestBody ListOfObjectTarifBook listOfObjectTarifBook) {
        ErrorResponse response = iTariffBookRefTaxationJoinService.deleteList(listOfObjectTarifBook);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
