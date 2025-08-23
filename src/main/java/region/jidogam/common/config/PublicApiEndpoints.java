package region.jidogam.common.config;

/**
 * 인증 없이 접근 가능한 공개 API 엔드포인트들을 관리하는 클래스
 */
public final class PublicApiEndpoints {

  // 인증 관련 API
  public static final String AUTH_LOGIN = "/api/auth/login";
  public static final String AUTH_REFRESH = "/api/auth/refresh";

  // 사용자 등록 API
  public static final String USERS_CREATE = "/api/users";

  // 닉네임, 이메일 중복 체크 API
  public static final String CHECK_NICKNAME  = "/api/users/check-nickname";
  public static final String CHECK_EMAIL = "/api/users/check-email";

  // 공개 조회 API
  public static final String SWAGGER_UI = "/swagger-ui/**";

  /**
   * 모든 공개 POST API 엔드포인트를 반환
   */
  public static String[] getPublicPostEndpoints() {
    return new String[]{
        AUTH_LOGIN,
        AUTH_REFRESH,
        USERS_CREATE
    };
  }

  /**
   * 모든 공개 GET API 엔드포인트를 반환
   */
  public static String[] getPublicGetEndpoints() {
    return new String[]{
        CHECK_NICKNAME,
        CHECK_EMAIL,
        SWAGGER_UI
    };
  }
}
