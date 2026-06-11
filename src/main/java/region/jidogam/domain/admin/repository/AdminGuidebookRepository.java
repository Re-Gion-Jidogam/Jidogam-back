package region.jidogam.domain.admin.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import region.jidogam.domain.guidebook.entity.Guidebook;

public interface AdminGuidebookRepository {

  Page<Guidebook> searchGuidebooks(String keyword, Boolean isPublished, Pageable pageable);
}
