package ma.itroad.aace.eth.coref.controller;

import ma.itroad.aace.eth.coref.model.view.ContactUserAccountVM;
import ma.itroad.aace.eth.coref.service.IContactUserAccountJoinService;
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
@RequestMapping("/contactUserAccountJoin")
public class ContactUserAccountJoinController {

    private final static String fileName = "relation-contact.xlsx";

    @Autowired
    private IContactUserAccountJoinService iContactUserAccountJoinService;

    @Autowired
    private ImportDataService importDataService;


    @GetMapping("all")
    ResponseEntity<Page<ContactUserAccountVM>> getAll(@RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "10") int size) {
        Page<ContactUserAccountVM> response = iContactUserAccountJoinService.getAll(page, size);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/import")
    public ResponseEntity<String> importFile(@RequestParam("file") MultipartFile file) {
        importDataService.setService(iContactUserAccountJoinService);
        return importDataService.importFile(file);
    }

    @GetMapping("/download")
    @ResponseBody
    public ResponseEntity<InputStreamResource> getFile() throws IOException {
        InputStreamResource file = new InputStreamResource(iContactUserAccountJoinService.load());
        MediaType excelMediaType = new MediaType("application", "vnd.ms-excel");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; fileName=" + fileName)
                .contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }
}
