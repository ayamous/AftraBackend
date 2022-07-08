package ma.itroad.aace.eth.coref.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImportDataService<S extends ma.itroad.aace.eth.core.service.ImportDataService> {

    private S service;

    public void setService(S service) {
        this.service = service;
    }

    public S getService() {
        return service;
    }


    public ResponseEntity<?> importFile(MultipartFile file) {

        String message;
        try {
            service.saveFromExcel(file);
            message = "Uploaded the file successfully: " + file.getOriginalFilename();
            return ResponseEntity.status(HttpStatus.OK).body(message);
        } catch (Exception e) {
            message = "Could not upload the file: " + file.getOriginalFilename() + "!";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
        }
    }
}
