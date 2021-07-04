package reservation.system;

import reservation.system.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class MypageViewHandler {


    @Autowired
    private MypageRepository mypageRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whenReserved_then_CREATE_1 (@Payload Reserved reserved) {
        try {

            if (!reserved.validate()) return;

            // view 객체 생성
            Mypage mypage = new Mypage();
            // view 객체에 이벤트의 Value 를 set 함
            mypage.setReserveId(reserved.getId());
            mypage.setSeatId(reserved.getSeatId());
            mypage.setName(reserved.getName());
            mypage.setReserveDate(reserved.getReserveDate());
            mypage.setExitDate(reserved.getExitDate());
            mypage.setPayType(reserved.getPayType());
            // view 레파지 토리에 save
            mypageRepository.save(mypage);
        
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whenChecked_then_UPDATE_1(@Payload Checked checked) {
        try {
            if (!checked.validate()) return;
                // view 객체 조회
            List<Mypage> mypageList = mypageRepository.findBySeatId(checked.getSeatId());
            for(Mypage mypage : mypageList){
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                mypage.setReserveId(checked.getReserveId());
                mypage.setSeatId(checked.getSeatId());
                mypage.setName(checked.getName());
                mypage.setReserveDate(checked.getReserveDate());
                mypage.setExitDate(checked.getExitDate());
                // mypage.setPayType(checked.getPayType());
                // view 레파지 토리에 save
                mypageRepository.save(mypage);
            }
            
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenChecked_then_UPDATE_2(@Payload Exited exited) {
        try {
            if (!exited.validate()) return;
                // view 객체 조회
            List<Mypage> mypageList = mypageRepository.findBySeatId(exited.getSeatId());
            for(Mypage mypage : mypageList){
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                mypage.setPayType("");
                mypage.setSeatId(0L);
                // mypage.setPayType(checked.getPayType());
                // view 레파지 토리에 save
                mypageRepository.save(mypage);
            }
            
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenReclaimed_then_DELETE_1(@Payload Exited exited) {
        try {
            if (!exited.validate()) return;
            // view 레파지 토리에 삭제 쿼리
            mypageRepository.deleteBySeatId(exited.getSeatId());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}