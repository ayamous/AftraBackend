package ma.itroad.aace.eth.coref.controller;

import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.bean.TaxRefBean;
import ma.itroad.aace.eth.coref.model.mapper.TaxRefMapper;
import ma.itroad.aace.eth.coref.model.view.TaxRefVM;
import ma.itroad.aace.eth.coref.service.ITaxRefService;
import ma.itroad.aace.eth.coref.service.helper.ChapterRefLang;
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
@RequestMapping(value = "/taxRefs", consumes = {MediaType.ALL_VALUE}, produces = MediaTypes.HAL_JSON_VALUE)
@ResponseBody
public class TaxRefController {

    @Autowired
    ITaxRefService iTaxRefService;

    @Autowired
    TaxRefMapper taxRefMapper;
    @Autowired
    ITaxRefService service;

    @Autowired
    ImportDataService importDataService;

 /*   @GetMapping
    public ResponseEntity<Page<TaxRefBean>> getAllTaxRefs(@RequestParam(defaultValue = "0") Integer page,
                                                          @RequestParam(defaultValue = "10") Integer size) {

        Page<TaxRefBean> response = iTaxRefService.getAll(page, size);
        return ResponseEntity.ok().body(response);
    }*/

    @PostMapping("/import")
    public ResponseEntity<String> importFile(@RequestParam("file") MultipartFile file) {

        importDataService.setService(iTaxRefService);
        ResponseEntity<String> imp = importDataService.importFile(file);
        return imp;
    }

    @GetMapping("all")
    ResponseEntity<Page<TaxRefVM>> getAll(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "DESC") String orderDirection) {

        Page<TaxRefVM> response = service.getAll(page, size, orderDirection);
        return ResponseEntity.ok().body(response);

    }

    @GetMapping("/download")
    @ResponseBody
    public ResponseEntity<InputStreamResource> getFile() throws IOException {
        String filename = "taxRefs.xlsx";
        InputStreamResource file = new InputStreamResource(iTaxRefService.load());
        MediaType excelMediaType = new MediaType("application", "vnd.ms-excel");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    @DeleteMapping("delete/{id}")
    @ResponseBody
    public ResponseEntity deleteTaxRef(@PathVariable("id") Long id) {
        ErrorResponse response = iTaxRefService.delete(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @DeleteMapping("/deleteList")
    @ResponseBody
    public ResponseEntity deleteList(@RequestBody ListOfObject listOfObject) {
        ErrorResponse response = iTaxRefService.deleteList(listOfObject);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/find/{id}")
    public TaxRefBean findChapter(@PathVariable("id") Long id) {
        return iTaxRefService.findById(id);
    }


    @GetMapping("/findByCode")
    public ResponseEntity getByCode(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size, @RequestParam String code) {

        if (code.equals(" ") || code == null|| code.equals(""))
            return getAll(page, size, "ASC");
        else
        return ResponseEntity.ok().body(iTaxRefService.findByCode(code, PageRequest.of(page,size)));
    }

//    @GetMapping("/{id}")
//    public ResponseEntity getById(@PathVariable("id") Long id){
//        return ResponseEntity.ok().body(iTaxRefService.findById(id));
//    }

    @PostMapping("/import-valid")
    public ResponseEntity<?> importValidFile(@RequestParam("file") MultipartFile file) {
        return iTaxRefService.saveFromExcelWithReturn(file);
    }
}
