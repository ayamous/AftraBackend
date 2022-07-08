package ma.itroad.aace.eth.coref.service;

import ma.itroad.aace.eth.core.rsql.service.IRsqlService;
import ma.itroad.aace.eth.core.service.IBaseRefService;
import ma.itroad.aace.eth.core.service.IBaseService;
import ma.itroad.aace.eth.core.service.ImportDataService;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.bean.TariffBookRefBean;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.entity.TarifBookRef;
import ma.itroad.aace.eth.coref.model.mapper.projections.TariffBookRefLangProjection;
import ma.itroad.aace.eth.coref.service.helper.TariffBookRefLang;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;

public interface ITarifBookRefService extends
		IBaseService<TarifBookRef, TariffBookRefBean>,
		IBaseRefService,
		IRsqlService<TarifBookRef, TariffBookRefBean>, ImportDataService {

	Page<TariffBookRefLang> getAll(final int page, final int size, String codeLang, String orderDirection);

	List<TarifBookRef> saveAll(List<TarifBookRef> tarifBookRefs);

	ErrorResponse delete(Long id);

	ErrorResponse deleteList(ListOfObject listOfObject);

	ErrorResponse deleteInternationalisation(String codeLang, Long tarifBookRefId) ;

	ByteArrayInputStream tariffBookRefsToExcel(List<TariffBookRefLang> tariffBookRefLangs);

	List<TariffBookRefLang> excelToTariffBookRefs(InputStream is);

	//ByteArrayInputStream load(String codeLang, final int page, final int size);

	ByteArrayInputStream load(String codeLang, final int page, final int size, String orderDirection);

	void addTarifBookRef(TariffBookRefLang chapterRefLang);

	TariffBookRefLang findTariffBook(Long id, String lang);

	void addInternationalisation(EntityRefLang entityRefLang, String lang);

	Page<TariffBookRefLang> filterByReferenceOrLabel(String value, Pageable pageable, String codeLang);

	Page<TariffBookRefLangProjection> filterByReferenceOrLabelProjection(String value, String lang, Pageable pageable);

	TarifBookRef updateTarifBookRef(Long id,TariffBookRefLang tariffBookRefLang);

    TariffBookRefBean findById(Long id);

	ResponseEntity<?> saveFromExcelWithReturn(MultipartFile file);

	Set<ConstraintViolation<TariffBookRefLang>> validateItems(TariffBookRefLang tariffBookRefLang);
}
