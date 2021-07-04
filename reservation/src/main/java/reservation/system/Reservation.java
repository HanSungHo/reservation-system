package reservation.system;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Reservation_table")
public class Reservation {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String name;
    private String reserveDate;
    private String exitDate;
    private String payType;
    private Long seatId;

    @PostPersist
    public void onPostPersist(){
        Reserved reserved = new Reserved();
        BeanUtils.copyProperties(this, reserved);

        reserved.publishAfterCommit();

        //Following code causes dependency to external APIs
        // it is NOT A GOOD PRACTICE. instead, Event-Policy mapping is recommended.

        reservation.system.external.Payment payment = new reservation.system.external.Payment();
        // mappings goes here

        System.out.println("##### id = " + this.getId() +"#####");
        payment.setReserveId(this.getId());
        payment.setSeatId(this.getSeatId());
        payment.setName(this.getName());
        payment.setPayType(this.getPayType());
        payment.setReserveDate(this.getReserveDate());
        payment.setExitDate(this.getExitDate());
        
        ReservationApplication.applicationContext.getBean(reservation.system.external.PaymentService.class)
            .pay(payment);


    }

    @PreRemove
    public void onPreRemove(){
        Exited exited = new Exited();
        BeanUtils.copyProperties(this, exited);
        exited.publishAfterCommit();


    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
    public Long getSeatId() {
        return seatId;
    }

    public void setSeatId(Long seatId) {
        this.seatId = seatId;
    }




}
