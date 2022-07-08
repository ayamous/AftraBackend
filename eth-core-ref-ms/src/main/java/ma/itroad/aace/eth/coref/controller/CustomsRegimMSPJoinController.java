package ma.itroad.aace.eth.coref.controller;

import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.CustomsRegimRefBean;
import ma.itroad.aace.eth.coref.model.bean.ListOfObjectTarifBook;
import ma.itroad.aace.eth.coref.model.view.CustomsRegimMSPVM;
import ma.itroad.aace.eth.coref.service.ICustomsRegimMSPJoinService;
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
import java.util.List;

@RestController
@RequestMapping("/customsRegimeMSPJoin")
public class CustomsRegimMSPJoinController {

	private final static String fileName = "customs-regime-MSP-join.xlsx";

	@Autowired
	ICustomsRegimMSPJoinService iCustomsRegimMSPJoinService;

	@Autowired
	ImportDataService importDataService;

	@GetMapping("all/{lang}")
	ResponseEntity<Page<CustomsRegimMSPVM>> getAll(
			@PathVariable("lang") String lang,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "DESC") String orderDirection) {
		Page<CustomsRegimMSPVM> response = iCustomsRegimMSPJoinService.getAll(page, size, lang);
		return ResponseEntity.ok().body(response);
	}

	@PostMapping("/save")
	public ResponseEntity<CustomsRegimMSPVM> save(@RequestBody CustomsRegimMSPVM customsRegimMSPVM) {
		CustomsRegimMSPVM response = iCustomsRegimMSPJoinService.save(customsRegimMSPVM);
		return ResponseEntity.ok().body(response);
	}

	@GetMapping("/download")
	@ResponseBody
	public ResponseEntity<InputStreamResource> getFile() throws IOException {
		InputStreamResource file = new InputStreamResource(iCustomsRegimMSPJoinService.load());
		MediaType excelMediaType = new MediaType("application", "vnd.ms-excel");
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; fileName=" + fileName)
				.contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(file);
	}

	@PostMapping("/import")
	public ResponseEntity<String> importFile(@RequestParam("file") MultipartFile file) {

		importDataService.setService(iCustomsRegimMSPJoinService);
		return importDataService.importFile(file);
	}
	
    @PostMapping("/import-valid")
    public ResponseEntity<?> importValidFile(@RequestParam("file") MultipartFile file) {
        
        return iCustomsRegimMSPJoinService.saveFromExcelAfterValidation(file);
    }

	@GetMapping("/searchByFilter")
	public ResponseEntity<Page<CustomsRegimMSPVM>> findByFilter(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size, @RequestParam String reference) {
		Page<CustomsRegimMSPVM> response = iCustomsRegimMSPJoinService.findCustomRegimeMspJoin(page, size, reference);
		return ResponseEntity.ok().body(response);
	}

	/*
	@GetMapping("all/{lang}")
	ResponseEntity<Page<CustomsRegimMSPVM>> getAll(@PathVariable("lang") String lang,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
												   @RequestParam(defaultValue = "DESC") String orderDirection) {
		Page<CustomsRegimMSPVM> response = iCustomsRegimMSPJoinService.getAll(lang, page, size, orderDirection);
		return ResponseEntity.ok().body(response);
	}*/

	@GetMapping("/download/{lang}")
	@ResponseBody
	public ResponseEntity<InputStreamResource> getFile(@PathVariable("lang") String lang) throws IOException {
		InputStreamResource file = new InputStreamResource(iCustomsRegimMSPJoinService.load(lang));
		MediaType excelMediaType = new MediaType("application", "vnd.ms-excel");
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; fileName=" + fileName)
				.contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(file);
	}

	@GetMapping("/find/{id}/{lang}")
	public Page<CustomsRegimMSPVM> findCustomsRegimMSPVMByLang(@PathVariable("lang") String lang,
			@PathVariable("id") Long id, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		return iCustomsRegimMSPJoinService.findCustomRegimeMspJoin(id, lang, page, size);
	}

	@DeleteMapping("/delete/{id}/{tarif_id}")
	@ResponseBody
	public ResponseEntity delete(@PathVariable("id") Long id, @PathVariable("tarif_id") Long tarif_id) {
		ErrorResponse response = iCustomsRegimMSPJoinService.delete(id, tarif_id);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	@DeleteMapping("/deleteList")
	@ResponseBody
	public ResponseEntity deleteList(@RequestBody ListOfObjectTarifBook listOfObjectTarifBook) {
		ErrorResponse response = iCustomsRegimMSPJoinService.deleteList(listOfObjectTarifBook);
		return ResponseEntity.status(response.getStatus()).body(response);
	}
}
