package reservation.system;

import reservation.system.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @Autowired SeatRepository seatRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverChecked_UpdateStatus(@Payload Checked checked){

        if(!checked.validate()) return;
        // Get Methods

        Long reserveId = checked.getReserveId();
        String name = checked.getName();
        String reserveDate = checked.getReserveDate();
        String exitDate = checked.getExitDate();
        Long seatId = checked.getSeatId();
        
        Seat seat = seatRepository.findBySeatId(seatId);

        System.out.println("seatId : " + seatId);
        System.out.println("seat : " + seat);
        if(reserveId != 0 ){
            if (seat != null) {
                seat.setReserveId(reserveId);
                seat.setName(name);
                seat.setReserveDate(reserveDate);
                seat.setExitDate(exitDate);

                seat.setSeatStatus("Reserved");

                seatRepository.save(seat);
                System.out.println("##### seat accepted by reservation reserve #####");
                System.out.println("seatId : " + seatId);
            }
        }
            
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReclaimed_UpdateStatus(@Payload Reclaimed reclaimed){

        if(!reclaimed.validate()) return;
        // Get Methods

        Long seatId = reclaimed.getSeatId();
        Long reserveId = reclaimed.getReserveId();
        Seat seat = seatRepository.findBySeatId(seatId);
        if(reserveId == 0){
            if (seat != null) {
                seat.setSeatStatus("Emptied");
                seat.setReserveId(0L);
                seat.setName("");
                seat.setReserveDate("");
                seat.setExitDate("");    
                seatRepository.save(seat); 

                System.out.println("##### return accepted by reservation return #####");
                System.out.println("seatId : " + seatId);    
            }             
            else{
                System.out.println("not found seatId : " + seatId);    
            }     
        }

        // Sample Logic //
        // System.out.println("\n\n##### listener UpdateStatus : " + reclaimed.toJson() + "\n\n");
            
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}
