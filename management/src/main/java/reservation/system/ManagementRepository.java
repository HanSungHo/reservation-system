package reservation.system;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="managements", path="managements")
public interface ManagementRepository extends PagingAndSortingRepository<Management, Long>{

    Management findBySeatId(Long SeatId);
}
