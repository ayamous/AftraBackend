package ma.itroad.aace.eth.coref.controller;


import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.view.PersonalContactVM;
import ma.itroad.aace.eth.coref.model.view.PortRefVM;
import ma.itroad.aace.eth.coref.service.impl.ImportDataService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import ma.itroad.aace.eth.coref.service.IPersonalContactService;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping(value = "/personalContacts", consumes = { MediaType.ALL_VALUE }, produces = MediaTypes.HAL_JSON_VALUE)
@ResponseBody
public class PersonalContactController {

    @Autowired
    ImportDataService importDataService;

    @Autowired
    IPersonalContactService personalContactService;

    @GetMapping("all")
    ResponseEntity<Page<PersonalContactVM>> getAll(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "10") int size) {

        Page<PersonalContactVM> response = personalContactService.getAll(page, size);
        return ResponseEntity.ok().body(response);

    }

    @PostMapping("/import")
    public ResponseEntity<String> importFile(@RequestParam("file") MultipartFile file) {

        importDataService.setService(personalContactService);
        return importDataService.importFile(file);
    }

    @GetMapping("/download")
    @ResponseBody
    public ResponseEntity<InputStreamResource> getFile() throws IOException {
        String filename = "PersonalContacts.xlsx";
        InputStreamResource file = new InputStreamResource(personalContactService.load());
        MediaType excelMediaType = new MediaType("application", "vnd.ms-excel");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);


    }

    @DeleteMapping("delete/{id}")
    @ResponseBody
    public ResponseEntity deletePersonalContact(@PathVariable("id") Long id) {
        ErrorResponse response = personalContactService.delete(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @DeleteMapping("/deleteList")
    @ResponseBody
    public ResponseEntity deleteList(@RequestBody ListOfObject listOfObject) {
        ErrorResponse response = personalContactService.deleteList(listOfObject);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
   
}
