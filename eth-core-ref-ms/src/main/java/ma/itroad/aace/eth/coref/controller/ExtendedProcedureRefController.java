package ma.itroad.aace.eth.coref.controller;

import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ChapterRefBean;
import ma.itroad.aace.eth.coref.model.bean.ExtendedProcedureRefBean;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.entity.ChapterRef;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.entity.ExtendedProcedureRef;
import ma.itroad.aace.eth.coref.model.mapper.ExtendedProcedureRefMapper;
import ma.itroad.aace.eth.coref.repository.ExtendedProcedureRefRepository;
import ma.itroad.aace.eth.coref.repository.NationalProcedureRefRepository;
import ma.itroad.aace.eth.coref.service.IExtendedProcedureRefService;
import ma.itroad.aace.eth.coref.service.helper.ExtendedProcedureRefLang;
import ma.itroad.aace.eth.coref.service.helper.LangEntityRefLang;
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

import javax.validation.Valid;
import java.io.IOException;

@RestController
@RequestMapping(value = "/extendedProcedureRefs", consumes = {
		MediaType.ALL_VALUE }, produces = MediaTypes.HAL_JSON_VALUE)
public class ExtendedProcedureRefController {

	@Autowired
	IExtendedProcedureRefService extendedprocedurerefService;

	@Autowired
	ExtendedProcedureRefMapper extendedprocedurerefMapper;

	@Autowired
	ImportDataService importDataService;


	@Autowired
	private ExtendedProcedureRefRepository extendedProcedureRefRepository;

	@Autowired
	private NationalProcedureRefRepository nationalProcedureRefRepository;


	@GetMapping("all")
	ResponseEntity<Page<ExtendedProcedureRefBean>> getAll(@RequestParam(defaultValue = "0") int page,
												@RequestParam(defaultValue = "10") int size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<ExtendedProcedureRefBean> response = extendedprocedurerefService.getAll(pageable);
		return ResponseEntity.ok().body(response);
	}

	@GetMapping("all/{lang}")
	ResponseEntity<Page<ExtendedProcedureRefLang>> getAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @PathVariable("lang") String lang, @RequestParam(defaultValue = "DESC") String orderDirection) {
		Page<ExtendedProcedureRefLang> response = extendedprocedurerefService.getAll(page, size, lang, orderDirection);
		return ResponseEntity.ok().body(response);
	}

	@PostMapping("/import")
	public ResponseEntity<String> importFile(@RequestParam("file") MultipartFile file) {

		importDataService.setService(extendedprocedurerefService);
		return importDataService.importFile(file);
	}

	@GetMapping("/download/{lang}")
	@ResponseBody
	public ResponseEntity<InputStreamResource> getFile(@PathVariable("lang") String lang) throws IOException {
		String filename = "extendedprocedurerefs.xlsx";
		InputStreamResource file = new InputStreamResource(extendedprocedurerefService.load(lang));
		MediaType excelMediaType = new MediaType("application", "vnd.ms-excel");
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
				.contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(file);
	}

	@GetMapping("/download")
	@ResponseBody
	public ResponseEntity<InputStreamResource> getFile() throws IOException {
		String filename = "extendedprocedurerefs.xlsx";
		InputStreamResource file = new InputStreamResource(extendedprocedurerefService.load());
		MediaType excelMediaType = new MediaType("application", "vnd.ms-excel");
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
				.contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(file);

	}

	@DeleteMapping("delete/{id}")
	@ResponseBody
	public ResponseEntity deleteExtendedProcedure(@PathVariable("id") Long id) {
		ErrorResponse response = extendedprocedurerefService.delete(id);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	@DeleteMapping("deleteInternationalisation/{id}/{lang}")
	@ResponseBody
	public ResponseEntity deleteInternationalisation(@PathVariable("id") Long id, @PathVariable("lang") String lang) {
		ErrorResponse response = extendedprocedurerefService.deleteInternationalisation(lang, id);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	@PostMapping("/add")
	public ExtendedProcedureRefLang addExtendedProcedure(@RequestBody ExtendedProcedureRefLang extendedProcedureRefLang) {
		extendedprocedurerefService.addExtendedProcedure(extendedProcedureRefLang);
		return extendedProcedureRefLang;
	}

	@PostMapping("/addInternationalisation/{lang}")
	public EntityRefLang addInternationalisation(@PathVariable("lang") String lang,@RequestBody EntityRefLang entityRefLang) {
		extendedprocedurerefService.addInternationalisation(entityRefLang,lang);
		return entityRefLang;
	}

	@GetMapping("/find/{lang}/{id}")
	public ExtendedProcedureRefLang findExtendedProcedure(@PathVariable("lang") String lang, @PathVariable("id") Long id) {
		return extendedprocedurerefService.findExtendedProcedure(id, lang);
	}

	@PutMapping("/edit/{id}")
	public ResponseEntity<ExtendedProcedureRef> updateExtendedProcedure(@PathVariable(value = "id") Long extendedProcedureRefId,
															  @Valid @RequestBody ExtendedProcedureRef extendedProcedureRefs) throws ResourceNotFoundException {
		ExtendedProcedureRef extendedProcedureRef = extendedProcedureRefRepository.findById(extendedProcedureRefId)
				.orElseThrow(() -> new ResourceNotFoundException("extendedProcedureRef not found for this id :: " + extendedProcedureRefId));

		extendedProcedureRef.setCode(extendedProcedureRefs.getCode());
		extendedProcedureRef.setNationalProcedureRef(nationalProcedureRefRepository.findByCode(extendedProcedureRefs.getNationalProcedureRef().getCode()));



		final ExtendedProcedureRef updateExtendedProcedure = extendedProcedureRefRepository.save(extendedProcedureRef);
		return ResponseEntity.ok(updateExtendedProcedure);
	}


	@GetMapping("/getAllByCodeOrLabel/{value}/{lang}")
	public ResponseEntity getAllByCodeOrLabel(@PathVariable("value") String value, @PathVariable("lang") String lang, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size)  {
		if (value.equals(" ") || value == null|| value.equals(""))
			return getAll(page, size, lang, "ASC");
		else
			return ResponseEntity.ok().body( extendedprocedurerefService.filterByCodeOrLabelProjection(value , PageRequest.of(page,size) , lang));
	}

	@DeleteMapping("/deleteList")
	@ResponseBody
	public ResponseEntity deleteList(@RequestBody ListOfObject listOfObject) {
		ErrorResponse response = extendedprocedurerefService.deleteList(listOfObject);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	@PostMapping("/import-valid")
	public ResponseEntity<?> importValidFile(@RequestParam("file") MultipartFile file) {
		return extendedprocedurerefService.saveFromExcelWithReturn(file);
	}

}