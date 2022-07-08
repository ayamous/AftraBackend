package ma.itroad.aace.eth.coref.service.helper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class PageHelper {

    /**
     * Convert list to paged page
     *
     * @param list     list
     * @param total    total number of records
     * @param pageable paging information
     * @return
     */
    public static <T> Page<T> listConvertToPage(List<T> list, long total, Pageable pageable) {
        // The position of the first data of the current page in the List
        int start = (int) pageable.getOffset();//(pageable.getPageNumber() - 1) * pageable.getPageSize();
        // The position of the last data of the current page in the List
        int end = Math.min((start + pageable.getPageSize()), list.size());
        //System.out.println(start + " , " + end + ", size:" + list.size());
        // Configure paging data
        return new PageImpl<T>(list.subList(start, end), pageable, list.size());

    }
}
