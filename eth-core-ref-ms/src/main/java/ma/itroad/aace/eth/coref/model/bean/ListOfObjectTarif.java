package ma.itroad.aace.eth.coref.model.bean;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class ListOfObjectTarif implements Serializable {
    private Long entitId;
    private Long tarifBookId;
}
