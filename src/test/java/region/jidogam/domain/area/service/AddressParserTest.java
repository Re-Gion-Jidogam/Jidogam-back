package region.jidogam.domain.area.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import region.jidogam.domain.area.dto.api.AddressInfo;
import region.jidogam.domain.area.parser.AddressParser;
import region.jidogam.domain.area.parser.SidoNormalizer;

@ExtendWith(MockitoExtension.class)
class AddressParserTest {

  @Mock
  private SidoNormalizer sidoNormalizer;

  @InjectMocks
  private AddressParser addressParser;

  @Test
  @DisplayName("도 + 시: '전북 익산시 망산길 11-17' → (전라북도, 익산시)")
  void baseCaseSuccess() {
    // given
    when(sidoNormalizer.normalize("전북")).thenReturn("전라북도");
    String fullAddress = "전북 익산시 망산길 11-17";

    // when
    AddressInfo addressInfo = addressParser.parseAddress(fullAddress);

    // then
    assertThat(addressInfo.sido()).isEqualTo("전라북도");
    assertThat(addressInfo.sigungu()).isEqualTo("익산시");
  }

  @Test
  @DisplayName("시 + 구: '서울 강남구 임시길' → (서울특별시, 강남구)")
  void siAndGuCaseSuccess() {
    // given
    when(sidoNormalizer.normalize("서울")).thenReturn("서울특별시");
    String fullAddress = "서울 강남구 임시길";

    // when
    AddressInfo addressInfo = addressParser.parseAddress(fullAddress);

    // then
    assertThat(addressInfo.sido()).isEqualTo("서울특별시");
    assertThat(addressInfo.sigungu()).isEqualTo("강남구");
  }

  @Test
  @DisplayName("도 + 시 + 구: '경기 수원시 영통구 임시길' → (경기도, 수원시 영통구)")
  void doAndsiAndGunCaseSuccess() {
    // given
    when(sidoNormalizer.normalize("경기")).thenReturn("경기도");
    String fullAddress = "경기 수원시 영통구 임시길";

    // when
    AddressInfo addressInfo = addressParser.parseAddress(fullAddress);

    // then
    assertThat(addressInfo.sido()).isEqualTo("경기도");
    assertThat(addressInfo.sigungu()).isEqualTo("수원시 영통구");
  }

  @Test
  @DisplayName("도 + 군: '충북 보은군 임시길' → (충청북도, 보은군)")
  void doAndGunCaseSuccess() {
    // given
    when(sidoNormalizer.normalize("충북")).thenReturn("충청북도");
    String fullAddress = "충북 보은군 임시길";

    // when
    AddressInfo addressInfo = addressParser.parseAddress(fullAddress);

    // then
    assertThat(addressInfo.sido()).isEqualTo("충청북도");
    assertThat(addressInfo.sigungu()).isEqualTo("보은군");
  }

  @Test
  @DisplayName("특별 케이스 세종시: '세종특별자치시 임시읍' → (세종특별자치시, 세종시)")
  void sejongCaseSuccess() {
    // given
    when(sidoNormalizer.normalize("세종특별자치시")).thenReturn("세종특별자치시");
    String fullAddress = "세종특별자치시 임시읍";

    // when
    AddressInfo addressInfo = addressParser.parseAddress(fullAddress);

    // then
    assertThat(addressInfo.sido()).isEqualTo("세종특별자치시");
    assertThat(addressInfo.sigungu()).isEqualTo("세종시");
  }

  @Test
  @DisplayName("양끝 공백 제거 및 분리 테스트")
  void trimsAndSplitsByWhitespace() {
    // given
    when(sidoNormalizer.normalize("전북")).thenReturn("전라북도");
    String fullAddress = "  전북 익산시\t망산길 11-17  ";

    // when
    AddressInfo addressInfo = addressParser.parseAddress(fullAddress);

    // then
    assertThat(addressInfo.sido()).isEqualTo("전라북도");
    assertThat(addressInfo.sigungu()).isEqualTo("익산시");
  }
}
