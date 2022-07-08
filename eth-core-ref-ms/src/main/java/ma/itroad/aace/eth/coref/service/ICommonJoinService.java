package ma.itroad.aace.eth.coref.service;

import ma.itroad.aace.eth.core.service.ImportDataService;
import org.springframework.data.domain.Page;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public interface ICommonJoinService<V extends Serializable> extends ImportDataService {

    Page<V> getAll(final int page, final int size);
    Collection<V> excelToElementsRefs(InputStream is);
}
