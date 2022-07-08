package ma.itroad.aace.eth.coref.service;

import ma.itroad.aace.eth.core.rsql.service.IRsqlService;
import ma.itroad.aace.eth.core.service.IBaseService;
import ma.itroad.aace.eth.core.service.ImportDataService;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.EconomicOperatorBean;
import ma.itroad.aace.eth.coref.model.bean.OrganizationBean;
import ma.itroad.aace.eth.coref.model.entity.ChapterRef;
import ma.itroad.aace.eth.coref.model.entity.EconomicOperator;
import org.springframework.data.domain.Page;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

public interface IEconomicOperatorService extends
        IBaseService<EconomicOperator, EconomicOperatorBean>,
        IRsqlService<EconomicOperator, EconomicOperatorBean> , ImportDataService {

    ByteArrayInputStream economicOperatorToExcel(List<EconomicOperator> economicOperators);
    List<EconomicOperator> excelToEconomicOperator(InputStream is);
    Page<EconomicOperatorBean> getAll(final int page, final int size);
    ByteArrayInputStream load();
    ErrorResponse delete(Long id);
}
