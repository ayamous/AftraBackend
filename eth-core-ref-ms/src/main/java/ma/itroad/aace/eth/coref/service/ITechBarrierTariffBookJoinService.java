package ma.itroad.aace.eth.coref.service;

import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ListOfObjectTarifBook;
import ma.itroad.aace.eth.coref.model.view.CountryGroupReferenceVM;
import ma.itroad.aace.eth.coref.model.view.TarifBookTaxationVM;
import ma.itroad.aace.eth.coref.model.view.TechBarrierTariffBookVM;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface ITechBarrierTariffBookJoinService extends ICommonJoinService<TechBarrierTariffBookVM> {
    Page<TechBarrierTariffBookVM> getAll(final int page, final int size, String codeLang);

    public TechBarrierTariffBookVM save(TechBarrierTariffBookVM item) ;

    Page<TechBarrierTariffBookVM> findTechBarTarifBookJoin(final int page, final int size, String  reference);

    ErrorResponse delete(Long id, Long tarif_id);

    ErrorResponse deleteList(ListOfObjectTarifBook listOfObjectTarifBook);

	ByteArrayInputStream TechBarrierTariffBookVMToExcel(List<TechBarrierTariffBookVM> techBarrierTariffBookVM);

	Set<ConstraintViolation<TechBarrierTariffBookVM>> validatetechBarrierTariffBookVM(
			TechBarrierTariffBookVM techBarrierTariffBookVM);

	ResponseEntity<?> saveFromExcelAfterValidation(MultipartFile file);

}
