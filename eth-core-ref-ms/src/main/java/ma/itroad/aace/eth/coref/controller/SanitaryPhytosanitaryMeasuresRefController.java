package ma.itroad.aace.eth.coref.controller;

import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.AgreementBean;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.bean.SanitaryPhytosanitaryMeasuresRefBean;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.entity.SanitaryPhytosanitaryMeasuresRef;
import ma.itroad.aace.eth.coref.model.mapper.SanitaryPhytosanitaryMeasuresRefMapper;
import ma.itroad.aace.eth.coref.model.mapper.projections.SanitaryPhytosanitaryMeasuresRefLangProjection;
import ma.itroad.aace.eth.coref.model.view.MspAndBarriersFilterPayload;
import ma.itroad.aace.eth.coref.model.view.ProductInformationFinderFilterPayload;
import ma.itroad.aace.eth.coref.model.view.SubPortalMspAndBarrsFilterPayload;
import ma.itroad.aace.eth.coref.model.view.SubportalProductInformationFinderFilterPayload;
import ma.itroad.aace.eth.coref.repository.SanitaryPhytosanitaryMeasuresRefRepository;
import ma.itroad.aace.eth.coref.repository.CountryRefRepository;
import ma.itroad.aace.eth.coref.service.ISanitaryPhytosanitaryMeasuresRefService;
import ma.itroad.aace.eth.coref.service.helper.CustomsOfficeRefLang;
import ma.itroad.aace.eth.coref.service.helper.SanitaryPhytosanitaryMeasuresRefLang;
import ma.itroad.aace.eth.coref.service.helper.detailed.NationalProcedureRefLangDetailed;
import ma.itroad.aace.eth.coref.service.helper.detailed.SanitaryPhytosanitaryMeasuresRefLangDetailed;
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
@RequestMapping(value = "/sanitaryPhytosanitaryMeasuresRefs", consumes = {
		MediaType.ALL_VALUE }, produces = MediaTypes.HAL_JSON_VALUE)
public class SanitaryPhytosanitaryMeasuresRefController {

	@Autowired
	ISanitaryPhytosanitaryMeasuresRefService sanitaryphytosanitarymeasuresrefService;

	@Autowired
	SanitaryPhytosanitaryMeasuresRefRepository sanitaryPhytosanitaryMeasuresRefRepository;

	@Autowired
	SanitaryPhytosanitaryMeasuresRefMapper sanitaryphytosanitarymeasuresrefMapper;

	@Autowired
	ImportDataService importDataService;

	@Autowired
	private CountryRefRepository countryRefRepository;

	@GetMapping("/findById/{id}")
	public ResponseEntity<SanitaryPhytosanitaryMeasuresRefBean> findById(@PathVariable Long id) {
		return ResponseEntity.ok().body(sanitaryphytosanitarymeasuresrefMapper
				.entityToBean(sanitaryPhytosanitaryMeasuresRefRepository.findOneById(id)));
	}

	@GetMapping("/all")
	ResponseEntity<Page<SanitaryPhytosanitaryMeasuresRefBean>> getAll(@RequestParam(defaultValue = "0") int page,
																	  @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "DESC") String orderDirection) {
		Pageable pageable = PageRequest.of(page, size);
		Page<SanitaryPhytosanitaryMeasuresRefBean> response = sanitaryphytosanitarymeasuresrefService.getAll(pageable);
		return ResponseEntity.ok().body(response);
	}


	@GetMapping("all/{lang}")
	ResponseEntity<Page<SanitaryPhytosanitaryMeasuresRefLang>> getAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @PathVariable("lang") String lang, @RequestParam(defaultValue = "DESC") String orderDirection) {
		Page<SanitaryPhytosanitaryMeasuresRefLang> response = sanitaryphytosanitarymeasuresrefService.getAll(page, size, lang, orderDirection);
		return ResponseEntity.ok().body(response);
	}

	@PostMapping("/import")
	public ResponseEntity<String> importFile(@RequestParam("file") MultipartFile file) {

		importDataService.setService(sanitaryphytosanitarymeasuresrefService);
		return importDataService.importFile(file);
	}

	@GetMapping("/download")
	@ResponseBody
	public ResponseEntity<InputStreamResource> getFile() throws IOException {
		String filename = "msps.xlsx";
		InputStreamResource file = new InputStreamResource(sanitaryphytosanitarymeasuresrefService.load());
		MediaType excelMediaType = new MediaType("application", "vnd.ms-excel");
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
				.contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(file);
	}

	@GetMapping("/download/{lang}")
	@ResponseBody
	public ResponseEntity<InputStreamResource> getFile(@PathVariable("lang") String lang) throws IOException {
		String filename = "msps.xlsx";
		InputStreamResource file = new InputStreamResource(sanitaryphytosanitarymeasuresrefService.load(lang));
		MediaType excelMediaType = new MediaType("application", "vnd.ms-excel");
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
				.contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(file);
	}

	@PostMapping("/add")
	public SanitaryPhytosanitaryMeasuresRefLang addSanitaryPhytosanitaryMeasuresRef(@RequestBody SanitaryPhytosanitaryMeasuresRefLang sanitaryPhytosanitaryMeasuresRefLang) {
		sanitaryphytosanitarymeasuresrefService.addSanitaryPhytosanitaryMeasuresRef(sanitaryPhytosanitaryMeasuresRefLang);
		return sanitaryPhytosanitaryMeasuresRefLang;
	}


	@PostMapping("/addInternationalisation/{lang}")
	public EntityRefLang addInternationalisation(@PathVariable("lang") String lang,@RequestBody EntityRefLang entityRefLang) {
		sanitaryphytosanitarymeasuresrefService.addInternationalisation(entityRefLang,lang);
		return entityRefLang;
	}

	@GetMapping("/find/{lang}/{id}")
	public SanitaryPhytosanitaryMeasuresRefLang findSanitaryPhytosanitaryMeasures(@PathVariable("lang") String lang, @PathVariable("id") Long id) {
		return sanitaryphytosanitarymeasuresrefService.findSanitaryPhytosanitaryMeasures(id, lang);
	}

	@GetMapping("/find/detail/{lang}/{id}")
	public SanitaryPhytosanitaryMeasuresRefLangDetailed findNationalProcedureDetailed(@PathVariable("lang") String lang, @PathVariable("id") Long id) {
		return sanitaryphytosanitarymeasuresrefService.findSanitaryPhytosanitaryMeasureDetailed(sanitaryphytosanitarymeasuresrefService.findSanitaryPhytosanitaryMeasures(id, lang)) ;
	}

	@PostMapping("/productInformationFinderfilter/{lang}")
	public ResponseEntity<Page<SanitaryPhytosanitaryMeasuresRefLang>> productInformationFinder(
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "DESC") String orderDirection,
			@RequestBody ProductInformationFinderFilterPayload productInformationFinderFilterPayload,@PathVariable("lang") String lang) {
		Page<SanitaryPhytosanitaryMeasuresRefLang> response = sanitaryphytosanitarymeasuresrefService
				.filterForProductInformationFinder(productInformationFinderFilterPayload, page, size, lang, orderDirection);
		return ResponseEntity.ok().body(response);
	}

	@PostMapping("/filter/{lang}")
	public ResponseEntity<Page<SanitaryPhytosanitaryMeasuresRefLang>> filterForPortal(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "DESC") String orderDirection,
			@RequestBody MspAndBarriersFilterPayload mspAndBarriersFilterPayload,
			@PathVariable("lang") String lang) {
		Page<SanitaryPhytosanitaryMeasuresRefLang> response = sanitaryphytosanitarymeasuresrefService.filterForPortal(mspAndBarriersFilterPayload, page, size, lang, orderDirection);
		return ResponseEntity.ok().body(response);
	}

	@PostMapping("/subPortal/filter/{lang}")
	public ResponseEntity<Page<SanitaryPhytosanitaryMeasuresRefLang>> filterForSubPortal(
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "DESC") String orderDirection,
			@RequestBody MspAndBarriersFilterPayload subPortalMspAndBarrsFilterPayload,
			@PathVariable("lang") String lang) {
		Page<SanitaryPhytosanitaryMeasuresRefLang> response = sanitaryphytosanitarymeasuresrefService
				.filterForPortal(subPortalMspAndBarrsFilterPayload, page, size, lang, orderDirection);
		return ResponseEntity.ok().body(response);
	}

	@PostMapping("/subPortal/productInformationFinderfilter/{lang}")
	public ResponseEntity<Page<SanitaryPhytosanitaryMeasuresRefLang>> subPortalProductInformationFinderfilter(
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "DESC") String orderDirection,
			@RequestBody SubportalProductInformationFinderFilterPayload payload,
			@PathVariable("lang") String lang) {
		Page<SanitaryPhytosanitaryMeasuresRefLang> response = sanitaryphytosanitarymeasuresrefService
				.subPortalProductInformationFinderfilter(payload, page, size, lang, orderDirection);
		return ResponseEntity.ok().body(response);
	}

	@DeleteMapping("delete/{id}")
	@ResponseBody
	public ResponseEntity deleteSanitaryPhytosanitaryMeasuresRef(@PathVariable("id") Long id) {
		ErrorResponse response = sanitaryphytosanitarymeasuresrefService.delete(id);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	@DeleteMapping("/deleteList")
	@ResponseBody
	public ResponseEntity deleteList(@RequestBody ListOfObject listOfObject) {
		ErrorResponse response = sanitaryphytosanitarymeasuresrefService.deleteList(listOfObject);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	@DeleteMapping("delete/{id}/{lang}")
	@ResponseBody
	public ResponseEntity deleteChapter(@PathVariable("id") Long id, @PathVariable("lang") String lang) {
		ErrorResponse response = sanitaryphytosanitarymeasuresrefService.delete(id, lang);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	@PutMapping("/edit/{id}")
	public ResponseEntity<SanitaryPhytosanitaryMeasuresRefBean> updateSanitaryPhytosanitaryMeasure(@PathVariable(value = "id") Long SanitaryPhytosanitaryMeasuresRefId,
																								   @Valid @RequestBody SanitaryPhytosanitaryMeasuresRefBean updateSanitaryPhytosanitaryMeasuresRef) throws ResourceNotFoundException {
		SanitaryPhytosanitaryMeasuresRef sanitaryPhytosanitaryMeasuresRef = sanitaryPhytosanitaryMeasuresRefRepository.findById(SanitaryPhytosanitaryMeasuresRefId)
				.orElseThrow(() -> new ResourceNotFoundException("sanitaryPhytosanitaryMeasuresRef not found for this id :: " + SanitaryPhytosanitaryMeasuresRefId));

		sanitaryPhytosanitaryMeasuresRef.setCode(updateSanitaryPhytosanitaryMeasuresRef.getCode());
		sanitaryPhytosanitaryMeasuresRef.setCountryRef(countryRefRepository.findByReference(updateSanitaryPhytosanitaryMeasuresRef.getCountryRef().getReference()));


		final SanitaryPhytosanitaryMeasuresRefBean updateSanitaryPhytosanitaryMeasure = sanitaryphytosanitarymeasuresrefMapper.entityToBean(sanitaryPhytosanitaryMeasuresRefRepository.save(sanitaryPhytosanitaryMeasuresRef)) ;
		return ResponseEntity.ok(updateSanitaryPhytosanitaryMeasure);
	}

	@GetMapping("/getAllByCodeOrLabel/{value}/{lang}")
	public ResponseEntity< ? > getAllByCodeOrLabel(@PathVariable("value") String value, @PathVariable("lang") String lang, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size)  {
//		return ResponseEntity.ok().body( sanitaryphytosanitarymeasuresrefService.filterByCodeOrLabel(value , PageRequest.of(page,size) , lang));
		if (value.equals(" ") || value == null|| value.equals(""))
			return getAll(page, size, lang, "ASC");
		else
		return ResponseEntity.ok().body( sanitaryphytosanitarymeasuresrefService.filterByCodeOrLabelProjection(value , lang,PageRequest.of(page,size)));

	}

//	@GetMapping("/{id}")
//	public ResponseEntity getById(@PathVariable("id") Long id){
//		return ResponseEntity.ok().body(sanitaryphytosanitarymeasuresrefService.findById(id));
//	}

	@PostMapping("/import-valid")
	public ResponseEntity<?> importValidFile(@RequestParam("file") MultipartFile file) {
		return sanitaryphytosanitarymeasuresrefService.saveFromExcelWithReturn(file);
	}
}
