package ma.itroad.aace.eth.coref.controller;

import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.CustomsOfficeRefBean;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.mapper.CustomsOfficeRefMapper;
import ma.itroad.aace.eth.coref.service.ICustomsOfficeRefService;
import ma.itroad.aace.eth.coref.service.helper.AgreementTypeLang;
import ma.itroad.aace.eth.coref.service.helper.CustomsOfficeRefLang;
import ma.itroad.aace.eth.coref.service.impl.ImportDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping(value = "/customsOfficeRefs", consumes = { MediaType.ALL_VALUE }, produces = MediaTypes.HAL_JSON_VALUE)
@ResponseBody
public class CustomsOfficeRefController {

    @Autowired
    ICustomsOfficeRefService customsOfficeRefService;

    @Autowired
    CustomsOfficeRefMapper customsofficerefMapper;

    @Autowired
    ImportDataService importDataService;

    @GetMapping("all")
    ResponseEntity<Page<CustomsOfficeRefBean>> getAll(@RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size,
													  @RequestParam(defaultValue = "DESC") String orderDirection) {
        Page<CustomsOfficeRefBean> response = customsOfficeRefService.getAll(page, size);
        return ResponseEntity.ok().body(response);
    }
    
	@GetMapping("all/{lang}")
	ResponseEntity<Page<CustomsOfficeRefLang>> getAll(@PathVariable("lang") String lang, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,  @RequestParam(defaultValue = "DESC") String orderDirection) {

		Page<CustomsOfficeRefLang> response = customsOfficeRefService.getAll(page, size, lang,orderDirection);
		return ResponseEntity.ok().body(response);

	}



    @PostMapping("/import")
    public ResponseEntity<String> importFile(@RequestParam("file") MultipartFile file) {

        importDataService.setService(customsOfficeRefService);
        return importDataService.importFile(file);
    }

    
    @PostMapping("/import-valid")
    public ResponseEntity<?> importValidFile(@RequestParam("file") MultipartFile file) {
        
        return customsOfficeRefService.saveFromExcelAfterValidation(file);
    }

    @GetMapping("/download")
    @ResponseBody
    public ResponseEntity<InputStreamResource> getFile() throws IOException {
        String filename = "customsOfficeRefs.xlsx";
        InputStreamResource file = new InputStreamResource(customsOfficeRefService.load());
        MediaType excelMediaType = new MediaType("application", "vnd.ms-excel");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);


    }
    
	@GetMapping("/download/{lang}")
	@ResponseBody
	public ResponseEntity<InputStreamResource> getFile(@PathVariable("lang") String lang) throws IOException {
		String filename = "customsOfficeRefs.xlsx";
		InputStreamResource file = new InputStreamResource(customsOfficeRefService.load(lang));
		MediaType excelMediaType = new MediaType("application", "vnd.ms-excel");
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
				.contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(file);
	}


	@DeleteMapping("/deleteList")
	@ResponseBody
	public ResponseEntity deleteList(@RequestBody ListOfObject listOfObject) {
		ErrorResponse response = customsOfficeRefService.deleteList(listOfObject);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	@PostMapping("/add")
	public CustomsOfficeRefBean add(@RequestBody CustomsOfficeRefLang customsOfficeRefLang) {
		return customsOfficeRefService.addInternationalisation(customsOfficeRefLang);
	}

	@GetMapping("/find/{lang}/{id}")
	public CustomsOfficeRefLang findChapter(@PathVariable("lang") String lang, @PathVariable("id") Long id) {
		return customsOfficeRefService.findCustomsOffice(id, lang);
	}

	@PostMapping("/add/{lang}")
	public EntityRefLang addInternationalisation(@PathVariable("lang") String lang, @RequestBody EntityRefLang entityRefLang) {
		return  customsOfficeRefService.addInternationalisation(entityRefLang, lang);
	}
	
    @DeleteMapping("delete/{id}")
    @ResponseBody
    public ResponseEntity deleteCustomOfficeRef(@PathVariable("id") Long id) {
        ErrorResponse response = customsOfficeRefService.delete(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    
    @DeleteMapping("delete/{id}/{lang}")
	@ResponseBody
	public ResponseEntity deleteCustomOfficeRef(@PathVariable("id") Long id, @PathVariable("lang") String lang) {
		ErrorResponse response = customsOfficeRefService.delete(id, lang);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	@GetMapping("/getAllByCodeOrLabel/{value}/{lang}")
	public ResponseEntity getAllByCodeOrLabel(@PathVariable("value") String value, @PathVariable("lang") String lang, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size)  {
		if (value.equals(" ") || value == null|| value.equals(""))
			return getAll(lang,page, size, "ASC");
		else
		return ResponseEntity.ok().body( customsOfficeRefService.filterByCodeOrLabelProjection(value , PageRequest.of(page,size) , lang));
	}


//	@GetMapping("/{id}")
//	public ResponseEntity getById(@PathVariable("id") Long id){
//		return ResponseEntity.ok().body(customsOfficeRefService.findById(id));
//	}


}
