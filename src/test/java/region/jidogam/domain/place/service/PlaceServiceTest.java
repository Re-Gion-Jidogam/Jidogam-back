package region.jidogam.domain.place.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import region.jidogam.domain.area.entity.Area;
import region.jidogam.domain.area.entity.Area.AreaType;
import region.jidogam.domain.area.service.AreaService;
import region.jidogam.domain.place.dto.PlaceCreateRequest;
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
  private PointService pointService;

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
        .points(10)
        .build();

    when(pointService.calculatePlacePoint(1.0)).thenReturn(10);
    when(areaService.getAreaByAddress(any(String.class))).thenReturn(area);
    when(placeRepository.save(any(Place.class))).thenReturn(place);

    // when
    Place newPlace = placeService.createPlace(request);

    // then
    assertThat(newPlace.getKakaoId()).isEqualTo("1234");
    assertThat(newPlace.getName()).isEqualTo("임시마트");
    assertThat(newPlace.getAddress()).isEqualTo(request.addressName());

  }
}