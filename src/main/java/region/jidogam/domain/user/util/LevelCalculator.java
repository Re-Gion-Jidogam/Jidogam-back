package region.jidogam.domain.user.util;

import org.springframework.stereotype.Component;

/**
 * 사용자 레벨 계산 유틸리티 클래스
 *
 * 경험치(EXP)를 기반으로 사용자의 레벨을 계산
 * 등차수열 공식을 사용하여 O(1) 시간복잡도로 최적화
 */
@Component
public class LevelCalculator {

  // 구간 경계 EXP (다음 레벨 시작 지점)
  private static final long LEVEL_10_END_EXP = 4000L;    // Lv10 끝 (Lv11 시작)
  private static final long LEVEL_25_END_EXP = 28000L;   // Lv25 끝 (Lv26 시작)
  private static final long LEVEL_35_END_EXP = 56500L;   // Lv35 끝 (Lv36 시작)

  // 레벨 1-10 구간 경계값 (각 레벨 시작 지점)
  private static final long[] LEVEL_1_TO_10_THRESHOLDS = {
      0L,     // Lv1
      100L,   // Lv2
      250L,   // Lv3
      450L,   // Lv4
      700L,   // Lv5
      1_000L, // Lv6
      1_400L, // Lv7
      1_900L, // Lv8
      2_500L, // Lv9
      3_200L  // Lv10
  };

  // 등차수열 계산을 위한 기준 EXP (각 레벨 시작 지점)
  private static final long LEVEL_10_BASE_EXP = 3_200L;   // Lv10 시작
  private static final long LEVEL_25_BASE_EXP = 25_700L;  // Lv25 시작
  private static final long LEVEL_35_BASE_EXP = 53_200L;  // Lv35 시작

  // 등차수열 파라미터
  // 레벨 11-25: 초항 800, 공차 100
  private static final int RANGE_11_25_FIRST_TERM = 800;
  private static final int RANGE_11_25_COMMON_DIFF = 100;
  private static final int RANGE_11_25_BASE_LEVEL = 10;
  private static final int RANGE_11_25_MAX_LEVEL = 25;

  // 레벨 26-35: 초항 2300, 공차 100
  private static final int RANGE_26_35_FIRST_TERM = 2_300;
  private static final int RANGE_26_35_COMMON_DIFF = 100;
  private static final int RANGE_26_35_BASE_LEVEL = 25;
  private static final int RANGE_26_35_MAX_LEVEL = 35;

  // 레벨 36+: 초항 3300, 공차 200
  private static final int RANGE_36_PLUS_FIRST_TERM = 3_300;
  private static final int RANGE_36_PLUS_COMMON_DIFF = 200;
  private static final int RANGE_36_PLUS_BASE_LEVEL = 35;

  // 경험치로 레벨 계산
  public int calculateLevel(long exp) {
    if (exp < 0) {
      throw new IllegalArgumentException("EXP는 0 이상이어야 합니다.");
    }

    if (exp < LEVEL_10_END_EXP) {
      return calculateLevelInRange1To10(exp);
    } else if (exp < LEVEL_25_END_EXP) {
      return calculateLevelInRange11To25(exp);
    } else if (exp < LEVEL_35_END_EXP) {
      return calculateLevelInRange26To35(exp);
    } else {
      return calculateLevelInRange36Plus(exp);
    }
  }

  // 레벨 1-10 구간에서는 단순 비교로 레벨 계산
  private static int calculateLevelInRange1To10(long exp) {
    for (int level = LEVEL_1_TO_10_THRESHOLDS.length - 1; level >= 0; level--) {
      if (exp >= LEVEL_1_TO_10_THRESHOLDS[level]) {
        return level + 1;
      }
    }
    return 1;
  }


  // 레벨 11-25 구간
  private static int calculateLevelInRange11To25(long exp) {
    return calculateLevelFromArithmeticSequence(
        exp,
        LEVEL_10_BASE_EXP,
        RANGE_11_25_BASE_LEVEL,
        RANGE_11_25_MAX_LEVEL,
        RANGE_11_25_FIRST_TERM,
        RANGE_11_25_COMMON_DIFF
    );
  }

  // 레벨 26-35 구간
  private static int calculateLevelInRange26To35(long exp) {
    return calculateLevelFromArithmeticSequence(
        exp,
        LEVEL_25_BASE_EXP,
        RANGE_26_35_BASE_LEVEL,
        RANGE_26_35_MAX_LEVEL,
        RANGE_26_35_FIRST_TERM,
        RANGE_26_35_COMMON_DIFF
    );
  }

  // 레벨 36+ 구간
  private static int calculateLevelInRange36Plus(long exp) {
    return calculateLevelFromArithmeticSequence(
        exp,
        LEVEL_35_BASE_EXP,
        RANGE_36_PLUS_BASE_LEVEL,
        Integer.MAX_VALUE, // 최대 레벨 제한 없음
        RANGE_36_PLUS_FIRST_TERM,
        RANGE_36_PLUS_COMMON_DIFF
    );
  }

  /**
   * 등차수열 기반 레벨 계산
   *
   * 등차수열 누적 합 공식: S_n = n*a + d*(0+1+2+...+(n-1)) = n*a + d*n*(n-1)/2
   * 이를 정리하면: (d/2)*n^2 + (a - d/2)*n = expFromBase
   * 이차방정식 근의 공식을 사용하여 n을 구함
   *
   * @param currentExp 현재 경험치
   * @param baseExp    기준 레벨까지의 누적 경험치
   * @param baseLevel  기준 레벨
   * @param maxLevel   최대 레벨
   * @param firstTerm  등차수열 초항 (a)
   * @param commonDiff 등차수열 공차 (d)
   * @return 계산된 레벨
   */
  private static int calculateLevelFromArithmeticSequence(
      long currentExp,
      long baseExp,
      int baseLevel,
      int maxLevel,
      int firstTerm,
      int commonDiff
  ) {
    long expFromBase = currentExp - baseExp;

    // 이차방정식 계수: (d/2)*n^2 + (a - d/2)*n - expFromBase = 0
    double coeffA = commonDiff / 2.0;
    double coeffB = firstTerm - commonDiff / 2.0;

    // 근의 공식: n = (-b + sqrt(b^2 + 4ac)) / 2a
    double discriminant = coeffB * coeffB + 4 * coeffA * expFromBase;
    double n = (-coeffB + Math.sqrt(discriminant)) / (2 * coeffA);

    int levelsAboveBase = (int) Math.floor(n);
    int calculatedLevel = baseLevel + levelsAboveBase;

    return Math.min(calculatedLevel, maxLevel);
  }
}