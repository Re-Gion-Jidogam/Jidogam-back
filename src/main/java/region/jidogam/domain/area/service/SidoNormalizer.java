package region.jidogam.domain.area.service;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import region.jidogam.domain.area.exception.UnknownSidoException;

@Slf4j
@Component
public class SidoNormalizer {

  private final Map<String, String> sidoAlias;

  public SidoNormalizer() {
    this.sidoAlias = createSidoAliasMap();
  }

  private static Map<String, String> createSidoAliasMap() {
    Map<String, String> addressMap = new HashMap<>();
    addAlias(addressMap, "서울특별시", "서울", "서울시", "서울특별시");
    addAlias(addressMap, "부산광역시", "부산", "부산시", "부산광역시");
    addAlias(addressMap, "대구광역시", "대구", "대구시", "대구광역시");
    addAlias(addressMap, "인천광역시", "인천", "인천시", "인천광역시");
    addAlias(addressMap, "광주광역시", "광주", "광주시", "광주광역시");
    addAlias(addressMap, "대전광역시", "대전", "대전시", "대전광역시");
    addAlias(addressMap, "울산광역시", "울산", "울산시", "울산광역시");
    addAlias(addressMap, "세종특별자치시", "세종", "세종시", "세종특별자치시");
    addAlias(addressMap, "경기도", "경기", "경기도");
    addAlias(addressMap, "강원특별자치도", "강원", "강원도", "강원특별자치도");
    addAlias(addressMap, "충청북도", "충북", "충청북도");
    addAlias(addressMap, "충청남도", "충남", "충청남도");
    addAlias(addressMap, "전라북도", "전북", "전라북도");
    addAlias(addressMap, "전라남도", "전남", "전라남도");
    addAlias(addressMap, "경상북도", "경북", "경상북도");
    addAlias(addressMap, "경상남도", "경남", "경상남도");
    addAlias(addressMap, "제주특별자치도", "제주", "제주도", "제주특별자치도");
    return Map.copyOf(addressMap);
  }

  private static void addAlias(Map<String, String> map, String standard, String... aliases) {
    for (String alias : aliases) {
      map.put(alias, standard);
    }
  }

  public String normalize(String sido) {
    if (sido == null || sido.isBlank()) {
      throw new UnknownSidoException(sido);
    }

    String key = sido.strip();
    String normalized = sidoAlias.get(key);

    if (normalized == null) {
      throw UnknownSidoException.withSido(sido);
    }

    return normalized;
  }

}
