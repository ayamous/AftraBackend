package ma.itroad.aace.eth.coref.controller;

import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.UserAccountBean;
import ma.itroad.aace.eth.coref.model.entity.UserAccount;
import ma.itroad.aace.eth.coref.service.IUserAccountService;
import ma.itroad.aace.eth.coref.service.impl.ImportDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping(value = "/userAccounts", consumes = {MediaType.ALL_VALUE}, produces = MediaTypes.HAL_JSON_VALUE)
public class UserAccountController {

    @Autowired
    private IUserAccountService iUserAccountService;

    @Autowired
    private ImportDataService importDataService;

    @GetMapping("/all")
    public ResponseEntity<Page<UserAccountBean>> getAllTaxRefs(@RequestParam(defaultValue = "0") Integer page,
                                                               @RequestParam(defaultValue = "10") Integer size, @RequestParam(defaultValue = "DESC") String orderDirection) {
        Page<UserAccountBean> response = iUserAccountService.getAll(page, size);
        return ResponseEntity.ok().body(response);
    }


    @PostMapping("/import")
    public ResponseEntity<String> importFile(@RequestParam("file") MultipartFile file) {
        importDataService.setService(iUserAccountService);
        return importDataService.importFile(file);
    }

    @GetMapping("/download")
    @ResponseBody
    public ResponseEntity<InputStreamResource> getFile() throws IOException {
        String filename = "utilisateurs.xlsx";
        InputStreamResource file = new InputStreamResource(iUserAccountService.load());
        MediaType excelMediaType = new MediaType("application", "vnd.ms-excel");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    @DeleteMapping("delete/{id}")
    @ResponseBody
    public ResponseEntity deleteUserAccount(@PathVariable("id") Long id) {
        ErrorResponse response = iUserAccountService.delete(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

}
