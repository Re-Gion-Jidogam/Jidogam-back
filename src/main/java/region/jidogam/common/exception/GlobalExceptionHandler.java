package region.jidogam.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import region.jidogam.common.dto.response.ResponseDto;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  // validation
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ResponseDto<Void>> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex) {

    CommonErrorCode commonErrorCode = CommonErrorCode.INVALIED_INPUT_VALUE;
    FieldError fieldError = ex.getFieldErrors().get(0);

    log.info("Validation failed: {} - {}", fieldError.getField(),
        fieldError.getRejectedValue());

    return createErrorResponse(commonErrorCode, ex.getMessage());
  }

  // request method
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  protected ResponseEntity<ResponseDto<Void>> handleHttpRequestMethodNotSupport(
      HttpRequestMethodNotSupportedException ex) {

    log.info("Request method not supported: {}", ex.getMethod());

    CommonErrorCode commonErrorCode = CommonErrorCode.METHOD_NOT_ALLOWED;

    return createErrorResponse(commonErrorCode, ex.getMessage());
  }

  // resource : 잘못된 uri
  @ExceptionHandler(NoResourceFoundException.class)
  protected ResponseEntity<ResponseDto<Void>> handleNoResourceFoundException(
      NoResourceFoundException ex) {

    log.info("Request for unsupported URI: {}", ex.getResourcePath());

    CommonErrorCode commonErrorCode = CommonErrorCode.URI_NOT_FOUND;

    return createErrorResponse(commonErrorCode, ex.getMessage());
  }

  // 잘못된 입력 값 : 타입 불일치 등의 json 파싱 실패
  @ExceptionHandler(HttpMessageNotReadableException.class)
  protected ResponseEntity<ResponseDto<Void>> handleHttpMessageNotReadableException(
      HttpMessageNotReadableException ex) {

    log.info("Request with invalid JSON format: {} | Error: {}",
        ex.getHttpInputMessage(), ex.getMostSpecificCause().getMessage());

    CommonErrorCode commonErrorCode = CommonErrorCode.INVALID_JSON_FORMAT;

    return createErrorResponse(commonErrorCode, ex.getMessage());
  }

  // business
  @ExceptionHandler(JidogamException.class)
  protected ResponseEntity<ResponseDto<Void>> handleBusinessException(JidogamException ex) {

    CommonErrorCode commonErrorCode = ex.getCommonErrorCode();

    return createErrorResponse(commonErrorCode, ex.getMessage());
  }

  // other
  @ExceptionHandler(Exception.class)
  protected ResponseEntity<ResponseDto<Void>> handleUnexpectedException(Exception ex) {

    log.error("Exception", ex);

    CommonErrorCode commonErrorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;

    return createErrorResponse(commonErrorCode, ex.getMessage());
  }

  // method level security
//  @ExceptionHandler(AuthorizationDeniedException.class)
//  protected ResponseEntity<ResponseDto<Void>> handleAuthorizationDeniedException(
//      AuthorizationDeniedException ex,
//      HttpServletRequest request,
//      @AuthenticationPrincipal CustomUserDetails principal
//  ) {
//    log.warn("Authorization denied: {}, path: {}, userId: {}, role: {}",
//        ex.getMessage(),
//        request.getRequestURI(),
//        principal != null ? principal.getId() : "anonymous",
//        principal != null ? principal.getAuthorities() : "anonymous"
//    );
//
//    AuthErrorCode errorCode = AuthErrorCode.ACCESS_DENIED;
//
//    return createErrorResponse(errorCode.getHttpStatus(), errorResponse);
//  }

  // auth
//  @ExceptionHandler(AuthException.class)
//  protected ResponseEntity<ResponseDto<Void>> handleAuthException(AuthException ex,
//      HttpServletResponse response) {
//
//    ErrorCode errorCode = ex.getErrorCode();
//
//    log.info("Authentication failed : {} | Error: {})",
//        errorCode, errorCode.getMessage());
//
//    // refresh token 쿠키 무효화
//    Cookie cookie = CookieUtil.expireRefreshTokenCookie();
//    response.addCookie(cookie);
//
//    return createErrorResponse(errorCode.getHttpStatus(), errorResponse);
//  }

  // 응답
  private ResponseEntity<ResponseDto<Void>> createErrorResponse(ErrorCode errorCode, String message) {
    ResponseDto<Void> responseDto = ResponseDto.error(errorCode.getCode(), message);
    return ResponseEntity
        .status(errorCode.getStatus())
        .contentType(MediaType.APPLICATION_JSON)
        .body(responseDto);
  }
}
