package ma.itroad.aace.eth.core.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;

public interface ImportDataService {
    void saveFromExcel(MultipartFile file);
    ByteArrayInputStream load();
}
