package ma.itroad.aace.eth.coref.service;

import ma.itroad.aace.eth.core.service.ImportDataService;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.bean.ListOfObjectTarifBook;
import ma.itroad.aace.eth.coref.model.mapper.RegulationTariffBookRefVMProjection;
import ma.itroad.aace.eth.coref.model.view.RegulationTariffBookRefVM;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;

public interface IRegulationTariffBookJoinService extends ImportDataService {

    Page<RegulationTariffBookRefVM> getAll(final int page, final int size, String codeLang);

    ByteArrayInputStream regulationTariffBookJoinToExcel(List<RegulationTariffBookRefVM> regulationTariffBookRefVMs);

    List<RegulationTariffBookRefVM> excelToElemetsRefs(InputStream is);

    RegulationTariffBookRefVM save(RegulationTariffBookRefVM model) ;

    Page<RegulationTariffBookRefVM> findRegulationTarifBookJoin(final int page, final int size, String  reference);

    ByteArrayInputStream load(String lang);

    Page<RegulationTariffBookRefVM> findRegulationTarifBookJoin(Long id, String lang, final int page, final int size);

    Page<RegulationTariffBookRefVM> getAll(String lang, final int page, final int size, String orderDirection);

    ErrorResponse delete(Long id, Long tarif_id);

    ErrorResponse deleteList(ListOfObjectTarifBook listOfObjectTarifBook);

	Set<ConstraintViolation<RegulationTariffBookRefVM>> validateRegulationTariffBookRefVM(
			RegulationTariffBookRefVM regulationTariffBookRefVM);

	ResponseEntity<?> saveFromExcelAfterValidation(MultipartFile file);
}
