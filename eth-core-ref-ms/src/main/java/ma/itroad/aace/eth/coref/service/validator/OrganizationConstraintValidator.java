package ma.itroad.aace.eth.coref.service.validator;

import ma.itroad.aace.eth.coref.model.entity.Organization;
import ma.itroad.aace.eth.coref.repository.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class OrganizationConstraintValidator implements ConstraintValidator<OrganizationConstraints, String> {

    @Autowired
    private OrganizationRepository organizationRepository;

@Override
public void initialize(OrganizationConstraints organization) {
        }

@Override
public boolean isValid(String organization, ConstraintValidatorContext context) {

    Organization codeValue = organizationRepository.findByReference(organization);

        return codeValue != null;
        }
}

