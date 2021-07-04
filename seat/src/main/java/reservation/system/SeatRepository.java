package reservation.system;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="seats", path="seats")
public interface SeatRepository extends PagingAndSortingRepository<Seat, Long>{

    Seat findBySeatId(Long seatId);
}
