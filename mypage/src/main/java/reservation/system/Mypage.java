package reservation.system;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="Mypage_table")
public class Mypage {

        @Id
        @GeneratedValue(strategy=GenerationType.AUTO)
        private Long id;
        private Long reserveId;
        private Long seatId;
        private String name;
        private String reserveDate;
        private String exitDate;
        private String payType;


        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
        public Long getReserveId() {
            return reserveId;
        }

        public void setReserveId(Long reserveId) {
            this.reserveId = reserveId;
        }
        public Long getSeatId() {
            return seatId;
        }

        public void setSeatId(Long seatId) {
            this.seatId = seatId;
        }
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
        public String getReserveDate() {
            return reserveDate;
        }

        public void setReserveDate(String reserveDate) {
            this.reserveDate = reserveDate;
        }
        public String getExitDate() {
            return exitDate;
        }

        public void setExitDate(String exitDate) {
            this.exitDate = exitDate;
        }
        public String getPayType() {
            return payType;
        }

        public void setPayType(String payType) {
            this.payType = payType;
        }

}
