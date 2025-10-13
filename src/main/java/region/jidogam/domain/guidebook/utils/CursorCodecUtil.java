package region.jidogam.domain.guidebook.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import region.jidogam.common.exception.InvalidCursorException;
import region.jidogam.domain.guidebook.dto.GuidebookCursor;
import region.jidogam.domain.guidebook.dto.GuidebookResponse;
import region.jidogam.domain.guidebook.dto.GuidebookSortBy;

@Slf4j
@Component
@RequiredArgsConstructor
public class CursorCodecUtil {

  public final ObjectMapper objectMapper;

  /**
   * 인코딩된 cursor 값을 디코딩하는 메서드
   *
   * @param encodedCursor 인코딩된 문자열 값
   */
  public GuidebookCursor decodeCursor(String encodedCursor) {
    log.info("decodeCursor - cursor 값 Base64 디코딩 시작");
    if (encodedCursor == null || encodedCursor.isBlank()) {
      log.info("null 입력으로 null를 반환");
      return null;
    }
    try {
      // 1. Base64 문자열 → Byte 변환
      byte[] decodedBytes = Base64.getUrlDecoder().decode(encodedCursor);
      // 2. Byte → JSON 변환
      String decodedJson = new String(decodedBytes, StandardCharsets.UTF_8);
      // 4. JSON → Cursor 객체 변환및 반환
      return objectMapper.readValue(decodedJson, GuidebookCursor.class);
    } catch (Exception e) {
      // 에러 커스텀 - 잘못된 입력
      log.info("Base64 문자열을 디코딩하여 객체로 변환 중 오류 발생", e);
      throw InvalidCursorException.withMessage("잘못된 커서 값 입니다.");
    }
  }

  /**
   * 가이드북 커서 페이지네이션의 마지막 데이터를 인코딩하여 반환하는 메서드
   *
   * @param lastItem GuidebookResponse 타입의 아이템
   * @param sortBy   정렬 기준
   */
  public String encodeNextCursor(GuidebookResponse lastItem, GuidebookSortBy sortBy) {
    UUID lastId = lastItem.gid();
    GuidebookCursor cursor;
    log.info("PARTICIPANT_COUNT: {}, lastId: {}", lastItem.participantCount(), lastId);
    switch (sortBy) {
      case CREATED_AT -> cursor = new GuidebookCursor(null, lastItem.createdAt(), lastId);
      case PARTICIPANT_COUNT ->
        cursor = new GuidebookCursor(lastItem.participantCount(), null, lastId);
      default -> throw new IllegalArgumentException("지원하지 않는 정렬:" + sortBy);
    }
    return encodeNextCursor(cursor);
  }

  /**
   * 내부에서 인코딩 로직을 담당하는 메서드
   *
   * @param cursor dto의 id와 value로 이루어진 커서 객체
   */
  private String encodeNextCursor(GuidebookCursor cursor) {
    try {
      // 1. 객체 → JSON 변환
      String cursorToJson = objectMapper.writeValueAsString(cursor);
      // 2. JSON → Base64 문자열 변환 및 반환
      return Base64.getUrlEncoder().encodeToString(cursorToJson.getBytes(StandardCharsets.UTF_8));
    } catch (Exception e) {
      log.warn("객체를 Base64로 인코딩 중 오류 발생", e);
      throw new RuntimeException(e);
    }
  }
}
