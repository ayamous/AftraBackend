package ma.itroad.aace.eth.coref.service;
 
import ma.itroad.aace.eth.core.rsql.service.IRsqlService;
import ma.itroad.aace.eth.core.service.IBaseService;
import ma.itroad.aace.eth.core.service.ImportDataService;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.bean.PersonalContactBean;
import ma.itroad.aace.eth.coref.model.entity.PersonalContact;
import ma.itroad.aace.eth.coref.model.view.PersonalContactVM;
import org.springframework.data.domain.Page;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;


public interface IPersonalContactService extends
        IBaseService<PersonalContact,PersonalContactBean>,
        IRsqlService<PersonalContact,PersonalContactBean> , ImportDataService {

    List<PersonalContact> excelToPersonalContact(InputStream is);
    ByteArrayInputStream personalContactToExcel(List<PersonalContact> personalContacts);
    Page<PersonalContactVM> getAll(final int page, final int size);
    ErrorResponse delete(Long id);
    ErrorResponse deleteList(ListOfObject listOfObject);

}
