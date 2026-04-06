package region.jidogam.domain.place.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import region.jidogam.domain.area.entity.Area;
import region.jidogam.domain.area.entity.Area.AreaType;
import region.jidogam.domain.area.service.AreaService;
import region.jidogam.domain.exp.service.ExpService;
import region.jidogam.domain.place.dto.PlaceCreateRequest;
import region.jidogam.domain.place.dto.PlaceGuidebookCountRequest;
import region.jidogam.domain.place.dto.PlaceGuidebookCountResponse;
import region.jidogam.domain.place.entity.Place;
import region.jidogam.domain.place.repository.PlaceRepository;

@ExtendWith(MockitoExtension.class)
class PlaceServiceTest {

  @Mock
  private PlaceRepository placeRepository;

  @Mock
  private AreaService areaService;

  @Mock
  private PlaceUpdateService placeUpdateService;

  @Mock
  private ExpService expService;

  @InjectMocks
  private PlaceService placeService;

  @Test
  @DisplayName("장소 생성 성공")
  void createPlaceSuccess() {
    // given
    Area area = Area.builder()
        .sigunguCode("1234")
        .sido("전라북도특별자치도")
        .sigungu("익산시")
        .weight(1.0)
        .weightUpdatedAt(LocalDateTime.now())
        .type(AreaType.NORMAL)
        .build();

    PlaceCreateRequest request = new PlaceCreateRequest(
        "1234",
        "임시마트",
        "전북 익산시 망산길 11-17",
        null,
        BigDecimal.valueOf(35.976749396987046),
        BigDecimal.valueOf(126.99599512792346)
    );

    Place place = Place.builder()
        .kakaoId("1234")
        .name(request.placeName())
        .address(request.addressName())
        .x(request.x())
        .y(request.y())
        .category(request.category())
        .area(area)
        .exp(10)
        .build();

    when(expService.calculatePlaceExp(1.0)).thenReturn(10);
    when(areaService.getAreaByAddress(any(String.class))).thenReturn(area);
    when(placeRepository.save(any(Place.class))).thenReturn(place);

    // when
    Place newPlace = placeService.createPlace(request);

    // then
    assertThat(newPlace.getKakaoId()).isEqualTo("1234");
    assertThat(newPlace.getName()).isEqualTo("임시마트");
    assertThat(newPlace.getAddress()).isEqualTo(request.addressName());

  }

  @Nested
  @DisplayName("장소별 가이드북 수 조회")
  class GetGuidebookCounts {

    @Test
    @DisplayName("여러 카카오 장소 ID로 가이드북 수 조회 성공")
    void success() {
      // given
      Area area = Area.builder()
          .sigunguCode("11680")
          .sido("서울특별시")
          .sigungu("강남구")
          .weight(1.0)
          .weightUpdatedAt(LocalDateTime.now())
          .type(AreaType.NORMAL)
          .build();

      Place place1 = Place.builder()
          .kakaoId("111")
          .name("장소1")
          .address("서울 강남구")
          .x(BigDecimal.valueOf(127.0))
          .y(BigDecimal.valueOf(37.5))
          .area(area)
          .guidebookCount(5)
          .build();
      ReflectionTestUtils.setField(place1, "id", UUID.randomUUID());

      Place place2 = Place.builder()
          .kakaoId("222")
          .name("장소2")
          .address("서울 강남구")
          .x(BigDecimal.valueOf(127.1))
          .y(BigDecimal.valueOf(37.6))
          .area(area)
          .guidebookCount(3)
          .build();
      ReflectionTestUtils.setField(place2, "id", UUID.randomUUID());

      when(placeRepository.findAllByKakaoIdIn(List.of("111", "222")))
          .thenReturn(List.of(place1, place2));

      PlaceGuidebookCountRequest request = new PlaceGuidebookCountRequest(
          List.of("111", "222"));

      // when
      List<PlaceGuidebookCountResponse> responses = placeService.getGuidebookCounts(request);

      // then
      assertThat(responses).hasSize(2);
      assertThat(responses.get(0).kakaoPid()).isEqualTo("111");
      assertThat(responses.get(0).guidebookCount()).isEqualTo(5);
      assertThat(responses.get(1).kakaoPid()).isEqualTo("222");
      assertThat(responses.get(1).guidebookCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("DB에 없는 카카오 ID가 포함된 경우 존재하는 장소만 반환")
    void returnsOnlyExistingPlaces() {
      // given
      Area area = Area.builder()
          .sigunguCode("11680")
          .sido("서울특별시")
          .sigungu("강남구")
          .weight(1.0)
          .weightUpdatedAt(LocalDateTime.now())
          .type(AreaType.NORMAL)
          .build();

      Place place1 = Place.builder()
          .kakaoId("111")
          .name("장소1")
          .address("서울 강남구")
          .x(BigDecimal.valueOf(127.0))
          .y(BigDecimal.valueOf(37.5))
          .area(area)
          .guidebookCount(2)
          .build();
      ReflectionTestUtils.setField(place1, "id", UUID.randomUUID());

      when(placeRepository.findAllByKakaoIdIn(List.of("111", "999")))
          .thenReturn(List.of(place1));

      PlaceGuidebookCountRequest request = new PlaceGuidebookCountRequest(
          List.of("111", "999"));

      // when
      List<PlaceGuidebookCountResponse> responses = placeService.getGuidebookCounts(request);

      // then
      assertThat(responses).hasSize(1);
      assertThat(responses.get(0).kakaoPid()).isEqualTo("111");
      assertThat(responses.get(0).guidebookCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("모든 카카오 ID가 DB에 없으면 빈 리스트 반환")
    void returnsEmptyWhenNoPlacesFound() {
      // given
      when(placeRepository.findAllByKakaoIdIn(List.of("999")))
          .thenReturn(List.of());

      PlaceGuidebookCountRequest request = new PlaceGuidebookCountRequest(
          List.of("999"));

      // when
      List<PlaceGuidebookCountResponse> responses = placeService.getGuidebookCounts(request);

      // then
      assertThat(responses).isEmpty();
    }
  }
}