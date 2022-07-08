package ma.itroad.aace.eth.coref.controller;

import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.bean.ListOfObjectTarifBook;
import ma.itroad.aace.eth.coref.model.mapper.projections.CountryGroupReferenceJoinProjection;
import ma.itroad.aace.eth.coref.model.view.CountryGroupReferenceVM;
import ma.itroad.aace.eth.coref.service.ICountryGroupJoinService;
import ma.itroad.aace.eth.coref.service.helper.CountryGroupReferenceVMEntityRefLang;
import ma.itroad.aace.eth.coref.service.helper.CountryGroupRefsEntityRefLang;
import ma.itroad.aace.eth.coref.service.impl.ImportDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/countryGroupJoin")
public class CountryGroupJoinController {


    private final static String fileName = "country-group-join.xlsx";

    @Autowired
    ICountryGroupJoinService countryGroupJoinService;

    @Autowired
    ImportDataService importDataService;

    @GetMapping("all")
    ResponseEntity<Page<CountryGroupReferenceVM>> getAll(@RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "10") int size,
                                                         @RequestParam(defaultValue = "DESC") String orderDirection) {
        Page<CountryGroupReferenceVM> response = countryGroupJoinService.getAll(page, size, orderDirection);
        return ResponseEntity.ok().body(response);
    }


    @GetMapping("/download/{lang}")
    @ResponseBody
    public ResponseEntity<InputStreamResource> getFile(@PathVariable("lang") String lang) throws IOException {
        InputStreamResource file = new InputStreamResource(countryGroupJoinService.load(lang));
        MediaType excelMediaType = new MediaType("application", "vnd.ms-excel");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; fileName=" + fileName)
                .contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    @PostMapping("/import")
    public ResponseEntity<String> importFile(@RequestParam("file") MultipartFile file) {

        importDataService.setService(countryGroupJoinService);
        return importDataService.importFile(file);
    }



    @PostMapping("/add")
    public CountryGroupReferenceVM addInternationalisation(@RequestBody CountryGroupReferenceVM countryGroupReferenceVM) {
        return countryGroupJoinService.save(countryGroupReferenceVM);
    }

    @GetMapping("/find/{lang}/{idCountry}/{idCountryGroup}")
    public CountryGroupReferenceVMEntityRefLang findCountryGroup(@PathVariable("lang") String lang, @PathVariable("idCountry") Long idCountry, @PathVariable("idCountryGroup") Long idCountryGroup) {
        return countryGroupJoinService.findCountryGroupReferenceVM(idCountry,idCountryGroup,lang);
    }


    @GetMapping("/searchByFilter")
    public ResponseEntity findByFilter(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam String reference){
        if (reference.equals(" ") || reference == null|| reference.equals(""))
            return getAll(page, size, "ASC");
        Page<CountryGroupReferenceJoinProjection> response = countryGroupJoinService.findByRefrence(page,size, reference);
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/deleteList")
    @ResponseBody
    public ResponseEntity deleteList(@RequestBody ListOfObjectTarifBook listOfObjectTarifBook) {
        ErrorResponse response = countryGroupJoinService.deleteList(listOfObjectTarifBook);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

}
