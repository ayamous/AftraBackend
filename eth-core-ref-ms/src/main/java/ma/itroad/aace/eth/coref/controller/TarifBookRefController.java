package ma.itroad.aace.eth.coref.controller;

import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ChapterRefBean;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.entity.AgreementType;
import ma.itroad.aace.eth.coref.model.mapper.projections.TariffBookRefLangProjection;
import ma.itroad.aace.eth.coref.repository.ChapterRefRepository;
import ma.itroad.aace.eth.coref.repository.TarifBookRefRepository;
import ma.itroad.aace.eth.coref.repository.UnitRefRepository;
import ma.itroad.aace.eth.coref.service.helper.UnitRefEntityRefLang;
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

import ma.itroad.aace.eth.coref.model.bean.SanitaryPhytosanitaryMeasuresRefBean;
import ma.itroad.aace.eth.coref.model.bean.TariffBookRefBean;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.entity.TarifBookRef;
import ma.itroad.aace.eth.coref.model.mapper.TariffBookRefMapper;
import ma.itroad.aace.eth.coref.service.ITarifBookRefService;
import ma.itroad.aace.eth.coref.service.helper.TariffBookRefLang;

import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;

@RestController
@RequestMapping(value = "/tarifBookRefs", consumes = { MediaType.ALL_VALUE }, produces = MediaTypes.HAL_JSON_VALUE)
public class TarifBookRefController {

	@Autowired
	ITarifBookRefService tariffbookrefService;

	@Autowired
	TariffBookRefMapper tariffbookrefMapper;

	@Autowired
	ImportDataService importDataService;

	@Autowired
	private TarifBookRefRepository tarifBookRefRepository;

	@Autowired
	private ChapterRefRepository chapterRefRepository;

	@Autowired
	private UnitRefRepository unitRefRepository;


	@GetMapping("all")
	ResponseEntity<Page<TariffBookRefBean>> getAll(@RequestParam(defaultValue = "0") int page,
												@RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "DESC") String orderDirection) {
		Pageable pageable = PageRequest.of(page, size);
		Page<TariffBookRefBean> response = tariffbookrefService.getAll(pageable);
		return ResponseEntity.ok().body(response);
	}

	@GetMapping("all/{lang}")
	ResponseEntity<Page<TariffBookRefLang>> getAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @PathVariable("lang") String lang, @RequestParam(defaultValue = "DESC") String orderDirection) {
		Page<TariffBookRefLang> response = tariffbookrefService.getAll(page, size, lang, orderDirection);
		return ResponseEntity.ok().body(response);
	}

	@PostMapping("/import")
	public ResponseEntity<String> importFile(@RequestParam("file") MultipartFile file) {
		importDataService.setService(tariffbookrefService);
		return importDataService.importFile(file);
	}

	@GetMapping("/download/{lang}")
	@ResponseBody
	public ResponseEntity<InputStreamResource> getFile(@PathVariable("lang") String lang, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "DESC") String orderDirection) throws IOException {
		String filename = "tarifBookrefs.xlsx";
		InputStreamResource file = new InputStreamResource(tariffbookrefService.load(lang, page, size, orderDirection));
		MediaType excelMediaType = new MediaType("application", "vnd.ms-excel");
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
				.contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(file);
	}

	@GetMapping("/download")
	@ResponseBody
	public ResponseEntity<InputStreamResource> getFile() throws IOException {
		String filename = "tarifBookrefs.xlsx";
		InputStreamResource file = new InputStreamResource(tariffbookrefService.load());
		MediaType excelMediaType = new MediaType("application", "vnd.ms-excel");
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
				.contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(file);

	}

//	@DeleteMapping("delete/{id}")
//	@ResponseBody
//	public ErrorResponse deleteTarifBookRef(@PathVariable("id") Long id) {
//		return tariffbookrefService.delete(id);
//	}

	@DeleteMapping("/deleteList")
	@ResponseBody
	public ResponseEntity deleteList(@RequestBody ListOfObject listOfObject) {
		ErrorResponse response = tariffbookrefService.deleteList(listOfObject);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	@PostMapping("/add")
	public TariffBookRefLang addTarifBookRef(@RequestBody TariffBookRefLang tarifBookRefLang) {
		tariffbookrefService.addTarifBookRef(tarifBookRefLang);
		return tarifBookRefLang;
	}


	@GetMapping("/find/{lang}/{id}")
	public TariffBookRefLang findTariffBook(@PathVariable("lang") String lang, @PathVariable("id") Long id) {
		return tariffbookrefService.findTariffBook(id, lang);
	}

	@PostMapping("/addInternationalisation/{lang}")
	public EntityRefLang addInternationalisation(@PathVariable("lang") String lang,@RequestBody EntityRefLang entityRefLang) {
		tariffbookrefService.addInternationalisation(entityRefLang,lang);
		return entityRefLang;
	}

	@DeleteMapping("deleteInternationalisation/{id}/{lang}")
	@ResponseBody
	public ResponseEntity deleteInternationalisation(@PathVariable("id") Long id, @PathVariable("lang") String lang) {
		ErrorResponse response = tariffbookrefService.deleteInternationalisation(lang, id);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	@DeleteMapping("delete/{id}")
	@ResponseBody
	public ResponseEntity deleteVersionRef(@PathVariable("id") Long id) {

		ErrorResponse response = tariffbookrefService.delete(id);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	@GetMapping("/getAllByReferenceOrLabel/{value}/{lang}")
	public ResponseEntity<?> getAllByReferenceOrLabel(@PathVariable("value") String value, @PathVariable("lang") String lang, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size)  {
//		return ResponseEntity.ok().body( tariffbookrefService.filterByReferenceOrLabel(value , PageRequest.of(page,size) , lang));
		if (value.equals(" ") || value == null|| value.equals(""))
			return getAll(page, size, lang, "ASC");
		else
		return ResponseEntity.ok().body( tariffbookrefService.filterByReferenceOrLabelProjection(value ,  lang,PageRequest.of(page,size) ));

	}

	@PostMapping("/import-valid")
	public ResponseEntity<?> importValidFile(@RequestParam("file") MultipartFile file) {

		return tariffbookrefService.saveFromExcelWithReturn(file);
	}

}
