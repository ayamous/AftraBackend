package ma.itroad.aace.eth.coref.model.view;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.itroad.aace.eth.coref.model.entity.Organization;
import ma.itroad.aace.eth.coref.model.enums.ContactType;
import ma.itroad.aace.eth.coref.model.enums.Occupation;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PersonalContactVM {
    private Long id;
    private String reference;
    private String firstName;
    private String lastName;
    private String mobileNumber;
    private String phoneNumber;
    private String adress;
    private String email;
    private String faxNumber;


    private ContactType contactType;

    private Occupation occupation;

    private String organizationRef;

    private Long organizationId;

}
