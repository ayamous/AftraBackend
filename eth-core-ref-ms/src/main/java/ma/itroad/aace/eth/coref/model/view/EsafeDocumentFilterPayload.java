package ma.itroad.aace.eth.coref.model.view;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EsafeDocumentFilterPayload {
    private String title;
    private String auteur;
    private String reference;
    private String ecoCode;
    private String creationDate;
}
