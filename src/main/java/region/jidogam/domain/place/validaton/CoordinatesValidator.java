package region.jidogam.domain.place.validaton;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 위도 경도 동시 입력 검증 어노테이션
 */
public class CoordinatesValidator implements
    ConstraintValidator<ValidCoordinates, Object> {

  @Override
  public void initialize(ValidCoordinates constraintAnnotation) {
    ConstraintValidator.super.initialize(constraintAnnotation);
  }

  @Override
  public boolean isValid(Object obj, ConstraintValidatorContext context) {
    if (obj == null) {
      return true;
    }

    try {
      Method latMethod = obj.getClass().getMethod("lat");
      Method lonMethod = obj.getClass().getMethod("lon");

      Double lat = (Double) latMethod.invoke(obj);
      Double lon = (Double) lonMethod.invoke(obj);

      boolean bothNull = (lat == null && lon == null);
      boolean bothPresent = (lat != null && lon != null);

      return bothNull || bothPresent;

    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      return false;
    }
  }
}
