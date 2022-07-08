package ma.itroad.aace.eth.coref.controller;

import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
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

import ma.itroad.aace.eth.coref.model.bean.PortRefBean;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.mapper.PortRefMapper;
import ma.itroad.aace.eth.coref.service.IPortRefService;
import ma.itroad.aace.eth.coref.service.helper.PortRefLang;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping(value = "/portRefs", consumes = { MediaType.ALL_VALUE }, produces = MediaTypes.HAL_JSON_VALUE)
@ResponseBody
public class PortRefController {

	@Autowired
	IPortRefService portRefService;

	@Autowired
	ImportDataService importDataService;

	@Autowired
	PortRefMapper portrefMapper;


	@GetMapping("all")
	ResponseEntity<Page<PortRefBean>> getAll(@RequestParam(defaultValue = "0") int page,
												@RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "DESC") String orderDirection) {
		Pageable pageable = PageRequest.of(page, size);
		Page<PortRefBean> response = portRefService.getAll(pageable);
		return ResponseEntity.ok().body(response);
	}

	@GetMapping("all/{lang}")
	ResponseEntity<Page<PortRefLang>> getAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @PathVariable("lang") String lang, @RequestParam(defaultValue = "DESC") String orderDirection) {
		Page<PortRefLang> response = portRefService.getAll(page, size, lang, orderDirection);
		return ResponseEntity.ok().body(response);
	}

	
	@PostMapping("/import")
	public ResponseEntity<String> importFile(@RequestParam("file") MultipartFile file) {

		importDataService.setService(portRefService);
		return importDataService.importFile(file);
	}
	
    @PostMapping("/import-valid")
    public ResponseEntity<?> importValidFile(@RequestParam("file") MultipartFile file) {
        
        return portRefService.saveFromExcelAfterValidation(file);
    }


	@GetMapping("/download")
	@ResponseBody
	public ResponseEntity<InputStreamResource> getFile() throws IOException {
		String filename = "PortRefs.xlsx";
		InputStreamResource file = new InputStreamResource(portRefService.load());
		MediaType excelMediaType = new MediaType("application", "vnd.ms-excel");
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
				.contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(file);

	}

	@GetMapping("/download/{lang}")
	@ResponseBody
	public ResponseEntity<InputStreamResource> getFile(@PathVariable("lang") String lang) throws IOException {
		String filename = "PortRefs.xlsx";
		InputStreamResource file = new InputStreamResource(portRefService.load(lang));
		MediaType excelMediaType = new MediaType("application", "vnd.ms-excel");
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
				.contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(file);
	}

	@GetMapping("/find/{lang}/{id}")
	public PortRefLang findPort(@PathVariable("lang") String lang, @PathVariable("id") Long id) {
		return portRefService.findPort(id, lang);
	}

	@PostMapping("/addInternationalisation/{lang}")
	public EntityRefLang addInternationalisation(@PathVariable("lang") String lang,@RequestBody EntityRefLang entityRefLang) {
		portRefService.addInternationalisation(entityRefLang,lang);
		return entityRefLang;
	}

	@PostMapping("/add")
	public PortRefLang addPort(@RequestBody PortRefLang portRefLang) {
		portRefService.addPort(portRefLang);
		return portRefLang;
	}

	@DeleteMapping("delete/{id}")
	@ResponseBody
	public ResponseEntity deletePort(@PathVariable("id") Long id) {
		ErrorResponse response = portRefService.delete(id);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	@DeleteMapping("/deleteList")
	@ResponseBody
	public ResponseEntity deleteList(@RequestBody ListOfObject listOfObject) {
		ErrorResponse response = portRefService.deleteList(listOfObject);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	@DeleteMapping("deleteInternationalisation/{id}/{lang}")
	@ResponseBody
	public ResponseEntity deleteInternationalisation(@PathVariable("id") Long id, @PathVariable("lang") String lang) {
		ErrorResponse response = portRefService.deleteInternationalisation(lang, id);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	@GetMapping("/getAllByCodeOrLabel/{value}/{lang}")
	public ResponseEntity getAllByCodeOrLabel(@PathVariable("value") String value, @PathVariable("lang") String lang, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size)  {
		if (value.equals(" ") || value == null|| value.equals(""))
			return getAll(page, size, lang, "ASC");
		else
		return ResponseEntity.ok().body( portRefService.filterByCodeOrLabelProjection(value , PageRequest.of(page,size) , lang));
	}

//	@GetMapping("/{id}")
//	public ResponseEntity getById(@PathVariable("id") Long id){
//		return ResponseEntity.ok().body(portRefService.findById(id));
//	}
}
