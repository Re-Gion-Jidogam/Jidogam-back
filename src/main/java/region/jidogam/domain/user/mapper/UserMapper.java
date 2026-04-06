package region.jidogam.domain.user.mapper;

import org.springframework.stereotype.Component;
import region.jidogam.domain.stamp.entity.Stamp;
import region.jidogam.domain.user.dto.UserDto;
import region.jidogam.domain.user.entity.User;

@Component
public class UserMapper {

  public UserDto toResponse(User user, Integer level, Stamp lastStamp){
    return UserDto.builder()
        .nickname(user.getNickname())
        .profileUrl(user.getProfileImageUrl())
        .level(level)
        .lastStampedDate(lastStamp == null? null : lastStamp.getCreatedAt())
        .build();
  }
}
