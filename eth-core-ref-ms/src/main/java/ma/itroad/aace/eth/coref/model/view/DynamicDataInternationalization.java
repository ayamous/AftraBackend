package ma.itroad.aace.eth.coref.model.view;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class DynamicDataInternationalization implements Serializable {

    String label;
    String description;
}
