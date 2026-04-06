package region.jidogam.domain.user.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("levelCalculator 테스트")
@ExtendWith(MockitoExtension.class)
class LevelCalculatorTest {

  @InjectMocks
  private LevelCalculator levelCalculator;

  @Test
  @DisplayName("레벨 1-10 구간: exp 0 -> 레벨 1")
  void calculateLevel_Range1To10() {
    assertThat(levelCalculator.calculateLevel(0L)).isEqualTo(1);
  }

  @Test
  @DisplayName("레벨 11-25 구간: exp 5000 -> 레벨 12")
  void calculateLevel_Range11To25() {
    assertThat(levelCalculator.calculateLevel(5000L)).isEqualTo(12);
  }

  @Test
  @DisplayName("레벨 26-35 구간: exp 30400 -> 레벨 27")
  void calculateLevel_Range26To35() {
    assertThat(levelCalculator.calculateLevel(30400L)).isEqualTo(27);
  }

  @Test
  @DisplayName("레벨 36+ 구간: exp 60000 -> 레벨 37")
  void calculateLevel_Range36Plus() {
    assertThat(levelCalculator.calculateLevel(60000L)).isEqualTo(37);
  }

  @Test
  @DisplayName("경계값: exp 3200 (Lv10) / exp 4000 (Lv11)")
  void calculateLevel_Boundary_Level10And11() {
    assertThat(levelCalculator.calculateLevel(3200L)).isEqualTo(10);
    assertThat(levelCalculator.calculateLevel(4000L)).isEqualTo(11);
  }

  @Test
  @DisplayName("경계값: exp 25700 (Lv25) / exp 28000 (Lv26)")
  void calculateLevel_Boundary_Level25And26() {
    assertThat(levelCalculator.calculateLevel(25700L)).isEqualTo(25);
    assertThat(levelCalculator.calculateLevel(28000L)).isEqualTo(26);
  }

  @Test
  @DisplayName("경계값: exp 53200 (Lv35) / exp 56500 (Lv36)")
  void calculateLevel_Boundary_Level35And36() {
    assertThat(levelCalculator.calculateLevel(53200L)).isEqualTo(35);
    assertThat(levelCalculator.calculateLevel(56500L)).isEqualTo(36);
  }

  @Test
  @DisplayName("경계값 근처: exp 99 (Lv1) / exp 100 (Lv2)")
  void calculateLevel_Boundary_Level1And2() {
    assertThat(levelCalculator.calculateLevel(99L)).isEqualTo(1);
    assertThat(levelCalculator.calculateLevel(100L)).isEqualTo(2);
  }

  @Test
  @DisplayName("예외: 음수 exp -> IllegalArgumentException")
  void calculateLevel_NegativeExp_ThrowsException() {
    assertThatThrownBy(() -> levelCalculator.calculateLevel(-100L))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("EXP는 0 이상이어야 합니다.");
  }
}
