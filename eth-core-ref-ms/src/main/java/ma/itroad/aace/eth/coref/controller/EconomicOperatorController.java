package ma.itroad.aace.eth.coref.controller;
 
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ChapterRefBean;
import ma.itroad.aace.eth.coref.model.bean.TaxationBean;
import ma.itroad.aace.eth.coref.model.entity.EconomicOperator;
import ma.itroad.aace.eth.coref.service.impl.ImportDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ma.itroad.aace.eth.coref.model.bean.EconomicOperatorBean;
import ma.itroad.aace.eth.coref.model.mapper.EconomicOperatorMapper;
import ma.itroad.aace.eth.coref.service.IEconomicOperatorService;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping(value = "/economicoperators", consumes = { MediaType.ALL_VALUE }, produces = MediaTypes.HAL_JSON_VALUE)
@ResponseBody
public class EconomicOperatorController {

    @Autowired
    IEconomicOperatorService economicoperatorService;

    @Autowired
    EconomicOperatorMapper economicoperatorMapper;

    @Autowired
    ImportDataService importDataService;


    @GetMapping("/all")
    public ResponseEntity<Page<EconomicOperatorBean>>  getAll(@RequestParam(defaultValue = "0") Integer page,
                                                   @RequestParam(defaultValue = "10") Integer size){
        Page<EconomicOperatorBean> response = economicoperatorService.getAll(page, size);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/import")
    public ResponseEntity<String> importFile(@RequestParam("file") MultipartFile file) {

        importDataService.setService(economicoperatorService);
        ResponseEntity<String> imp = importDataService.importFile(file);
        return imp;
    }

    @GetMapping("/download")
    @ResponseBody
    public ResponseEntity<InputStreamResource> getFile() throws IOException {
        String filename = "economic_operator.xlsx";
        InputStreamResource file = new InputStreamResource(economicoperatorService.load());
        MediaType excelMediaType = new MediaType("application", "vnd.ms-excel");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }
    /**
     *
     * @param id the id of economic operator
     * @return {ResponseEntity}
     */
    @DeleteMapping("delete/{id}")
    @ResponseBody
    public ResponseEntity deleteEconomicOperator(@PathVariable("id") Long id) {
        ErrorResponse response = economicoperatorService.delete(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
   
}
