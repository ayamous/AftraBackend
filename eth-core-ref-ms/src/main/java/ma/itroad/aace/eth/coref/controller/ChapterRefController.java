package ma.itroad.aace.eth.coref.controller;

import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ChapterRefBean;
import ma.itroad.aace.eth.coref.model.bean.CityRefBean;
import ma.itroad.aace.eth.coref.model.bean.CountryRefBean;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.entity.ChapterRef;
import ma.itroad.aace.eth.coref.model.entity.CityRef;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.mapper.ChapterRefMapper;
import ma.itroad.aace.eth.coref.repository.ChapterRefRepository;
import ma.itroad.aace.eth.coref.repository.SectionRefRepository;
import ma.itroad.aace.eth.coref.service.IChapterRefService;
import ma.itroad.aace.eth.coref.service.helper.ChapterRefLang;
import ma.itroad.aace.eth.coref.service.helper.CityRefEntityRefLang;
import ma.itroad.aace.eth.coref.service.helper.TechnicalBarrierRefLang;
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
@RequestMapping(value = "/chapterRefs", consumes = { MediaType.ALL_VALUE }, produces = MediaTypes.HAL_JSON_VALUE)
@ResponseBody
public class ChapterRefController {

	@Autowired
	IChapterRefService chapterRefService;

	@Autowired
	ChapterRefMapper chapterrefMapper;

	@Autowired
	ImportDataService importDataService;

	@Autowired
	private ChapterRefRepository chapterRefRepository;

	@Autowired
	private SectionRefRepository sectionRefRepository;

	@GetMapping("all")
	ResponseEntity<Page<ChapterRefBean>> getAll(@RequestParam(defaultValue = "0") int page,
												@RequestParam(defaultValue = "10") int size,
												@RequestParam(defaultValue = "DESC") String orderDirection) {
		Pageable pageable = PageRequest.of(page, size);
		Page<ChapterRefBean> response = chapterRefService.getAll(pageable);
		return ResponseEntity.ok().body(response);
	}

	@GetMapping("all/{lang}")
	ResponseEntity<Page<ChapterRefLang>> getAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @PathVariable("lang") String lang,  @RequestParam(defaultValue = "DESC") String orderDirection) {
		Page<ChapterRefLang> response = chapterRefService.getAll(page, size, lang, orderDirection);
		return ResponseEntity.ok().body(response);
	}


	@PostMapping("/import")
	public ResponseEntity<String> importFile(@RequestParam("file") MultipartFile file) {

		importDataService.setService(chapterRefService);
		return importDataService.importFile(file);
	}

	@GetMapping("/download/{lang}")
	@ResponseBody
	public ResponseEntity<InputStreamResource> getFile(@PathVariable("lang") String lang) throws IOException {
		String filename = "chapterRefs.xlsx";
		InputStreamResource file = new InputStreamResource(chapterRefService.load(lang));
		MediaType excelMediaType = new MediaType("application", "vnd.ms-excel");
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
				.contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(file);
	}

	@GetMapping("/download")
	@ResponseBody
	public ResponseEntity<InputStreamResource> getFile() throws IOException {
		String filename = "chapterRefs.xlsx";
		InputStreamResource file = new InputStreamResource(chapterRefService.load());
		MediaType excelMediaType = new MediaType("application", "vnd.ms-excel");
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
				.contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(file);

	}

	@DeleteMapping("delete/{id}")
	@ResponseBody
	public ResponseEntity deleteChapter(@PathVariable("id") Long id) {
		ErrorResponse response = chapterRefService.delete(id);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	@DeleteMapping("/deleteList")
	@ResponseBody
	public ResponseEntity deleteList(@RequestBody ListOfObject listOfObject) {
		ErrorResponse response = chapterRefService.deleteList(listOfObject);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	@DeleteMapping("deleteInternationalisation/{id}/{lang}")
	@ResponseBody
	public ResponseEntity deleteInternationalisation(@PathVariable("id") Long id, @PathVariable("lang") String lang) {
		ErrorResponse response = chapterRefService.deleteInternationalisation(lang, id);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	@PostMapping("/add")
	public ChapterRefLang addChapter(@RequestBody ChapterRefLang chapterRefLang) {
		chapterRefService.addChapter(chapterRefLang);
		return chapterRefLang;
	}

	@GetMapping("/find/{lang}/{id}")
	public ChapterRefLang findChapter(@PathVariable("lang") String lang, @PathVariable("id") Long id) {
		return chapterRefService.findChapter(id, lang);
	}

	@PostMapping("/addInternationalisation/{lang}")
	public EntityRefLang addInternationalisation(@PathVariable("lang") String lang,@RequestBody EntityRefLang entityRefLang) {
		chapterRefService.addInternationalisation(entityRefLang,lang);
		return entityRefLang;
	}

	@PutMapping("/edit/{id}")
	public ResponseEntity<ChapterRef> updateChapter(@PathVariable(value = "id") Long chapterRefId,
											  @Valid @RequestBody ChapterRef chapterRefs) throws ResourceNotFoundException {
		ChapterRef chapterRef = chapterRefRepository.findById(chapterRefId)
				.orElseThrow(() -> new ResourceNotFoundException("chapterRef not found for this id :: " + chapterRefId));

		chapterRef.setCode(chapterRefs.getCode());
		chapterRef.setSectionRef(sectionRefRepository.findByCode(chapterRefs.getSectionRef().getCode()));


		final ChapterRef updateChapter = chapterRefRepository.save(chapterRef);
		return ResponseEntity.ok(updateChapter);
	}

	@GetMapping("/getAllByCodeOrLabel/{value}/{lang}")
	public ResponseEntity getAllByCodeOrLabel(@PathVariable("value") String value, @PathVariable("lang") String lang, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size)  {
		if (value.equals(" ") || value == null|| value.equals(""))
			return getAll(page, size, lang, "ASC");
		else
			return ResponseEntity.ok().body( chapterRefService.filterByCodeOrLabelProjection(value , PageRequest.of(page,size), lang));
//		return ResponseEntity.ok().body( chapterRefService.filterByCodeOrLabel(value , page,size, lang));
	}

//	@GetMapping("/{id}")
//	public ResponseEntity getById(@PathVariable("id") Long id){
//		return ResponseEntity.ok().body(chapterRefService.findById(id));
//	}

	@PostMapping("/import-valid")
	public ResponseEntity<?> importValidFile(@RequestParam("file") MultipartFile file) {

		return chapterRefService.saveFromExcelWithReturn(file);
	}

}
