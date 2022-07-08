package ma.itroad.aace.eth.coref.service;

import ma.itroad.aace.eth.core.rsql.service.IRsqlService;
import ma.itroad.aace.eth.core.service.IBaseService;
import ma.itroad.aace.eth.core.service.ImportDataService;
import ma.itroad.aace.eth.coref.model.bean.VersionTariffBookBean;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.entity.VersionTariffBookRef;
import ma.itroad.aace.eth.coref.model.view.MSPTariffBookRefVM;
import ma.itroad.aace.eth.coref.model.view.VersionTariffBookRefVM;
import ma.itroad.aace.eth.coref.service.helper.VersionTarrifBookRefLang;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;

import ma.itroad.aace.eth.coref.exception.ErrorResponse;

public interface IVersionTariffBookRefService extends IBaseService<VersionTariffBookRef, VersionTariffBookBean>,
		IRsqlService<VersionTariffBookRef, VersionTariffBookBean>, ImportDataService {

	//ByteArrayInputStream load(String lang);

	ByteArrayInputStream load(String codeLang, final int page, final int size);

	//Page<VersionTariffBookBean> getAll(final int page, final int size);
	Page<VersionTariffBookRefVM> getAll(final int page, final int size);

	public Page<VersionTariffBookBean> getAll(String lang, int page, int size, String orderDirection);

	public Collection<VersionTariffBookRefVM> excelToElementsRefs(InputStream is);

	///public ErrorResponse delete(Long id, String lang);

	public void addInternationalisation(EntityRefLang entityRefLang, String lang);

	public VersionTarrifBookRefLang findVersionTariffBook(Long id, String lang);

	public VersionTariffBookBean addInternationalisation(VersionTarrifBookRefLang versionTarrifBookRefLang);

	public ByteArrayInputStream versionTarifBookToExcel(List<VersionTarrifBookRefLang> versionTarrifBookRefLang);

	VersionTariffBookBean add(VersionTariffBookRefVM versionTariffBookRefVM);

    VersionTariffBookBean findById(Long id);

	ErrorResponse delete(Long id);

	Set<ConstraintViolation<VersionTariffBookRefVM>> validateVersionTariffBookRefVM(
			VersionTariffBookRefVM versionTariffBookRefVM);

	ResponseEntity<?> saveFromExcelAfterValidation(MultipartFile file);
}
