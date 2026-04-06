package region.jidogam.domain.area.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record AreaWeightUpdateRequest(

    boolean useDefaultWeights,
    List<RegionPopulation> regions
) {

  public record RegionPopulation(

      @JsonProperty("SIDO_NM")
      String sidoName,

      @JsonProperty("SGG_NM")
      String sggName,

      @JsonProperty("POPL_DCRS_REGN_YN")
      Flag isPopulationDecreaseRegion, // 인구감소지역 여부

      @JsonProperty("POPL_DCRS_ITS_REGN_YN")
      Flag isPopulationDecreaseInterestRegion, // 인구감소지역 관심지역 여부

      @JsonProperty("WEIGHT")
      Double weight // useDefaultWeights가 false인 경우, 이 값으로 세팅
  ) {

    public String getFullName() {
      return this.sidoName + " " + this.sggName;
    }

    public enum Flag {
      Y(true),
      N(false);

      private final boolean value;

      Flag(boolean value) {
        this.value = value;
      }

      public boolean getValue() {
        return value;
      }
    }
  }
}
