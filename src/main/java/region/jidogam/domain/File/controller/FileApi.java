package region.jidogam.domain.File.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import region.jidogam.domain.File.dto.UploadUrlResponse;

@Tag(name = "File", description = "파일 관련 API")
public interface FileApi {

  @Operation(summary = "이미지 업로드 URL 생성", description = "S3 Presigned URL을 생성하여 이미지 업로드를 위한 URL을 반환합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "업로드 URL 생성 성공",
          content = @Content(schema = @Schema(implementation = UploadUrlResponse.class))),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
  })
  ResponseEntity<UploadUrlResponse> generateUploadUrl(
      @Parameter(description = "업로드할 파일의 키(경로)", required = true)
      @RequestParam String key
  );
}
