package region.jidogam.infrastructure.place.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SemasStoreItem(

    String bizesId,      // 상가업소번호 (업소 고유 식별자)
    String bizesNm,       // 상호명
    String brchNm,        // 지점명
    String indsLclsCd,    // 상권업종대분류코드
    String indsLclsNm,    // 상권업종대분류명
    String indsMclsCd,    // 상권업종중분류코드
    String indsMclsNm,    // 상권업종중분류명
    String indsSclsCd,    // 상권업종소분류코드
    String indsSclsNm,    // 상권업종소분류명
    String ctprvnCd,       // 시도코드
    String ctprvnNm,       // 시도명
    String signguCd,       // 시군구코드
    String signguNm,       // 시군구명
    String adongCd,        // 행정동코드
    String adongNm,        // 행정동명
    String ldongCd,        // 법정동코드
    String ldongNm,        // 법정동명
    String lnoCd,          // 지번코드
    String plotSctCd,      // 대지구분코드 (1: 대지, 2: 산 등)
    String plotSctNm,      // 대지구분명
    String lnoMnno,        // 지번본번지
    String lnoSlno,        // 지번부번지
    String lnoAdr,          // 지번주소
    String rdnmCd,          // 도로명코드
    String rdnm,            // 도로명
    String bldMnno,         // 건물본번지
    String bldSlno,         // 건물부번지
    String bldMngNo,        // 건물관리번호
    String bldNm,           // 건물명
    String rdnmAdr,         // 도로명주소
    String oldZipcd,        // 구우편번호 (6자리, 개편 전)
    String newZipcd,        // 신우편번호 (5자리)
    String dongNo,          // 동정보 (건물 내 동 번호)
    String flrNo,           // 층정보
    String hoNo,            // 호정보
    BigDecimal lon,         // 경도
    BigDecimal lat          // 위도
) {

}