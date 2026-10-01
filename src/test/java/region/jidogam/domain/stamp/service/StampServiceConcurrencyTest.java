package region.jidogam.domain.stamp.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import region.jidogam.domain.area.entity.AdministrativeLevel;
import region.jidogam.domain.area.entity.Area;
import region.jidogam.domain.area.repository.AreaRepository;
import region.jidogam.domain.place.entity.Place;
import region.jidogam.domain.place.entity.Place.Source;
import region.jidogam.domain.place.repository.PlaceRepository;
import region.jidogam.domain.stamp.dto.PlaceStampRequest;
import region.jidogam.domain.stamp.exception.StampCooldownException;
import region.jidogam.domain.stamp.exception.StampDuplicateException;
import region.jidogam.domain.stamp.repository.StampRepository;
import region.jidogam.domain.user.entity.User;
import region.jidogam.domain.user.repository.UserRepository;

/**
 * 방문 체크(도장) 동시성 테스트
 *
 * - 클래스에 @Transactional을 붙이지 않는다.
 * 작업 스레드는 각자 새 트랜잭션으로 실행되므로, 테스트 데이터가 커밋되어 있어야 보인다.
 * - 자동 롤백이 없으므로 @AfterEach에서 테스트가 만든 데이터만 직접 정리한다.
 * - H2 대신 Testcontainers PostgreSQL을 사용해 실제 락·제약 동작과 맞춘다.
 * - 수정 전/후 비교를 위해 검증 전에 실행 결과 리포트를 로그로 남긴다. (로그 prefix: [CONCURRENCY])
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
public class StampServiceConcurrencyTest {

  private static final Logger log = LoggerFactory.getLogger(StampServiceConcurrencyTest.class);

  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");
  // TODO: 운영 DB와 같은 PostgreSQL 버전으로 맞추기

  @DynamicPropertySource
  static void configure(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);

    // test 프로필의 H2 설정 덮어쓰기
    registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
    registry.add("spring.jpa.properties.hibernate.dialect",
        () -> "org.hibernate.dialect.PostgreSQLDialect");

    // 운영과 같은 스키마로: Flyway 마이그레이션 적용, Hibernate 자동 생성은 끔
    registry.add("spring.flyway.enabled", () -> "true");
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    // test 프로필의 defer-datasource-initialization=true는 Flyway와 순환 의존을 만들므로 끔
    registry.add("spring.jpa.defer-datasource-initialization", () -> "false");

    // 로컬 .env 값에 따라 결과가 달라지지 않도록 쿨타임 고정
    registry.add("jidogam.stamp.cooldown.time", () -> "5m");
  }

  @Autowired
  private StampService stampService;
  @Autowired
  private StampRepository stampRepository;
  @Autowired
  private PlaceRepository placeRepository;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private AreaRepository areaRepository;
  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  // 테스트가 만든 데이터만 정리하기 위해 ID를 모아둔다 (관리자 계정 등 기존 데이터 보존)
  private final List<UUID> createdUserIds = new ArrayList<>();
  private final List<UUID> createdPlaceIds = new ArrayList<>();
  private final List<UUID> createdAreaIds = new ArrayList<>();

  @AfterEach
  void tearDown() {
    // 외래 키 때문에 참조하는 쪽부터 삭제
    if (!createdUserIds.isEmpty()) {
      jdbcTemplate.update("DELETE FROM stamps WHERE user_id IN (:ids)",
          Map.of("ids", createdUserIds));
    }
    placeRepository.deleteAllByIdInBatch(createdPlaceIds);
    areaRepository.deleteAllByIdInBatch(createdAreaIds);
    userRepository.deleteAllByIdInBatch(createdUserIds);
  }

  @Test
  @DisplayName("같은 사용자가 같은 장소에 동시에 100번 요청하면 도장은 1건만 생성되고, 나머지는 도메인 예외로 실패한다")
  void sameUserSamePlace_concurrentRequests_onlyOneStamp() throws InterruptedException {
    // given
    User user = saveUser(0);
    Place place = savePlace(saveArea(), 0, 100);
    PlaceStampRequest request = new PlaceStampRequest(place.getId());

    // when
    int threads = 100;
    ConcurrencyResult result = runConcurrently(threads,
        i -> stampService.stampPlace(request, user.getId()));
    report("같은 사용자 · 같은 장소", result, List.of(user.getId()), List.of(place.getId()));

    // then 1. 성공은 한 번
    assertThat(result.success()).isEqualTo(1);

    // then 2. 실패는 모두 도메인 예외 (DB 예외가 그대로 새어 나가지 않음)
    assertThat(result.errors()).hasSize(threads - 1);
    assertThat(result.errors()).noneMatch(e -> e instanceof DataIntegrityViolationException);
    assertThat(result.errors()).allMatch(e ->
        e instanceof StampDuplicateException || e instanceof StampCooldownException);

    // then 3. DB 최종 상태: 한 번만 반영
    assertThat(stampRepository.count()).isEqualTo(1);
    assertThat(findUserExp(user.getId())).isEqualTo(100);
    assertThat(findPlaceStampCount(place.getId())).isEqualTo(1);
  }

  @Test
  @DisplayName("서로 다른 사용자 100명이 같은 장소에 동시에 방문 체크하면 방문 수가 정확히 100이 된다")
  void differentUsersSamePlace_concurrentRequests_stampCountIsExact() throws InterruptedException {
    // given
    int threads = 100;
    Place place = savePlace(saveArea(), 0, 100);
    List<UUID> userIds = new ArrayList<>();
    for (int i = 0; i < threads; i++) {
      userIds.add(saveUser(i).getId());
    }
    PlaceStampRequest request = new PlaceStampRequest(place.getId());

    // when
    ConcurrencyResult result = runConcurrently(threads,
        i -> stampService.stampPlace(request, userIds.get(i)));
    report("다른 사용자 · 같은 장소", result, userIds, List.of(place.getId()));

    // then: 모두 성공하고, 방문 수 갱신이 유실되지 않음
    assertThat(result.errors()).isEmpty();
    assertThat(result.success()).isEqualTo(threads);
    assertThat(stampRepository.count()).isEqualTo(threads);
    assertThat(findPlaceStampCount(place.getId())).isEqualTo(threads);
    assertThat(userIds).allSatisfy(id -> assertThat(findUserExp(id)).isEqualTo(100));
  }

  @Test
  @DisplayName("같은 사용자가 서로 다른 장소 100곳에 동시에 요청하면 쿨타임으로 1건만 성공하고, 경험치도 1건만 반영된다")
  void sameUserDifferentPlaces_concurrentRequests_cooldownAndExpConsistent()
      throws InterruptedException {
    // given
    int threads = 100;
    User user = saveUser(0);
    Area area = saveArea();
    List<UUID> placeIds = new ArrayList<>();
    for (int i = 0; i < threads; i++) {
      placeIds.add(savePlace(area, i, 100).getId());
    }

    // when
    ConcurrencyResult result = runConcurrently(threads,
        i -> stampService.stampPlace(new PlaceStampRequest(placeIds.get(i)), user.getId()));
    report("같은 사용자 · 다른 장소", result, List.of(user.getId()), placeIds);

    // then 1. 쿨타임 때문에 성공은 한 번, 나머지는 쿨타임 예외
    assertThat(result.success()).isEqualTo(1);
    assertThat(result.errors()).hasSize(threads - 1);
    assertThat(result.errors()).allMatch(e -> e instanceof StampCooldownException);

    // then 2. 도장 1건, 경험치는 실제로 생성된 도장의 획득 경험치 합과 일치 (lost update 없음)
    assertThat(stampRepository.count()).isEqualTo(1);
    assertThat(findUserExp(user.getId())).isEqualTo(sumEarnedExp(user.getId()));
  }

  /**
   * 모든 스레드를 준비시킨 뒤 동시에 출발시키고, 성공 수와 예외를 모은다.
   * - ready: 모든 스레드가 대기 상태가 될 때까지 기다림
   * - start: 한 번에 풀어 동시에 실행
   * - done : 모두 끝날 때까지 기다림
   * 작업에는 스레드 번호(0 ~ threads-1)를 넘긴다.
   */
  private ConcurrencyResult runConcurrently(int threads, IntConsumer task)
      throws InterruptedException {

    ExecutorService executor = Executors.newFixedThreadPool(threads); // 풀 크기 = 요청 수
    CountDownLatch ready = new CountDownLatch(threads);
    CountDownLatch start = new CountDownLatch(1);
    CountDownLatch done = new CountDownLatch(threads);

    AtomicInteger success = new AtomicInteger();
    List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());

    try {
      for (int i = 0; i < threads; i++) {
        int index = i;
        executor.submit(() -> {
          ready.countDown();
          try {
            start.await();
            task.accept(index);
            success.incrementAndGet();
          } catch (Throwable e) {
            errors.add(e);
          } finally {
            done.countDown();
          }
        });
      }

      assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
      long startedAt = System.nanoTime();
      start.countDown();
      assertThat(done.await(60, TimeUnit.SECONDS)).isTrue(); // 무한 대기 방지
      long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);

      return new ConcurrencyResult(success.get(), List.copyOf(errors), elapsedMillis);
    } finally {
      executor.shutdownNow(); // 검증 실패 시에도 스레드가 남지 않도록
    }
  }

  private record ConcurrencyResult(int success, List<Throwable> errors, long elapsedMillis) {

  }

  /**
   * 수정 전/후 비교용 실행 결과 리포트. 검증보다 먼저 호출해 실패해도 수치가 남게 한다.
   */
  private void report(String scenario, ConcurrencyResult result, List<UUID> userIds,
      List<UUID> placeIds) {
    Map<String, Long> errorTypes = result.errors().stream()
        .collect(Collectors.groupingBy(e -> e.getClass().getSimpleName(), TreeMap::new,
            Collectors.counting()));
    Map<String, Object> ids = Map.of("userIds", userIds, "placeIds", placeIds);

    Long stampCount = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM stamps WHERE user_id IN (:userIds)", ids, Long.class);
    Long totalUserExp = jdbcTemplate.queryForObject(
        "SELECT COALESCE(SUM(exp), 0) FROM users WHERE id IN (:userIds)", ids, Long.class);
    Long totalEarnedExp = jdbcTemplate.queryForObject(
        "SELECT COALESCE(SUM(earned_exp), 0) FROM stamps WHERE user_id IN (:userIds)", ids,
        Long.class);
    Long totalPlaceStampCount = jdbcTemplate.queryForObject(
        "SELECT COALESCE(SUM(stamp_count), 0) FROM places WHERE id IN (:placeIds)", ids,
        Long.class);

    log.info("""

            [CONCURRENCY] ===== {} =====
            [CONCURRENCY] 요청 수            : {}
            [CONCURRENCY] 소요 시간(ms)       : {}
            [CONCURRENCY] 성공 수            : {}
            [CONCURRENCY] 실패 유형별 수      : {}
            [CONCURRENCY] 생성된 도장 수      : {}
            [CONCURRENCY] 장소 방문 수 합     : {}
            [CONCURRENCY] 사용자 경험치 합    : {}
            [CONCURRENCY] 도장 획득 경험치 합 : {} (경험치 합과 다르면 lost update)
            """,
        scenario, result.success() + result.errors().size(), result.elapsedMillis(),
        result.success(), errorTypes, stampCount, totalPlaceStampCount, totalUserExp,
        totalEarnedExp);
  }

  private long findUserExp(UUID userId) {
    return userRepository.findById(userId).orElseThrow().getExp();
  }

  private int findPlaceStampCount(UUID placeId) {
    return placeRepository.findById(placeId).orElseThrow().getStampCount();
  }

  private long sumEarnedExp(UUID userId) {
    return jdbcTemplate.queryForObject(
        "SELECT COALESCE(SUM(earned_exp), 0) FROM stamps WHERE user_id = :userId",
        Map.of("userId", userId), Long.class);
  }

  // ---- 테스트 데이터 생성 ----

  private User saveUser(int seq) {
    User user = userRepository.save(User.builder()
        .email("tester" + seq + "@jidogam.com")
        .nickname("tester" + seq)
        .password("password")
        .build());
    createdUserIds.add(user.getId());
    return user;
  }

  private Area saveArea() {
    Area area = areaRepository.save(Area.builder()
        .name("테스트시")
        .code("T" + UUID.randomUUID().toString().substring(0, 8)) // 시도 code 유니크
        .administrativeLevel(AdministrativeLevel.SIDO)
        .build());
    createdAreaIds.add(area.getId());
    return area;
  }

  private Place savePlace(Area area, int seq, int exp) {
    Place place = placeRepository.save(Place.builder()
        .area(area)
        .externalId("test-place-" + seq)
        .source(Source.SEMAS)
        .name("테스트 장소 " + seq)
        .x(new BigDecimal("127.0"))
        .y(new BigDecimal("37.5"))
        .jibunAddress("테스트 주소 " + seq)
        .fetchedAt(LocalDateTime.now())
        .exp(exp)
        .build());
    createdPlaceIds.add(place.getId());
    return place;
  }
}
