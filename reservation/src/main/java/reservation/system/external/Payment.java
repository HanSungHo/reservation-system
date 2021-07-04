package reservation.system.external;

public class Payment {

    private Long id;
    private String payType;
    private Long reserveId;
    private String reserveDate;
    private String exitDate;
    private String name;
    private Long seatId;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getPayType() {
        return payType;
    }
    public void setPayType(String payType) {
        this.payType = payType;
    }
    public Long getReserveId() {
        return reserveId;
    }
    public void setReserveId(Long reserveId) {
        this.reserveId = reserveId;
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
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Long getSeatId() {
        return seatId;
    }
    public void setSeatId(Long seatId) {
        this.seatId = seatId;
    }

}
