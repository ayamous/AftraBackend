package ma.itroad.aace.eth.coref.controller;

import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.bean.RefTransportationTypeBean;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.mapper.RefTransportationTypeMapper;
import ma.itroad.aace.eth.coref.service.IRefTransportationTypeService;
import ma.itroad.aace.eth.coref.service.helper.TransportationTypeEntityRefLang;
import ma.itroad.aace.eth.coref.service.helper.UnitRefEntityRefLang;
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
@RequestMapping(value = "/refTransportationTypes", consumes = {MediaType.ALL_VALUE}, produces = MediaTypes.HAL_JSON_VALUE)
@ResponseBody
public class RefTransportationTypeController {

    @Autowired
    IRefTransportationTypeService refTransportationTypeService;

    @Autowired
    RefTransportationTypeMapper refTransportationTypeMapper;

    @Autowired
    ImportDataService importDataService;

    @GetMapping("/all")
    ResponseEntity<Page<RefTransportationTypeBean>> getAll(@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "DESC") String orderDirection) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RefTransportationTypeBean> response = refTransportationTypeService.getAll(pageable);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("all/{lang}")
    ResponseEntity<Page<TransportationTypeEntityRefLang>> getAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @PathVariable("lang") String lang, @RequestParam(defaultValue = "DESC") String orderDirection) {
        Page<TransportationTypeEntityRefLang> response = refTransportationTypeService.getAll(page, size, lang, orderDirection);
        return ResponseEntity.ok().body(response);
    }


    @PostMapping("/import")
    public ResponseEntity<String> importFile(@RequestParam("file") MultipartFile file) {
        importDataService.setService(refTransportationTypeService);
        return importDataService.importFile(file);
    }

    @GetMapping("/download/{lang}")
    @ResponseBody
    public ResponseEntity<InputStreamResource> getFile(@PathVariable("lang") String lang) throws IOException {
        String filename = "RefTransportationTypes.xlsx";
        InputStreamResource file = new InputStreamResource(refTransportationTypeService.load(lang));
        MediaType excelMediaType = new MediaType("application", "vnd.ms-excel");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    @DeleteMapping("delete/{id}")
    @ResponseBody
    public ResponseEntity deleteTransportationTypeEntityRef(@PathVariable("id") Long id) {

        ErrorResponse response = refTransportationTypeService.delete(id);
        return ResponseEntity.status(response.getStatus()).body(response);

    }

    @DeleteMapping("deleteInternationalisation/{id}/{lang}")
    @ResponseBody
    public ResponseEntity deleteInternationalisation(@PathVariable("id") Long id, @PathVariable("lang") String lang) {

        ErrorResponse response = refTransportationTypeService.deleteInternationalisation(lang, id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/add")
    public TransportationTypeEntityRefLang addTransportationTypeEntityRef(@RequestBody TransportationTypeEntityRefLang transportationTypeEntityRefLang) {
        refTransportationTypeService.addTransportationTypeEntityRef(transportationTypeEntityRefLang);
        return transportationTypeEntityRefLang;
    }


    @GetMapping("/find/{lang}/{id}")
    public TransportationTypeEntityRefLang findTransportationType(@PathVariable("lang") String lang, @PathVariable("id") Long id) {
        return refTransportationTypeService.findTransportationType(id, lang);
    }

    @PostMapping("/addInternationalisation/{lang}")
    public EntityRefLang addInternationalisation(@PathVariable("lang") String lang, @RequestBody EntityRefLang entityRefLang) {
        refTransportationTypeService.addInternationalisation(entityRefLang,lang);
        return entityRefLang;
    }

    @GetMapping("/getAllByCodeOrLabel/{value}/{lang}")
    public ResponseEntity getAllByCodeOrLabel(@PathVariable("value") String value, @PathVariable("lang") String lang, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size)  {
        if (value.equals(" ") || value == null|| value.equals(""))
            return getAll(page, size, lang, "ASC");
        else
        return ResponseEntity.ok().body( refTransportationTypeService.filterByCodeOrLabelProjection(value , PageRequest.of(page,size) , lang));
    }

    @DeleteMapping("/deleteList")
    @ResponseBody
    public ResponseEntity deleteList(@RequestBody ListOfObject listOfObject) {
        ErrorResponse response = refTransportationTypeService.deleteList(listOfObject);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/import-valid")
    public ResponseEntity<?> importValidFile(@RequestParam("file") MultipartFile file) {
        return refTransportationTypeService.saveFromExcelWithReturn(file);
    }

}
