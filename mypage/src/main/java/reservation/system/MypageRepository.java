package reservation.system;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MypageRepository extends CrudRepository<Mypage, Long> {

    List<Mypage> findBySeatId(Long seatId);

        void deleteBySeatId(Long seatId);
}