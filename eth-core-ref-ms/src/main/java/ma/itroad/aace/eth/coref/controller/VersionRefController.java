package ma.itroad.aace.eth.coref.controller;

import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.AgreementTypeBean;
import ma.itroad.aace.eth.coref.model.bean.SectionRefBean;
import ma.itroad.aace.eth.coref.model.bean.VersionBean;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.mapper.VersionMapper;
import ma.itroad.aace.eth.coref.service.IVersionService;
import ma.itroad.aace.eth.coref.service.helper.AgreementTypeLang;
import ma.itroad.aace.eth.coref.service.helper.SectionRefEntityRefLang;
import ma.itroad.aace.eth.coref.service.helper.TaxationRefLang;
import ma.itroad.aace.eth.coref.service.helper.VersionRefEntityRefLang;
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
@RequestMapping(value = "/versionRefs", consumes = {MediaType.ALL_VALUE}, produces = MediaTypes.HAL_JSON_VALUE)
public class VersionRefController {

    @Autowired
    IVersionService versionService;

    @Autowired
    VersionMapper versionMapper;

    @Autowired
    ImportDataService importDataService;

    @GetMapping("/all")
    ResponseEntity<Page<VersionBean>> getAll(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "DESC") String orderDirection) {
        Pageable pageable = PageRequest.of(page, size);
        Page<VersionBean> response = versionService.getAll(pageable);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("all/{lang}")
    ResponseEntity<Page<VersionRefEntityRefLang>> getAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @PathVariable("lang") String lang, @RequestParam(defaultValue = "DESC") String orderDirection) {
        Page<VersionRefEntityRefLang> response = versionService.getAll(page, size, lang, orderDirection);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/import")
    public ResponseEntity<String> importFile(@RequestParam("file") MultipartFile file) {
        importDataService.setService(versionService);
        return importDataService.importFile(file);
    }

    @GetMapping("/download/{lang}")
    @ResponseBody
    public ResponseEntity<InputStreamResource> getFile(@PathVariable("lang") String lang) throws IOException {
        String filename = "versionRefs.xlsx";
        InputStreamResource file = new InputStreamResource(versionService.load(lang));
        MediaType excelMediaType = new MediaType("application", "vnd.ms-excel");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    @GetMapping("/download")
    @ResponseBody
    public ResponseEntity<InputStreamResource> getFile() throws IOException {
        String filename = "versionRefs.xlsx";
        InputStreamResource file = new InputStreamResource(versionService.load());
        MediaType excelMediaType = new MediaType("application", "vnd.ms-excel");
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    @DeleteMapping("delete/{id}")
    @ResponseBody
    public ResponseEntity deleteVersionRef(@PathVariable("id") Long id) {

        ErrorResponse response = versionService.delete(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @DeleteMapping("deleteInternationalisation/{id}/{lang}")
    @ResponseBody
    public ResponseEntity deleteInternationalisation(@PathVariable("id") Long id, @PathVariable("lang") String lang) {

        ErrorResponse response = versionService.deleteInternationalisation(lang, id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }


    @PostMapping("/add")
    public VersionRefEntityRefLang addVersionRef(@RequestBody VersionRefEntityRefLang versionRefEntityRefLang) {
        versionService.addVersionRef(versionRefEntityRefLang);
        return versionRefEntityRefLang;
    }


    @GetMapping("/find/{lang}/{id}")
    public VersionRefEntityRefLang findVersionRef(@PathVariable("lang") String lang, @PathVariable("id") Long id) {
        return versionService.findVersionRef(id, lang);
    }


    @PostMapping("/addInternationalisation/{lang}")
    public EntityRefLang addInternationalisation(@PathVariable("lang") String lang, @RequestBody EntityRefLang entityRefLang) {
        versionService.addInternationalisation(entityRefLang,lang);
        return entityRefLang;
    }

    @GetMapping("/getAllByCodeOrLabel/{value}/{lang}")
    public ResponseEntity getAllByReferenceOrLabel(
            @PathVariable("value") String value, @PathVariable("lang") String lang, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size)  {
        if (value.equals(" ") || value == null|| value.equals(""))
            return getAll(page, size, lang, "ASC");
        else
        return ResponseEntity.ok().body( versionService.filterByReferenceOrLabelProjection(value , PageRequest.of(page,size) , lang));
    }

//    @GetMapping("/{id}")
//    public ResponseEntity getById(@PathVariable("id") Long id){
//        return ResponseEntity.ok().body(versionService.findById(id));
//    }

    @PostMapping("/import-valid")
    public ResponseEntity<?> importValidFile(@RequestParam("file") MultipartFile file) {

        return versionService.saveFromExcelWithReturn(file);
    }

}
