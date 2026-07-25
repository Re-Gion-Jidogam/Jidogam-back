package region.jidogam.domain.area.dto;

/**
 * 시군구의 code는 시도 code까지 포함된 완전한 코드(5자리)여야 함.
 */
public record Sigungu(

    String name,
    String code
) {

}
