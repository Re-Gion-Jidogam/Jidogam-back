package region.jidogam.domain.place.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import region.jidogam.domain.area.entity.AdministrativeLevel;
import region.jidogam.domain.area.entity.Area;
import region.jidogam.domain.area.entity.Area.PopulationDeclineCategory;
import region.jidogam.domain.area.service.AreaService;
import region.jidogam.domain.exp.service.ExpService;
import region.jidogam.domain.place.dto.ExternalPlaceData;
import region.jidogam.domain.place.entity.Place;
import region.jidogam.domain.place.entity.PlaceChangeHistory.ChangeSource;
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
        .code("1234")
        .name("익산시")
        .administrativeLevel(AdministrativeLevel.SIGUNGU)
        .weight(1.0)
        .weightUpdatedAt(LocalDateTime.now())
        .populationDeclineCategory(PopulationDeclineCategory.NORMAL)
        .build();

    ExternalPlaceData data = ExternalPlaceData.builder()
        .externalId("1234")
        .placeName("임시마트")
        .jibunAddress("전북 익산시 망산길 11-17")
        .roadAddress("전북 익산시 망산길 11-17")
        .sigunguCode("52140")
        .sigunguName("익산시")
        .y(BigDecimal.valueOf(35.976749396987046))
        .x(BigDecimal.valueOf(126.99599512792346))
        .fetchedAt(LocalDateTime.now())
        .build();

    Place place = Place.builder()
        .externalId("1234")
        .source(Place.Source.SEMAS)
        .name(data.placeName())
        .jibunAddress(data.jibunAddress())
        .roadAddress(data.roadAddress())
        .fetchedAt(data.fetchedAt())
        .x(data.x())
        .y(data.y())
        .categoryCode(data.categoryCode())
        .categoryName(data.categoryName())
        .area(area)
        .exp(10)
        .build();

    when(expService.calculatePlaceExp(1.0)).thenReturn(10);
    when(areaService.getByCode("52140", "익산시")).thenReturn(area);
    when(placeRepository.save(any(Place.class))).thenReturn(place);

    // when
    Place newPlace = placeService.createPlace(data, Place.Source.SEMAS);

    // then
    assertThat(newPlace.getExternalId()).isEqualTo("1234");
    assertThat(newPlace.getName()).isEqualTo("임시마트");
    assertThat(newPlace.getJibunAddress()).isEqualTo(data.jibunAddress());

  }

  @Test
  @DisplayName("upsertPlace: 이미 존재하는 장소면 변경 이력만 기록하고 새로 생성하지 않는다")
  void upsertPlaceUpdatesExistingPlace() {
    // given
    Place existingPlace = Place.builder()
        .externalId("1234")
        .source(Place.Source.SEMAS)
        .name("기존이름")
        .jibunAddress("전북 익산시 망산길 11-17")
        .fetchedAt(LocalDateTime.now().minusDays(1))
        .x(BigDecimal.valueOf(126.99599512792346))
        .y(BigDecimal.valueOf(35.976749396987046))
        .exp(10)
        .build();

    ExternalPlaceData data = ExternalPlaceData.builder()
        .externalId("1234")
        .placeName("변경된이름")
        .jibunAddress("전북 익산시 망산길 11-17")
        .y(BigDecimal.valueOf(35.976749396987046))
        .x(BigDecimal.valueOf(126.99599512792346))
        .fetchedAt(LocalDateTime.now())
        .build();

    when(placeRepository.findByExternalIdAndSource("1234", Place.Source.SEMAS))
        .thenReturn(java.util.Optional.of(existingPlace));

    // when
    Place result = placeService.upsertPlace(data, Place.Source.SEMAS);

    // then
    assertThat(result).isSameAs(existingPlace);
    verify(placeUpdateService).detectUpdateAndRecord(existingPlace, data, ChangeSource.SEMAS);
    verify(placeRepository, never()).save(any(Place.class));
  }

  @Test
  @DisplayName("upsertPlace: 존재하지 않는 장소면 새로 생성한다")
  void upsertPlaceCreatesNewPlace() {
    // given
    Area area = Area.builder()
        .code("1234")
        .name("익산시")
        .administrativeLevel(AdministrativeLevel.SIGUNGU)
        .weight(1.0)
        .weightUpdatedAt(LocalDateTime.now())
        .populationDeclineCategory(PopulationDeclineCategory.NORMAL)
        .build();

    ExternalPlaceData data = ExternalPlaceData.builder()
        .externalId("5678")
        .placeName("신규장소")
        .jibunAddress("전북 익산시 망산길 11-17")
        .sigunguCode("52140")
        .sigunguName("익산시")
        .y(BigDecimal.valueOf(35.976749396987046))
        .x(BigDecimal.valueOf(126.99599512792346))
        .fetchedAt(LocalDateTime.now())
        .build();

    Place savedPlace = Place.builder()
        .externalId("5678")
        .source(Place.Source.SEMAS)
        .name(data.placeName())
        .jibunAddress(data.jibunAddress())
        .fetchedAt(data.fetchedAt())
        .x(data.x())
        .y(data.y())
        .area(area)
        .exp(10)
        .build();

    when(placeRepository.findByExternalIdAndSource("5678", Place.Source.SEMAS))
        .thenReturn(java.util.Optional.empty());
    when(expService.calculatePlaceExp(1.0)).thenReturn(10);
    when(areaService.getByCode("52140", "익산시")).thenReturn(area);
    when(placeRepository.save(any(Place.class))).thenReturn(savedPlace);

    // when
    Place result = placeService.upsertPlace(data, Place.Source.SEMAS);

    // then
    assertThat(result.getExternalId()).isEqualTo("5678");
    verify(placeUpdateService, never()).detectUpdateAndRecord(any(), any(), any());
  }
}
