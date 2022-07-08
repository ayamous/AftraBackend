package ma.itroad.aace.eth.coref.service;

import ma.itroad.aace.eth.core.rsql.service.IRsqlService;
import ma.itroad.aace.eth.core.service.IBaseService;
import ma.itroad.aace.eth.core.service.ImportDataService;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.CustomsOfficeRefBean;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.entity.CustomsOfficeRef;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.mapper.projections.CustomsOfficeRefLangProjection;
import ma.itroad.aace.eth.coref.service.helper.AgreementLang;
import ma.itroad.aace.eth.coref.service.helper.CustomsOfficeRefLang;
import ma.itroad.aace.eth.coref.service.impl.exceldto.CustomsOfficeRefExcelDTO;

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

public interface ICustomsOfficeRefService extends IBaseService<CustomsOfficeRef, CustomsOfficeRefBean>,
		IRsqlService<CustomsOfficeRef, CustomsOfficeRefBean>, ImportDataService {

	List<CustomsOfficeRef> excelToCustomsOfficeRef(InputStream is);

	ByteArrayInputStream customsOfficeRefToExcel(List<CustomsOfficeRef> customsOfficeRefs);

	ByteArrayInputStream customsOfficeRefLangsToExcel(List<CustomsOfficeRefExcelDTO> customsOfficeRefExcelDTOs);

	ByteArrayInputStream load();

	public ByteArrayInputStream load(String lang);

	Page<CustomsOfficeRefBean> getAll(final int page, final int size);

	CustomsOfficeRefBean addInternationalisation(CustomsOfficeRefLang customsOfficeRefLang);

	public CustomsOfficeRefLang findCustomsOffice(Long id, String lang);

	public EntityRefLang addInternationalisation(EntityRefLang entityRefLang, String lang);

	List<CustomsOfficeRef> saveAll(List<CustomsOfficeRef> customsOfficeRef);

	ErrorResponse delete(Long id);

	ErrorResponse deleteList(ListOfObject listOfObject);
	
	Page<CustomsOfficeRefLang> getAll(final int page, final int size, String codeLang, String orderDirection);
	ErrorResponse delete(Long id, String lang);

	Page<CustomsOfficeRefLang> filterByCodeOrLabel(String value, Pageable pageable, String codeLang);

	CustomsOfficeRefBean findById(Long id);

    Page<CustomsOfficeRefLangProjection> filterByCodeOrLabelProjection(String value, Pageable pageable, String lang);

	CustomsOfficeRef updateItem(Long id, CustomsOfficeRefLang customsOfficeRefLang);

	Set<ConstraintViolation<CustomsOfficeRefLang>> validateCustomsOfficeRef(CustomsOfficeRefLang customsOfficeRefBean);

	ResponseEntity<?> saveFromExcelAfterValidation(MultipartFile file);
}
