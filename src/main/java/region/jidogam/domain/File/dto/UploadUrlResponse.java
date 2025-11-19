package region.jidogam.domain.File.dto;

import lombok.Builder;

@Builder
public record UploadUrlResponse(
    String preSignedUrl,
    String key
) {

}
