package ma.itroad.aace.eth.coref.service;

import ma.itroad.aace.eth.core.service.ImportDataService;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ListOfObjectTarifBook;
import ma.itroad.aace.eth.coref.model.bean.TariffBookRefBean;
import ma.itroad.aace.eth.coref.model.mapper.projections.TarifBookTaxationVMProjection;
import ma.itroad.aace.eth.coref.model.view.TarifBookTaxationVM;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;

public interface ITariffBookRefTaxationJoinService extends ImportDataService {

    Page<TarifBookTaxationVM> getAll(final int page, final int size, String codeLang);

    ByteArrayInputStream tarifBookTaxationJoinToExcel(List<TarifBookTaxationVM> tarifBookTaxationVMs);

    List<TarifBookTaxationVM> excelToElemetsRefs(InputStream is);

    TariffBookRefBean save(TarifBookTaxationVM model) ;

    Page<TarifBookTaxationVMProjection> findTarifTaxation(final int page, final int size, String  reference);

    ByteArrayInputStream load(String codeLang, final int page, final int size);

    Page<TarifBookTaxationVM> findTarifTaxationJoin(Long id, String lang, final int page, final int size);

    Page<TarifBookTaxationVM> getAll(String lang, final int page, final int size, String orderDirection);

    ErrorResponse delete(Long id, Long tarif_id);

    ErrorResponse deleteList(ListOfObjectTarifBook listOfObjectTarifBook);

	Set<ConstraintViolation<TarifBookTaxationVM>> validateTarifBookTaxationVM(TarifBookTaxationVM tarifBookTaxationVM);

	ResponseEntity<?> saveFromExcelAfterValidation(MultipartFile file);
}
