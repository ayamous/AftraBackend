package ma.itroad.aace.eth.coref.controller;

import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.bean.RefCurrencyBean;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.mapper.RefCurrencyMapper;
import ma.itroad.aace.eth.coref.service.IRefCurrencyService;
import ma.itroad.aace.eth.coref.service.helper.RefCurrencyRefEntityRefLang;
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

@RestController
@RequestMapping(value = "/refCurrencies", consumes = {MediaType.ALL_VALUE}, produces = MediaTypes.HAL_JSON_VALUE)
//@ResponseBody
public class RefCurrencyController {

    @Autowired
    IRefCurrencyService refCurrencyService;

    @Autowired
    RefCurrencyMapper refCurrencyMapper;

    @Autowired
    ImportDataService importDataService;

  /*  @GetMapping("/RefCurrency")
    public Page<RefCurrencyBean> getAllRefCurrency(@RequestParam(defaultValue = "0") Integer page,
                                               @RequestParam(defaultValue = "10") Integer size){
        Sort.Order order = new Sort.Order(Sort.Direction.DESC, "id");
        return iRefCurrencyService.getAll(PageRequest.of(page, size,  Sort.by(order)));
    }*/

    @GetMapping("/all")
    ResponseEntity<Page<RefCurrencyBean>> getAll(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "DESC") String orderDirection) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RefCurrencyBean> response = refCurrencyService.getAll(pageable);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("all/{lang}")
    ResponseEntity<Page<RefCurrencyRefEntityRefLang>> getAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @PathVariable("lang") String lang, @RequestParam(defaultValue = "DESC") String orderDirection) {
        Page<RefCurrencyRefEntityRefLang> response = refCurrencyService.getAll(page, size, lang, orderDirection);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/import")
    public ResponseEntity<String> importFile(@RequestParam("file") MultipartFile file) {
        importDataService.setService(refCurrencyService);
        return importDataService.importFile(file);
    }

    @GetMapping("/download/{lang}")
    @ResponseBody
    public ResponseEntity<InputStreamResource> getFile(@PathVariable("lang") String lang) throws IOException {
        String filename = "currencies.xlsx";
        InputStreamResource file = new InputStreamResource(refCurrencyService.load(lang));
        MediaType excelMediaType = new MediaType("application", "vnd.ms-excel");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    @DeleteMapping("delete/{id}")
    @ResponseBody
    public ResponseEntity deleteRefCurrency(@PathVariable("id") Long id) {
        ErrorResponse response = refCurrencyService.delete(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @DeleteMapping("deleteInternationalisation/{id}/{lang}")
    @ResponseBody
    public ResponseEntity deleteInternationalisation(@PathVariable("id") Long id, @PathVariable("lang") String lang) {

        ErrorResponse response = refCurrencyService.deleteInternationalisation(lang, id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/add")
    public RefCurrencyRefEntityRefLang addRefCurrencyRef(@RequestBody RefCurrencyRefEntityRefLang currencyRefEntityRefLang) {
        refCurrencyService.addRefCurrencyRef(currencyRefEntityRefLang);
        return currencyRefEntityRefLang;
    }

    @PostMapping("/addInternationalisation/{lang}")
    public EntityRefLang addInternationalisation(@PathVariable("lang") String lang,@RequestBody EntityRefLang entityRefLang) {
        refCurrencyService.addInternationalisation(entityRefLang,lang);
        return entityRefLang;
    }

    @GetMapping("/find/{lang}/{id}")
    public RefCurrencyRefEntityRefLang findCurrency(@PathVariable("lang") String lang, @PathVariable("id") Long id) {
        return refCurrencyService.findCurrency(id, lang);
    }

    @GetMapping("/getAllByCodeOrLabel/{value}/{lang}")
    public ResponseEntity getAllByCodeOrLabel(@PathVariable("value") String value, @PathVariable("lang") String lang, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size)  {
        if (value.equals(" ") || value == null|| value.equals(""))
            return getAll(page, size, lang, "ASC");
        else
        return ResponseEntity.ok().body( refCurrencyService.filterByCodeOrLabelProjection(value , PageRequest.of(page,size) , lang));
    }

    @DeleteMapping("/deleteList")
    @ResponseBody
    public ResponseEntity deleteList(@RequestBody ListOfObject listOfObject) {
        ErrorResponse response = refCurrencyService.deleteList(listOfObject);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

//    @GetMapping("/{id}")
//    public ResponseEntity getById(@PathVariable("id") Long id){
//        return ResponseEntity.ok().body(refCurrencyService.findById(id));
//    }

    @PostMapping("/import-valid")
    public ResponseEntity<?> importValidFile(@RequestParam("file") MultipartFile file) {
        return refCurrencyService.saveFromExcelWithReturn(file);
    }
}
