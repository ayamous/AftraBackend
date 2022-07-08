package ma.itroad.aace.eth.coref.controller;

import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ChapterRefBean;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.bean.SanitaryPhytosanitaryMeasuresRefBean;
import ma.itroad.aace.eth.coref.model.mapper.projections.TaxationRefLangProjection;
import ma.itroad.aace.eth.coref.model.view.ProductInformationFinderFilterPayload;
import ma.itroad.aace.eth.coref.model.view.SubportalProductInformationFinderFilterPayload;
import ma.itroad.aace.eth.coref.repository.TaxationRepository;
import ma.itroad.aace.eth.coref.service.helper.CategoryRefEntityRefLang;
import ma.itroad.aace.eth.coref.service.helper.TariffBookRefLang;
import ma.itroad.aace.eth.coref.service.helper.detailed.TaxationRefLangDetailed;
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

import ma.itroad.aace.eth.coref.model.bean.TaxationBean;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.entity.Taxation;
import ma.itroad.aace.eth.coref.model.mapper.TaxationMapper;
import ma.itroad.aace.eth.coref.service.ITaxationService;
import ma.itroad.aace.eth.coref.service.helper.TaxationRefLang;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(value = "/taxations", consumes = { MediaType.ALL_VALUE }, produces = MediaTypes.HAL_JSON_VALUE)
@ResponseBody
public class TaxationController {

	@Autowired
	ITaxationService iTaxationService;

	@Autowired
	TaxationMapper taxationMapper;

	@Autowired
	ImportDataService importDataService;

	@Autowired
	TaxationRepository taxationRepository;

	@GetMapping("all")
	ResponseEntity<Page<TaxationBean>> getAll(@RequestParam(defaultValue = "0") int page,
												@RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "DESC") String orderDirection) {
		Pageable pageable = PageRequest.of(page, size);
		Page<TaxationBean> response = iTaxationService.getAll(pageable);
		return ResponseEntity.ok().body(response);
	}

	@GetMapping("all/{lang}")
	ResponseEntity<Page<TaxationRefLang>> getAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @PathVariable("lang") String lang, @RequestParam(defaultValue = "DESC") String orderDirection) {
		Page<TaxationRefLang> response = iTaxationService.getAll(page, size, lang, orderDirection);
		return ResponseEntity.ok().body(response);
	}

	@PostMapping("/import")
	public ResponseEntity<String> importFile(@RequestParam("file") MultipartFile file) {

		importDataService.setService(iTaxationService);
		return importDataService.importFile(file);
	}

	@GetMapping("/download")
	@ResponseBody
	public ResponseEntity<InputStreamResource> getFile() throws IOException {
		String filename = "taxations.xlsx";
		InputStreamResource file = new InputStreamResource(iTaxationService.load());
		MediaType excelMediaType = new MediaType("application", "vnd.ms-excel");
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
				.contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(file);

	}

	@DeleteMapping("deleteInternationalisation/{id}/{lang}")
	@ResponseBody
	public ResponseEntity deleteInternationalisation(@PathVariable("id") Long id, @PathVariable("lang") String lang) {
		ErrorResponse response = iTaxationService.deleteInternationalisation(lang, id);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	@GetMapping("/download/{lang}")
	@ResponseBody
	public ResponseEntity<InputStreamResource> getFile(@PathVariable("lang") String lang) throws IOException {
		String filename = "taxations.xlsx";
		InputStreamResource file = new InputStreamResource(iTaxationService.load(lang));
		MediaType excelMediaType = new MediaType("application", "vnd.ms-excel");
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
				.contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(file);
	}

	@DeleteMapping("delete/{id}")
	@ResponseBody
	public ResponseEntity deleteTaxation(@PathVariable("id") Long id) {
		ErrorResponse response = iTaxationService.delete(id);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	@DeleteMapping("/deleteList")
	@ResponseBody
	public ResponseEntity deleteList(@RequestBody ListOfObject listOfObject) {
		ErrorResponse response = iTaxationService.deleteList(listOfObject);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	@PostMapping("/add")
	public TaxationRefLang addTaxation(@RequestBody TaxationRefLang taxationRefLang) {
		iTaxationService.addTaxation(taxationRefLang);
		return taxationRefLang;
	}

	@GetMapping("/find/{lang}/{id}")
	public TaxationRefLang findTaxation(@PathVariable("lang") String lang, @PathVariable("id") Long id) {
		return iTaxationService.findTaxation(id, lang);
	}

	@GetMapping("/findById/{id}")
	public ResponseEntity<TaxationBean> findById(@PathVariable Long id) {
		return ResponseEntity.ok().body(taxationMapper.entityToBean(taxationRepository.findOneById(id)));
	}

	@PostMapping("/addInternationalisation/{lang}")
	public EntityRefLang addInternationalisation(@PathVariable("lang") String lang,@RequestBody EntityRefLang entityRefLang) {
		iTaxationService.addInternationalisation(entityRefLang,lang);
		return entityRefLang;
	}

	@PostMapping("/productInformationFinderfilter/{lang}")
	public ResponseEntity<Page<TaxationRefLang>> filterForPortal(@RequestParam(defaultValue = "0") int page,@RequestParam(defaultValue = "10") int size, @RequestBody ProductInformationFinderFilterPayload productInformationFinderFilterPayload, @PathVariable("lang") String lang, @RequestParam(defaultValue = "DESC") String orderDirection) {
		Page<TaxationRefLang> response = iTaxationService.filterForPortal(productInformationFinderFilterPayload, page, size, lang, orderDirection);
		return ResponseEntity.ok().body(response);
	}

	@PostMapping("/subPortal/productInformationFinderfilter/{lang}")
	public ResponseEntity<Page<TaxationRefLang>> subPortalProductInformationFinderfilter(
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			@RequestBody SubportalProductInformationFinderFilterPayload payload, @PathVariable("lang") String lang,
			@RequestParam(defaultValue = "DESC") String orderDirection) {
		Page<TaxationRefLang> response = iTaxationService.subPortalProductInformationFinderfilter(payload, page, size, lang, orderDirection);
		return ResponseEntity.ok().body(response);
	}

	@GetMapping("/getAllByCodeOrLabel/{value}/{lang}")
	public ResponseEntity<?> getAllByReferenceOrLabel(
			@PathVariable("value") String value, @PathVariable("lang") String lang, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size)  {
//		return ResponseEntity.ok().body( iTaxationService.filterByReferenceOrLabel(value , PageRequest.of(page,size) , lang));
		if (value.equals(" ") || value == null|| value.equals(""))
			return getAll(page, size, lang, "ASC");
		else
		return ResponseEntity.ok().body( iTaxationService.filterByReferenceOrLabelProjection(value ,lang, PageRequest.of(page,size)));

	}

	@GetMapping("/find/detail/{lang}/{id}")
	public TaxationRefLangDetailed findTaxationDetailedDetailed(@PathVariable("lang") String lang, @PathVariable("id") Long id) {
		return iTaxationService.findTaxationDetailedDetailed(iTaxationService.findTaxation(id, lang)) ;
	}

//	@GetMapping("/{id}")
//	public ResponseEntity getById(@PathVariable("id") Long id){
//		return ResponseEntity.ok().body(iTaxationService.findById(id));
//	}

	@PostMapping("/import-valid")
	public ResponseEntity<?> importValidFile(@RequestParam("file") MultipartFile file) {
		return iTaxationService.saveFromExcelWithReturn(file);
	}
}
