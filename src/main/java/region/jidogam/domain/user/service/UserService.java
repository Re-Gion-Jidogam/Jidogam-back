package region.jidogam.domain.user.service;

import region.jidogam.domain.user.dto.UserCreateRequest;

public interface UserService {

  void create(UserCreateRequest request);

}
