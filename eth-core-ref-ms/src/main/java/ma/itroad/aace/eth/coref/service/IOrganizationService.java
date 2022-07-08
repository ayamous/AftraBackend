package ma.itroad.aace.eth.coref.service;

import ma.itroad.aace.eth.core.rsql.service.IRsqlService;
import ma.itroad.aace.eth.core.service.IBaseService;
import ma.itroad.aace.eth.core.service.ImportDataService;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.bean.OrganizationBean;
import ma.itroad.aace.eth.coref.model.entity.Organization;
import ma.itroad.aace.eth.coref.service.helper.OrganizationHelper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

public interface IOrganizationService extends
        IBaseService<Organization, OrganizationBean>,
        IRsqlService<Organization, OrganizationBean>, ImportDataService {

    Page<OrganizationHelper> getAll(final int page, final int size, String orderDirection);

    List<Organization> excelToOrganizations(InputStream is);

    ByteArrayInputStream organizationsToExcel(List<Organization> organizations);

    ErrorResponse delete(Long id);

    ErrorResponse deleteList(ListOfObject listOfObject);

    Page<OrganizationHelper> searchOrganizationByKeyWord(int page, int size, String keyWord, String orderDirection);

    Page<OrganizationHelper> filterByReferenceOrName(String value, Pageable pageable);

    OrganizationBean findById(Long id);

    ResponseEntity<?> saveFromExcelWithReturn(MultipartFile file);

}
