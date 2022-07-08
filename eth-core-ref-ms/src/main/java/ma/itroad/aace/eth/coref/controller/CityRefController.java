package ma.itroad.aace.eth.coref.controller;

import ma.itroad.aace.eth.coref.model.bean.CityRefBean;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.CountryRefBean;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.bean.SectionRefBean;
import ma.itroad.aace.eth.coref.model.entity.CityRef;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.entity.Lang;
import ma.itroad.aace.eth.coref.model.mapper.projections.CityRefEntityRefLangProjection;
import ma.itroad.aace.eth.coref.repository.CityRefRepository;
import ma.itroad.aace.eth.coref.service.ICityRefService;
import ma.itroad.aace.eth.coref.service.helper.CityRefEntityRefLang;
import ma.itroad.aace.eth.coref.service.helper.CountryRefEntityRefLang;
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
import ma.itroad.aace.eth.coref.repository.CountryRefRepository;

import javax.validation.Valid;
import java.io.IOException;

@RestController
@RequestMapping(value = "/cityRefs", consumes = {MediaType.ALL_VALUE}, produces = MediaTypes.HAL_JSON_VALUE)
@ResponseBody
public class CityRefController {

    @Autowired
    ICityRefService iCityRefService;

    @Autowired
    ImportDataService importDataService;

    @Autowired
    CountryRefRepository countryRefRepository;

    @Autowired
    private CityRefRepository cityRefRepository;


    @GetMapping("/all")
    ResponseEntity<Page<CityRefBean>> getAll(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "10") int size,
                                             @RequestParam(defaultValue = "DESC") String orderDirection) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CityRefBean> response = iCityRefService.getAll(pageable);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("all/{lang}")
    ResponseEntity<Page<CityRefEntityRefLang>> getAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @PathVariable("lang") String lang,  @RequestParam(defaultValue = "DESC") String orderDirection) {
        Page<CityRefEntityRefLang> response = iCityRefService.getAll(page, size, lang, orderDirection);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/import")
    public ResponseEntity<String> importFile(@RequestParam("file") MultipartFile file) {

        importDataService.setService(iCityRefService);
        return importDataService.importFile(file);
    }

    @GetMapping("/download/{lang}")
    @ResponseBody
    public ResponseEntity<InputStreamResource> getFile(@PathVariable("lang") String lang, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "DESC") String orderDirection) throws IOException {
        String filename = "cityRefs.xlsx";
        InputStreamResource file = new InputStreamResource(iCityRefService.load(lang, page, size, orderDirection));
        MediaType excelMediaType = new MediaType("application", "vnd.ms-excel");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    @DeleteMapping("delete/{id}")
    @ResponseBody
    public ResponseEntity deleteCity(@PathVariable("id") Long id) {
        ErrorResponse response = iCityRefService.delete(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @DeleteMapping("/deleteList")
    @ResponseBody
    public ResponseEntity deleteList(@RequestBody ListOfObject listOfObject) {
        ErrorResponse response = iCityRefService.deleteList(listOfObject);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @DeleteMapping("deleteInternationalisation/{id}/{lang}")
    @ResponseBody
    public ResponseEntity deleteInternationalisation(@PathVariable("id") Long id, @PathVariable("lang") String lang) {
        ErrorResponse response = iCityRefService.deleteInternationalisation(lang, id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }


    @PostMapping("/add")
    public CityRefEntityRefLang addCityRef(@RequestBody CityRefEntityRefLang cityRefEntityRefLang) {
        iCityRefService.addCityRef(cityRefEntityRefLang);
        return cityRefEntityRefLang;
    }

    @GetMapping("/find/{lang}/{id}")
    public CityRefEntityRefLang findCountry(@PathVariable("lang") String lang, @PathVariable("id") Long id) {
        return iCityRefService.findcity(id, lang);
    }


    @PostMapping("/addInternationalisation/{lang}")
    public EntityRefLang addInternationalisation(@PathVariable("lang") String lang,@RequestBody EntityRefLang entityRefLang) {
        iCityRefService.addInternationalisation(entityRefLang,lang);
        return entityRefLang;
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<CityRef> updateCity(@PathVariable(value = "id") Long cityRefId,
                                              @Valid @RequestBody CityRef cityRefs) throws ResourceNotFoundException {
        CityRef cityRef = cityRefRepository.findById(cityRefId)
                .orElseThrow(() -> new ResourceNotFoundException("cityRef not found for this id :: " + cityRefId));

        cityRef.setReference(cityRefs.getReference());
        cityRef.setCountryRef(countryRefRepository.findByReference(cityRefs.getCountryRef().getReference()));
        final CityRef updateCity = cityRefRepository.save(cityRef);
        return ResponseEntity.ok(updateCity);
    }

    @GetMapping("/getAllByReferenceOrLabel/{value}/{lang}")
    public      ResponseEntity< ? > getAllByReferenceOrLabel(@PathVariable("value") String value, @PathVariable("lang") String lang, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size)  {
        if (value.equals(" ") || value == null|| value.equals(""))
            return getAll(page, size, lang, "ASC");
        else
        return ResponseEntity.ok().body(iCityRefService.filterByReferenceOrLabel(value,lang,page,size));
        //        return ResponseEntity.ok().body( iCityRefService.filterByReferenceOrLabel(value , PageRequest.of(page,size) , lang));
    }

//    @GetMapping("/{id}")
//    public ResponseEntity getById(@PathVariable("id") Long id){
//        return ResponseEntity.ok().body(iCityRefService.findById(id));
//    }

    @PostMapping("/import-valid")
    public ResponseEntity<?> importValidFile(@RequestParam("file") MultipartFile file) {
        return iCityRefService.saveFromExcelWithReturn(file);
    }

}
