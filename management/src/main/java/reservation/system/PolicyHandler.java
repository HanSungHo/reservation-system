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
    @Autowired ManagementRepository managementRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPaid_Check(@Payload Paid paid){

        if(!paid.validate()) return;
        // Get Methods
        Long reserveId = paid.getReserveId();
        String name = paid.getName();
        String reserveDate = paid.getReserveDate();
        String exitDate = paid.getExitDate();
        String payType = paid.getPayType();
        Long seatId = paid.getSeatId();

        Management management = managementRepository.findBySeatId(seatId);
        if (management != null) {
            if(management.getSeatStatus().equals("Emptied"))
            {
                management.setReserveId(reserveId);
                management.setName(name);
                management.setReserveDate(reserveDate);
                management.setExitDate(exitDate);
                management.setPayType(payType);
                management.setSeatStatus("Reserved");
                managementRepository.save(management);
                
                System.out.println("##### seat accepted by reservation reserve #####");
                System.out.println("reserveId : " + reserveId);
            }
            else {
                System.out.println("##### seat number is not emptied #####");
                System.out.println("seatId : " + seatId);
            }
        }
            
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverExited_Reclaim(@Payload Exited exited){

        if(!exited.validate()) return;
        // Get Methods
        Long seatId = exited.getSeatId();
        
        System.out.println("##### seat recalim #####");
        System.out.println("seatId : " + seatId);
        Management management = managementRepository.findBySeatId(seatId);

        System.out.println("##### management recalim #####");
        System.out.println("management : " + management);

        if (management != null) {
            if(management.getSeatStatus().equals("Reserved"))
            {
                management.setReserveId(0L);
                management.setName("");
                management.setReserveDate("");
                management.setExitDate("");
                management.setPayType("");
                management.setSeatStatus("Emptied");
                managementRepository.save(management);
                
                System.out.println("##### seat recalim #####");
                System.out.println("seatId : " + seatId);
            }
        }

            
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}
