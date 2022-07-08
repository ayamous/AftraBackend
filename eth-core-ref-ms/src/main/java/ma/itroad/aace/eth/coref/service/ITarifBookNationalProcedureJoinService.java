package ma.itroad.aace.eth.coref.service;

import ma.itroad.aace.eth.core.service.ImportDataService;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ListOfObjectTarifBook;
import ma.itroad.aace.eth.coref.model.bean.TariffBookRefBean;
import ma.itroad.aace.eth.coref.model.mapper.projections.TarifBookNationalProcedureVMProjection;
import ma.itroad.aace.eth.coref.model.view.TarifBookNationalProcedureVM;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;

public interface ITarifBookNationalProcedureJoinService extends ImportDataService {

    Page<TarifBookNationalProcedureVM> getAll(final int page, final int size, String codeLang);

    ByteArrayInputStream tarifBookNationalProcedureJoinToExcel(List<TarifBookNationalProcedureVM> tarifBookNationalProcedureVMs);

    List<TarifBookNationalProcedureVM> excelToElemetsRefs(InputStream is);

    TariffBookRefBean save(TarifBookNationalProcedureVM model) ;

    Page<TarifBookNationalProcedureVM> findTarifBookNationalProcedure(final int page, final int size, String  reference);

    ByteArrayInputStream load(String codeLang, final int page, final int size);

    Page<TarifBookNationalProcedureVM> findTarifBookNationalProcedureVMByLang(Long id, String lang, final int page, final int size);

    Page<TarifBookNationalProcedureVM> getAll(String lang, final int page, final int size, String orderDirection);

    ErrorResponse delete(Long id, Long tarif_id);

    ErrorResponse deleteList(ListOfObjectTarifBook listOfObjectTarifBook);

	Set<ConstraintViolation<TarifBookNationalProcedureVM>> validateTarifBookNationalProcedureVM(
			TarifBookNationalProcedureVM tarifBookNationalProcedureVM);

	ResponseEntity<?> saveFromExcelAfterValidation(MultipartFile file);
}
