package ma.itroad.aace.eth.coref.controller;

import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.bean.NationalProcedureRefBean;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.mapper.NationalProcedureRefMapper;
import ma.itroad.aace.eth.coref.model.mapper.projections.NationalProcedureRefLangProjection;
import ma.itroad.aace.eth.coref.model.view.NationalProcedureRefVM;
import ma.itroad.aace.eth.coref.model.view.ProductInformationFinderFilterPayload;
import ma.itroad.aace.eth.coref.model.view.SubPortalNationalProcedureAndRegulationFilterPayload;
import ma.itroad.aace.eth.coref.model.view.SubportalProductInformationFinderFilterPayload;
import ma.itroad.aace.eth.coref.repository.NationalProcedureRefRepository;
import ma.itroad.aace.eth.coref.service.INationalProcedureRefService;
import ma.itroad.aace.eth.coref.service.helper.NationalProcedureRefLang;
import ma.itroad.aace.eth.coref.service.helper.PortRefLang;
import ma.itroad.aace.eth.coref.service.helper.TariffBookRefLang;
import ma.itroad.aace.eth.coref.service.helper.detailed.NationalProcedureRefLangDetailed;
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
@RequestMapping(value = "/nationalProcedureRefs", consumes = {
		MediaType.ALL_VALUE }, produces = MediaTypes.HAL_JSON_VALUE)
public class NationalProcedureRefController {

	@Autowired
	INationalProcedureRefService nationalprocedurerefService;
	@Autowired
	NationalProcedureRefMapper nationalprocedurerefMapper;
	@Autowired
	NationalProcedureRefRepository nationalProcedureRefRepository;
	@Autowired
	ImportDataService importDataService;

	@GetMapping("all")
	ResponseEntity<Page<NationalProcedureRefBean>> getAll(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "DESC") String orderDirection) {
		Page<NationalProcedureRefBean> response = nationalprocedurerefService.getAll(page, size);
		return ResponseEntity.ok().body(response);
	}

	@GetMapping("all/{lang}")
	ResponseEntity<Page<NationalProcedureRefLang>> getAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @PathVariable("lang") String lang, @RequestParam(defaultValue = "DESC") String orderDirection) {
		Page<NationalProcedureRefLang> response = nationalprocedurerefService.getAll(page, size, lang, orderDirection);
		return ResponseEntity.ok().body(response);
	}

	@PostMapping("/import")
	public ResponseEntity<String> importFile(@RequestParam("file") MultipartFile file) {

		importDataService.setService(nationalprocedurerefService);
		return importDataService.importFile(file);
	}

	@GetMapping("/findById/{id}")
	public ResponseEntity<NationalProcedureRefBean> findById(@PathVariable Long id) {
		return ResponseEntity.ok()
				.body(nationalprocedurerefMapper.entityToBean(nationalProcedureRefRepository.findOneById(id)));
	}

	@GetMapping("/download")
	@ResponseBody
	public ResponseEntity<InputStreamResource> getFile() throws IOException {
		String filename = "nationalprocedurerefs.xlsx";
		InputStreamResource file = new InputStreamResource(nationalprocedurerefService.load());
		MediaType excelMediaType = new MediaType("application", "vnd.ms-excel");
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
				.contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(file);
	}

	@GetMapping("/download/{lang}")
	@ResponseBody
	public ResponseEntity<InputStreamResource> getFile(@PathVariable("lang") String lang) throws IOException {
		String filename = "nationalprocedurerefs.xlsx";
		InputStreamResource file = new InputStreamResource(nationalprocedurerefService.load(lang));
		MediaType excelMediaType = new MediaType("application", "vnd.ms-excel");
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
				.contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(file);
	}

	@DeleteMapping("delete/{id}")
	@ResponseBody
	public ResponseEntity deleteNationalProcedure(@PathVariable("id") Long id) {
		ErrorResponse response = nationalprocedurerefService.delete(id);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	@DeleteMapping("/deleteList")
	@ResponseBody
	public ResponseEntity deleteList(@RequestBody ListOfObject listOfObject) {
		ErrorResponse response = nationalprocedurerefService.deleteList(listOfObject);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	@PostMapping("/add")
	public NationalProcedureRefLang addNationalProcedure(@RequestBody NationalProcedureRefLang nationalProcedureRefLang) {
		nationalprocedurerefService.addNationalProcedure(nationalProcedureRefLang);
		return nationalProcedureRefLang;
	}

	@GetMapping("/find/{lang}/{id}")
	public NationalProcedureRefLang findNationalProcedure(@PathVariable("lang") String lang, @PathVariable("id") Long id) {
		return nationalprocedurerefService.findNationalProcedure(id, lang);
	}

	@GetMapping("/find/detail/{lang}/{id}")
	public NationalProcedureRefLangDetailed findNationalProcedureDetailed(@PathVariable("lang") String lang, @PathVariable("id") Long id) {
		return nationalprocedurerefService.findNationalProcedureDetailed(nationalprocedurerefService.findNationalProcedure(id, lang)) ;
	}

	@PostMapping("/addInternationalisation/{lang}")
	public EntityRefLang addInternationalisation(@PathVariable("lang") String lang,@RequestBody EntityRefLang entityRefLang) {
		nationalprocedurerefService.addInternationalisation(entityRefLang,lang);
		return entityRefLang;
	}

	@PostMapping("/productInformationFinderfilter/{lang}")
	public ResponseEntity<Page<NationalProcedureRefLang>> filterForPortal(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "DESC") String orderDirection,
			@RequestBody ProductInformationFinderFilterPayload productInformationFinderFilterPayload, @PathVariable("lang")String lang) {
		Page<NationalProcedureRefLang> response = nationalprocedurerefService.filterForPortal(page, size, productInformationFinderFilterPayload, lang, orderDirection);
		return ResponseEntity.ok().body(response);
	}

	@PostMapping("/filter/{lang}")
	public ResponseEntity<Page<NationalProcedureRefLang>> techBarFilter(@RequestParam(defaultValue = "0") int page,
																		@RequestParam(defaultValue = "10") int size,
																		@RequestParam(defaultValue = "DESC") String orderDirection,
																		@RequestBody NationalProcedureRefVM nationalProcedureRefVM,
																		@PathVariable("lang") String lang) {
		Page<NationalProcedureRefLang> response = nationalprocedurerefService.nationalProcedureFilter(nationalProcedureRefVM, page, size, lang, orderDirection);
		return ResponseEntity.ok().body(response);
	}

	@PostMapping("/subPortal/filter/{lang}")
	public ResponseEntity<Page<NationalProcedureRefLang>> filterForSubPortal(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "DESC") String orderDirection,
			@RequestBody NationalProcedureRefVM subPortalNationalProcedureAndRegulationFilterPayload,
	        @PathVariable String lang) {
		Page<NationalProcedureRefLang> response = nationalprocedurerefService
				.nationalProcedureFilter(subPortalNationalProcedureAndRegulationFilterPayload, page, size, lang, orderDirection);
		return ResponseEntity.ok().body(response);
	}

	@PostMapping("/subPortal/productInformationFinderfilter/{lang}")
	public ResponseEntity<Page<NationalProcedureRefLang>> subPortalProductInformationFinderfilter(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "DESC") String orderDirection,
			@RequestBody SubportalProductInformationFinderFilterPayload subPortalNationalProcedureAndRegulationFilterPayload,
			@PathVariable String lang) {
		Page<NationalProcedureRefLang> response = nationalprocedurerefService
				.procedureSubportalProductInformationFinderFilter(subPortalNationalProcedureAndRegulationFilterPayload,
						page, size, lang, orderDirection);
		return ResponseEntity.ok().body(response);
	}

	@GetMapping("/getAllByCodeOrLabel/{value}/{lang}")
	public ResponseEntity< ? > getAllByCodeOrLabel(@PathVariable("value") String value, @PathVariable("lang") String lang, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size)  {
//		return ResponseEntity.ok().body( nationalprocedurerefService.filterByCodeOrLabel(value , PageRequest.of(page,size) , lang));
		if (value.equals(" ") || value == null|| value.equals(""))
			return getAll(page, size, lang, "ASC");
		else
		return ResponseEntity.ok().body(nationalprocedurerefService.filterByCodeOrLabelProjection(value,lang,page,size));
	}

//	@GetMapping("/{id}")
//	public ResponseEntity getById(@PathVariable("id") Long id){
//		return ResponseEntity.ok().body(nationalprocedurerefService.findById(id));
//	}

	@PostMapping("/import-valid")
	public ResponseEntity<?> importValidFile(@RequestParam("file") MultipartFile file) {

		return nationalprocedurerefService.saveFromExcelAfterValidation(file);
	}

}
