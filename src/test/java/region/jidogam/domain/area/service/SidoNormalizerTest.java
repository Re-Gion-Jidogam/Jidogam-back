package region.jidogam.domain.area.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import region.jidogam.domain.area.exception.UnknownSidoException;

class SidoNormalizerTest {

  private final SidoNormalizer normalizer = new SidoNormalizer();

  @DisplayName("시도 정상 매핑")
  @ParameterizedTest(name = "[{index}] \"{0}\" → \"{1}\"")
  @CsvSource({
    // 시
    "서울, 서울특별시",
    "부산, 부산광역시",
    "대구, 대구광역시",
    "인천, 인천광역시",
    "세종, 세종특별자치시",
    // 도
    "경기, 경기도",
    "강원도, 강원특별자치도",
    "충북, 충청북도",
    "전남, 전라남도",
    "경남, 경상남도",
    "제주도, 제주특별자치도"
  })
  void success(String input, String expected) {
    assertThat(normalizer.normalize(input)).isEqualTo(expected);
  }

  @Test
  @DisplayName("null 입력은 예외 발생")
  void failsWhenInputIsNull() {
    assertThrows(UnknownSidoException.class, () -> normalizer.normalize(null));
  }

  @Test
  @DisplayName("빈 문자열 입력은 예외 발생")
  void failsWhenInputIsEmpty() {
    assertThrows(UnknownSidoException.class, () -> normalizer.normalize(""));
  }

  @Test
  @DisplayName("정의되지 않은 시도명은 예외 발생")
  void failsWhenInputIsUndefinedSido() {
    assertThrows(UnknownSidoException.class, () -> normalizer.normalize("서울도"));
  }
}