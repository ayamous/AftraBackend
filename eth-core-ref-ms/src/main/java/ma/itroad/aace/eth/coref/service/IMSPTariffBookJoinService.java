package ma.itroad.aace.eth.coref.service;

import ma.itroad.aace.eth.core.service.ImportDataService;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.bean.ListOfObjectTarifBook;
import ma.itroad.aace.eth.coref.model.mapper.projections.MSPTariffBookRefVMProjection;
import ma.itroad.aace.eth.coref.model.view.MSPTariffBookRefVM;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;

public interface IMSPTariffBookJoinService extends ImportDataService {

    Page<MSPTariffBookRefVM> getAll(final int page, final int size, String codeLang);

    ByteArrayInputStream mspTariffBookJoinToExcel(List<MSPTariffBookRefVM> mspTariffBookJoinVMs);

    List<MSPTariffBookRefVM> excelToElemetsRefs(InputStream is);

     MSPTariffBookRefVM save(MSPTariffBookRefVM model) ;

    Page<MSPTariffBookRefVM> findMspTarifBookJoin(final int page, final int size, String  reference);
    
     ByteArrayInputStream load(String lang);

     Page<MSPTariffBookRefVM> findMspTarifBookJoin(Long id, String lang, final int page, final int size);
    
    Page<MSPTariffBookRefVM> getAll(String lang, final int page, final int size, String orderDirection);

    ErrorResponse delete(Long id, Long tarif_id);

    ErrorResponse deleteList(ListOfObjectTarifBook listOfObjectTarifBook);

	Set<ConstraintViolation<MSPTariffBookRefVM>> validatemSPTariffBookRefVM(MSPTariffBookRefVM mSPTariffBookRefVM);

	ResponseEntity<?> saveFromExcelAfterValidation(MultipartFile file);
}
