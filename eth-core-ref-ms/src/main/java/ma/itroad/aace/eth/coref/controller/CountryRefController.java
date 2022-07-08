package ma.itroad.aace.eth.coref.controller;

import ma.itroad.aace.eth.core.model.enums.TableRef;
import ma.itroad.aace.eth.coref.model.bean.CountryRefBean;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.entity.CountryRef;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.mapper.CountryRefMapper;
import ma.itroad.aace.eth.coref.repository.CountryRefRepository;
import ma.itroad.aace.eth.coref.service.ICountryRefService;
import ma.itroad.aace.eth.coref.service.helper.CountryRefEntityRefLang;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(value = "/countryRefs", consumes = {MediaType.ALL_VALUE}, produces = MediaTypes.HAL_JSON_VALUE)
@ResponseBody
public class CountryRefController {

    @Autowired
    ICountryRefService countryRefService;

    @Autowired
    CountryRefRepository countryRefRepository;

    @Autowired
    CountryRefMapper countryRefsMapper;

    @Autowired
    ImportDataService importDataService;


    @GetMapping("/all")
    ResponseEntity<Page<CountryRefBean>> getAll(@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size,
                                                @RequestParam(defaultValue = "DESC") String orderDirection) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CountryRefBean> response = countryRefService.getAll(pageable);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("all/{lang}")
    ResponseEntity<Page<CountryRefEntityRefLang>> getAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @PathVariable("lang") String lang,  @RequestParam(defaultValue = "DESC") String orderDirection) {
        Page<CountryRefEntityRefLang> response = countryRefService.getAll(page, size, lang, orderDirection);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/import")
    public ResponseEntity<String> importFile(@RequestParam("file") MultipartFile file) {

        importDataService.setService(countryRefService);
        return importDataService.importFile(file);
    }

    @GetMapping("/download/{lang}")
    @ResponseBody
    public ResponseEntity<InputStreamResource> getFile(@PathVariable("lang") String lang) throws IOException {
        String filename = "countryRefss.xlsx";
        InputStreamResource file = new InputStreamResource(countryRefService.load(lang));
        MediaType excelMediaType = new MediaType("application", "vnd.ms-excel");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    @DeleteMapping("delete/{id}")
    @ResponseBody
    public ResponseEntity deleteCountryRef(@PathVariable("id") Long id) {
        ErrorResponse response = countryRefService.delete(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @DeleteMapping("deleteInternationalisation/{id}/{lang}")
    @ResponseBody
    public ResponseEntity deleteInternationalisation(@PathVariable("id") Long id, @PathVariable("lang") String lang) {
        ErrorResponse response = countryRefService.deleteInternationalisation(lang, id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @DeleteMapping("/deleteList")
    @ResponseBody
    public ResponseEntity deleteList(@RequestBody ListOfObject listOfObject) {
        ErrorResponse response = countryRefService.deleteList(listOfObject);
        return ResponseEntity.status(response.getStatus()).body(response);
    }



    @PostMapping("/add")
    public CountryRefEntityRefLang addCountryRef(@RequestBody CountryRefEntityRefLang countryRefEntityRefLang) {
        countryRefService.addCountryRef(countryRefEntityRefLang);
        return countryRefEntityRefLang;
    }

    @GetMapping("/find/{lang}/{id}")
    public CountryRefEntityRefLang findCountry(@PathVariable("lang") String lang, @PathVariable("id") Long id) {
        return countryRefService.findCountry(id, lang);
    }

    @PostMapping("/addInternationalisation/{lang}")
    public EntityRefLang addInternationalisation(@PathVariable("lang") String lang,@RequestBody EntityRefLang entityRefLang) {
        countryRefService.addInternationalisation(entityRefLang,lang);
        return entityRefLang;
    }

    @GetMapping("/getAllByReferenceOrLabel/{value}/{lang}")
    public      ResponseEntity  getAllByReferenceOrLabel(@PathVariable("value") String value, @PathVariable("lang") String lang, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size)  {
        if (value.equals(" ") || value == null|| value.equals(""))
            return getAll(page, size, lang, "ASC");
        else
        return ResponseEntity.ok().body( countryRefService.filterByReferenceOrLabelProjection(value ,   PageRequest.of(page, size), lang));
    }

//    @GetMapping("/{id}")
//    public ResponseEntity getById(@PathVariable("id") Long id){
//        return ResponseEntity.ok().body(countryRefService.findById(id));
//    }


    @PostMapping("/import-valid")
    public ResponseEntity<?> importValidFile(@RequestParam("file") MultipartFile file) {
        return countryRefService.saveFromExcelWithReturn(file);
    }

}
