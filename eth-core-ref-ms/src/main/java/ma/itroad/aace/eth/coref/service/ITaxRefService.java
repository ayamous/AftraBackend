package ma.itroad.aace.eth.coref.service;

import ma.itroad.aace.eth.core.rsql.service.IRsqlService;
import ma.itroad.aace.eth.core.service.IBaseService;
import ma.itroad.aace.eth.core.service.ImportDataService;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.bean.TaxRefBean;
import ma.itroad.aace.eth.coref.model.entity.TaxRef;
import ma.itroad.aace.eth.coref.model.view.TaxRefVM;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

public interface ITaxRefService extends
        IBaseService<TaxRef, TaxRefBean>,
        IRsqlService<TaxRef, TaxRefBean>,
        ImportDataService {

    Page<TaxRefVM> getAll(final int page, final int size, String orderDirection);

    ByteArrayInputStream taxRefsToExcel(List<TaxRef> taxRefs);

    List<TaxRef> excelToTaxRefs(InputStream is);

    ByteArrayInputStream load();

    ErrorResponse delete(Long id);

    ErrorResponse deleteList(ListOfObject listOfObject);

    TaxRefBean findById(Long id);

    Page<TaxRefBean> findByCode(String code, Pageable pageable);


    ResponseEntity<?> saveFromExcelWithReturn(MultipartFile file);
}