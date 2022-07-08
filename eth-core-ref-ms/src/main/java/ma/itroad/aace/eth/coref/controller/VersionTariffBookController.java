package ma.itroad.aace.eth.coref.controller;

import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.VersionTariffBookBean;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.view.VersionTariffBookRefVM;
import ma.itroad.aace.eth.coref.service.IVersionTariffBookRefService;
import ma.itroad.aace.eth.coref.service.helper.VersionTarrifBookRefLang;
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
@RequestMapping(value = "/versionTariffBookRef", consumes = {
		MediaType.ALL_VALUE }, produces = MediaTypes.HAL_JSON_VALUE)
public class VersionTariffBookController {

	private final static String fileName = "versionTariffBookRef.xlsx";

	@Autowired
	IVersionTariffBookRefService service;

	@Autowired
	ImportDataService importDataService;

	@GetMapping("/all")
	ResponseEntity<Page<VersionTariffBookRefVM>> getAll(@RequestParam(defaultValue = "0") int page,
													@RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "DESC") String orderDirection) {
		Page<VersionTariffBookRefVM> response = service.getAll(page, size);
		return ResponseEntity.ok().body(response);
	}


	@PostMapping("/save")
	public ResponseEntity<VersionTariffBookBean> save(@RequestBody VersionTariffBookRefVM versionTariffBookRefVM) {
		VersionTariffBookBean response = service.add(versionTariffBookRefVM);
		return ResponseEntity.ok().body(response);
	}

	@GetMapping("all/{lang}")
	ResponseEntity<Page<VersionTariffBookBean>> getAll(@PathVariable("lang") String lang, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "DESC") String orderDirection) {
		Page<VersionTariffBookBean> response = service.getAll(lang, page, size, orderDirection);
		return ResponseEntity.ok().body(response);
	}

	@PostMapping("/import")
	public ResponseEntity<String> importFile(@RequestParam("file") MultipartFile file) {
		importDataService.setService(service);
		return importDataService.importFile(file);
	}
    @PostMapping("/import-valid")
    public ResponseEntity<?> importValidFile(@RequestParam("file") MultipartFile file) {
        
        return service.saveFromExcelAfterValidation(file);
    }
	
	@GetMapping("/download")
	@ResponseBody
	public ResponseEntity<InputStreamResource> getFile() throws IOException {
		InputStreamResource file = new InputStreamResource(service.load());
		MediaType excelMediaType = new MediaType("application", "vnd.ms-excel");
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
				.contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(file);

	}

	@GetMapping("/download/{lang}")
	@ResponseBody
	public ResponseEntity<InputStreamResource> getFile(@PathVariable("lang") String lang, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) throws IOException {
		InputStreamResource file = new InputStreamResource(service.load(lang, page, size));
		MediaType excelMediaType = new MediaType("application", "vnd.ms-excel");
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; fileName=" + fileName)
				.contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(file);
	}

	@PostMapping("/add/{lang}")
	public void addInternationalisation(@PathVariable("lang") String lang, @RequestBody EntityRefLang entityRefLang) {
		service.addInternationalisation(entityRefLang, lang);
	}

	@GetMapping("/find/{lang}/{id}")
	public VersionTarrifBookRefLang findCountryGroup(@PathVariable("lang") String lang, @PathVariable("id") Long id) {
		return service.findVersionTariffBook(id, lang);
	}

	@PostMapping("/add")
	public VersionTariffBookBean addInternationalisation(@RequestBody VersionTarrifBookRefLang versionTarrifBookRefLang) {
		return service.addInternationalisation(versionTarrifBookRefLang);
	}
	
	@DeleteMapping("delete/{id}")
	@ResponseBody
	public ResponseEntity deletePortRef(@PathVariable("id") Long id) {
		ErrorResponse response = service.delete(id);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

//	@GetMapping("/{id}")
//	public ResponseEntity getById(@PathVariable("id") Long id){
//		return ResponseEntity.ok().body(service.findById(id));
//	}

}