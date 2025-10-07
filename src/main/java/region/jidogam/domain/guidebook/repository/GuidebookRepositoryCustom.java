package region.jidogam.domain.guidebook.repository;

import java.util.List;
import region.jidogam.common.dto.GuidebookSearchCondition;
import region.jidogam.domain.guidebook.entity.Guidebook;
import region.jidogam.domain.user.entity.User;

public interface GuidebookRepositoryCustom {

  List<Guidebook> findPublicGuidebooksByAuthor(User author, GuidebookSearchCondition condition);

}
