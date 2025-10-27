package region.jidogam.domain.place.validaton;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import region.jidogam.domain.place.dto.PlaceSearchRequest;

/**
 * 위도 경도 동시 입력 검증 어노테이션
 */
public class CoordinatesValidator implements
    ConstraintValidator<ValidCoordinates, PlaceSearchRequest> {

  @Override
  public void initialize(ValidCoordinates constraintAnnotation) {
    ConstraintValidator.super.initialize(constraintAnnotation);
  }

  @Override
  public boolean isValid(PlaceSearchRequest request, ConstraintValidatorContext context) {

    if (request == null) {
      return true;
    }

    Double lat = request.lat();
    Double lng = request.lng();

    boolean bothNull = (lat == null && lng == null);
    boolean bothPresent = (lat != null && lng != null);

    return bothNull || bothPresent;
  }
}
