package ma.itroad.aace.eth.coref.service;

import ma.itroad.aace.eth.core.service.ImportDataService;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.CustomsRegimRefBean;
import ma.itroad.aace.eth.coref.model.bean.ListOfObjectTarifBook;
import ma.itroad.aace.eth.coref.model.view.CustomsRegimMSPVM;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;

public interface ICustomsRegimMSPJoinService extends ImportDataService {

    Page<CustomsRegimMSPVM> getAll(final int page, final int size, String codeLang);

    ByteArrayInputStream customsRegimMSPJoinToExcel(List<CustomsRegimMSPVM> customsRegimMSPVMS);

    List<CustomsRegimMSPVM> excelToElemetsRefs(InputStream is);

    public CustomsRegimMSPVM save(CustomsRegimMSPVM model) ;

    Page<CustomsRegimMSPVM> findCustomRegimeMspJoin(final int page, final int size, String  reference);
    
    public ByteArrayInputStream load(String lang);

    public Page<CustomsRegimMSPVM> findCustomRegimeMspJoin(Long id, String lang, final int page, final int size);
    
    Page<CustomsRegimMSPVM> getAll(String lang, final int page, final int size, String orderDirection);

    ErrorResponse delete(Long id, Long tarif_id);

    ErrorResponse deleteList(ListOfObjectTarifBook listOfObjectTarifBook);

	Set<ConstraintViolation<CustomsRegimMSPVM>> validateCustomsRegimMSPVM(CustomsRegimMSPVM customsRegimMSPVM);

	ResponseEntity<?> saveFromExcelAfterValidation(MultipartFile file);
}
