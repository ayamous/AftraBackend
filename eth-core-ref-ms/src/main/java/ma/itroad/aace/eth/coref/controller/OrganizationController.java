package ma.itroad.aace.eth.coref.controller;

import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.CountryRefBean;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.bean.OrganizationBean;
import ma.itroad.aace.eth.coref.service.helper.CategoryRefEntityRefLang;
import ma.itroad.aace.eth.coref.service.helper.OrganizationHelper;
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
import ma.itroad.aace.eth.coref.model.mapper.OrganizationMapper;
import ma.itroad.aace.eth.coref.service.IOrganizationService;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping(value = "/organizations", consumes = {MediaType.ALL_VALUE}, produces = MediaTypes.HAL_JSON_VALUE)
@ResponseBody
public class OrganizationController {

    @Autowired
    IOrganizationService iOrganizationService;

    @Autowired
    ImportDataService importDataService;


    @GetMapping("/all")
    ResponseEntity<Page<OrganizationHelper>> getAll(@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "DESC") String orderDirection) {
        Page<OrganizationHelper> response = iOrganizationService.getAll(page, size, orderDirection);
        return ResponseEntity.ok().body(response);
    }
    @GetMapping("/searchOrganization")
    ResponseEntity<Page<OrganizationHelper>> getSearchData(
            @RequestParam(defaultValue = "") String reference,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "DESC") String orderDirection) {
        Page<OrganizationHelper> response = iOrganizationService.searchOrganizationByKeyWord(page, size, reference, orderDirection);
        return ResponseEntity.ok().body(response);
    }
    @PostMapping("/import")
    public ResponseEntity<String> importFile(@RequestParam("file") MultipartFile file) {

        importDataService.setService(iOrganizationService);
        ResponseEntity<String> imp = importDataService.importFile(file);
        return imp;
    }

    @GetMapping("/download")
    @ResponseBody
    public ResponseEntity<InputStreamResource> getFile() throws IOException {
        String filename = "Organisations.xlsx";
        InputStreamResource file = new InputStreamResource(iOrganizationService.load());
        MediaType excelMediaType = new MediaType("application", "vnd.ms-excel");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    @DeleteMapping("delete/{id}")
    @ResponseBody
    public ResponseEntity deleteOrganization(@PathVariable("id") Long id) {
        ErrorResponse response = iOrganizationService.delete(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @DeleteMapping("/deleteList")
    @ResponseBody
    public ResponseEntity deleteList(@RequestBody ListOfObject listOfObject) {
        ErrorResponse response = iOrganizationService.deleteList(listOfObject);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/getAllByCodeOrName/{value}")
    public ResponseEntity< Page<OrganizationHelper> > filterByReferenceOrName(@PathVariable("value") String value,
                                                                                @RequestParam(defaultValue = "0") int page,
                                                                                @RequestParam(defaultValue = "10") int size)  {
        return ResponseEntity.ok().body(iOrganizationService.filterByReferenceOrName(value , PageRequest.of(page,size) ));
    }

//    @GetMapping("/{id}")
//    public ResponseEntity getById(@PathVariable("id") Long id){
//        return ResponseEntity.ok().body(iOrganizationService.findById(id));
//    }


    @PostMapping("/import-valid")
    public ResponseEntity<?> importValidFile(@RequestParam("file") MultipartFile file) {

        return iOrganizationService.saveFromExcelWithReturn(file);
    }

}
