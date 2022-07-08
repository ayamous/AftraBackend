package ma.itroad.aace.eth.coref.service.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import ma.itroad.aace.eth.coref.model.entity.VersionRef;
import ma.itroad.aace.eth.coref.repository.VersionRefRepository;

public class VersionConstraintValidator implements ConstraintValidator<VersionConstraints, String> {

	@Autowired
	private VersionRefRepository versionRefRepository;

	@Override
	public void initialize(VersionConstraints version) {
	}

	@Override
	public boolean isValid(String version, ConstraintValidatorContext context) {

		VersionRef versionValue = versionRefRepository.findByVersion(version);

		return versionValue != null;
	}
}