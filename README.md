# Seat Reservation system

본 예제는 MSA/DDD/Event Storming/EDA 를 포괄하는 분석/설계/구현/운영 전단계를 커버하도록 구성한 예제입니다.
이는 클라우드 네이티브 애플리케이션의 개발에 요구되는 체크포인트들을 통과하기 위한 예시 답안을 포함합니다.
- 체크포인트 : https://workflowy.com/s/assessment-check-po/T5YrzcMewfo4J6LW


# Table of contents

- [자리 예약](#---)
  - [서비스 시나리오](#서비스-시나리오)
  - [체크포인트](#체크포인트)
  - [분석/설계](#분석설계)
  - [구현:](#구현-)
    - [DDD 의 적용](#ddd-의-적용)
    - [폴리글랏 퍼시스턴스](#폴리글랏-퍼시스턴스)
    - [폴리글랏 프로그래밍](#폴리글랏-프로그래밍)
    - [동기식 호출 과 Fallback 처리](#동기식-호출-과-Fallback-처리)
    - [비동기식 호출 과 Eventual Consistency](#비동기식-호출-과-Eventual-Consistency)
  - [운영](#운영)
    - [CI/CD 설정](#cicd설정)
    - [동기식 호출 / 서킷 브레이킹 / 장애격리](#동기식-호출-서킷-브레이킹-장애격리)
    - [오토스케일 아웃](#오토스케일-아웃)
    - [무정지 재배포](#무정지-재배포)
  - [신규 개발 조직의 추가](#신규-개발-조직의-추가)

# 서비스 시나리오

자리 예약 시스템

기능적 요구사항
1. 회원이 자리을 예약한다.
2. 회원이 예약한 자리에 대해 결제를 한다.
3. 예약 시스템은 자리가 비어있는지 확인한다.
4. 비어있으면 예약이 성공한다.
5. 비어있지 않으면 예약을 할 수 없다.
6. 회원이 퇴실하면 자리가 공석이 된다.

비기능적 요구사항
1. 트랜잭션
    1. 결제가 되지 않은 예약건은 아예 예약처리가 성립되지 않아야 한다  Sync 호출 
1. 장애격리
    1. 관리시스템이 정상적이지 않더라고 예약은 가능해야 한다.  Async (event-driven), Eventual Consistency
    1. 예약시스템이 과중되면 사용자를 받지 않고 잠시후 하도록 유도한다.  Circuit breaker, fallback
1. 성능
    1. 회원이 예약결과를 Mypage에서 확인할 수 있어야 한다. CQRS


# 체크포인트

- 분석 설계


  - 이벤트스토밍: 
    - 스티커 색상별 객체의 의미를 제대로 이해하여 헥사고날 아키텍처와의 연계 설계에 적절히 반영하고 있는가?
    - 각 도메인 이벤트가 의미있는 수준으로 정의되었는가?
    - 어그리게잇: Command와 Event 들을 ACID 트랜잭션 단위의 Aggregate 로 제대로 묶었는가?
    - 기능적 요구사항과 비기능적 요구사항을 누락 없이 반영하였는가?    

  - 서브 도메인, 바운디드 컨텍스트 분리
    - 팀별 KPI 와 관심사, 상이한 배포주기 등에 따른  Sub-domain 이나 Bounded Context 를 적절히 분리하였고 그 분리 기준의 합리성이 충분히 설명되는가?
      - 적어도 3개 이상 서비스 분리
    - 폴리글랏 설계: 각 마이크로 서비스들의 구현 목표와 기능 특성에 따른 각자의 기술 Stack 과 저장소 구조를 다양하게 채택하여 설계하였는가?
    - 서비스 시나리오 중 ACID 트랜잭션이 크리티컬한 Use 케이스에 대하여 무리하게 서비스가 과다하게 조밀히 분리되지 않았는가?
  - 컨텍스트 매핑 / 이벤트 드리븐 아키텍처 
    - 업무 중요성과  도메인간 서열을 구분할 수 있는가? (Core, Supporting, General Domain)
    - Request-Response 방식과 이벤트 드리븐 방식을 구분하여 설계할 수 있는가?
    - 장애격리: 서포팅 서비스를 제거 하여도 기존 서비스에 영향이 없도록 설계하였는가?
    - 신규 서비스를 추가 하였을때 기존 서비스의 데이터베이스에 영향이 없도록 설계(열려있는 아키택처)할 수 있는가?
    - 이벤트와 폴리시를 연결하기 위한 Correlation-key 연결을 제대로 설계하였는가?

  - 헥사고날 아키텍처
    - 설계 결과에 따른 헥사고날 아키텍처 다이어그램을 제대로 그렸는가?
    
- 구현
  - [DDD] 분석단계에서의 스티커별 색상과 헥사고날 아키텍처에 따라 구현체가 매핑되게 개발되었는가?
    - Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 데이터 접근 어댑터를 개발하였는가
    - [헥사고날 아키텍처] REST Inbound adaptor 이외에 gRPC 등의 Inbound Adaptor 를 추가함에 있어서 도메인 모델의 손상을 주지 않고 새로운 프로토콜에 기존 구현체를 적응시킬 수 있는가?
    - 분석단계에서의 유비쿼터스 랭귀지 (업무현장에서 쓰는 용어) 를 사용하여 소스코드가 서술되었는가?
  - Request-Response 방식의 서비스 중심 아키텍처 구현
    - 마이크로 서비스간 Request-Response 호출에 있어 대상 서비스를 어떠한 방식으로 찾아서 호출 하였는가? (Service Discovery, REST, FeignClient)
    - 서킷브레이커를 통하여  장애를 격리시킬 수 있는가?
  - 이벤트 드리븐 아키텍처의 구현
    - 카프카를 이용하여 PubSub 으로 하나 이상의 서비스가 연동되었는가?
    - Correlation-key:  각 이벤트 건 (메시지)가 어떠한 폴리시를 처리할때 어떤 건에 연결된 처리건인지를 구별하기 위한 Correlation-key 연결을 제대로 구현 하였는가?
    - Message Consumer 마이크로서비스가 장애상황에서 수신받지 못했던 기존 이벤트들을 다시 수신받아 처리하는가?
    - Scaling-out: Message Consumer 마이크로서비스의 Replica 를 추가했을때 중복없이 이벤트를 수신할 수 있는가
    - CQRS: Materialized View 를 구현하여, 타 마이크로서비스의 데이터 원본에 접근없이(Composite 서비스나 조인SQL 등 없이) 도 내 서비스의 화면 구성과 잦은 조회가 가능한가?

  - 폴리글랏 플로그래밍
    - 각 마이크로 서비스들이 하나이상의 각자의 기술 Stack 으로 구성되었는가?
    - 각 마이크로 서비스들이 각자의 저장소 구조를 자율적으로 채택하고 각자의 저장소 유형 (RDB, NoSQL, File System 등)을 선택하여 구현하였는가?
  - API 게이트웨이
    - API GW를 통하여 마이크로 서비스들의 집입점을 통일할 수 있는가?
    - 게이트웨이와 인증서버(OAuth), JWT 토큰 인증을 통하여 마이크로서비스들을 보호할 수 있는가?
- 운영
  - SLA 준수
    - 셀프힐링: Liveness Probe 를 통하여 어떠한 서비스의 health 상태가 지속적으로 저하됨에 따라 어떠한 임계치에서 pod 가 재생되는 것을 증명할 수 있는가?
    - 서킷브레이커, 레이트리밋 등을 통한 장애격리와 성능효율을 높힐 수 있는가?
    - 오토스케일러 (HPA) 를 설정하여 확장적 운영이 가능한가?
    - 모니터링, 앨럿팅: 
  - 무정지 운영 CI/CD (10)
    - Readiness Probe 의 설정과 Rolling update을 통하여 신규 버전이 완전히 서비스를 받을 수 있는 상태일때 신규버전의 서비스로 전환됨을 siege 등으로 증명 
    - Contract Test :  자동화된 경계 테스트를 통하여 구현 오류나 API 계약위반를 미리 차단 가능한가?


# 분석/설계


## AS-IS 조직 (Horizontally-Aligned)
  ![image](https://user-images.githubusercontent.com/487999/79684144-2a893200-826a-11ea-9a01-79927d3a0107.png)

## TO-BE 조직 (Vertically-Aligned)
  ![image](https://user-images.githubusercontent.com/487999/79684159-3543c700-826a-11ea-8d5f-a3fc0c4cad87.png)


## Event Storming 결과

### 완성된 모형

![image](https://user-images.githubusercontent.com/34739884/124378699-ce36f480-dced-11eb-94a0-161fe7a77cb6.JPG)

### 완성본에 대한 기능적/비기능적 요구사항을 커버하는지 검증

![image](https://user-images.githubusercontent.com/34739884/124379505-72229f00-dcf2-11eb-92c0-a24827d5f561.JPG)

    - 회원이 자리을 예약한다. (1)
    - 회원이 예약한 자리에 대해 결제를 한다. (2)
    - 예약 시스템은 자리가 비어있는지 확인한다. (3)
    - 비어있으면 예약이 성공한다. (3, 4)
    - 비어있지 않으면 예약을 할 수 없다. (3, 4)
    - 회원이 퇴실하면 자리가 공석이 된다. (5)

### 비기능 요구사항에 대한 검증

![image](https://user-images.githubusercontent.com/34739884/124379735-ad719d80-dcf3-11eb-889f-2a089038477f.JPG)

    - 결제가 되지 않은 예약건은 아예 예약처리가 성립되지 않아야 한다. (1)
    - 관리시스템이 정상적이지 않더라고 예약은 가능해야 한다. (2)
    - 회원이 진행도를 Mypage에서 확인할 수 있어야 한다. (3)

## 헥사고날 아키텍처 다이어그램 도출
    
![image](https://user-images.githubusercontent.com/34739884/124379905-bca51b00-dcf4-11eb-879d-6ee6424d71ad.JPG)


    - Chris Richardson, MSA Patterns 참고하여 Inbound adaptor와 Outbound adaptor를 구분함
    - 호출관계에서 PubSub 과 Req/Resp 를 구분함
    - 서브 도메인과 바운디드 컨텍스트의 분리:  각 팀의 KPI 별로 아래와 같이 관심 구현 스토리를 나눠가짐


# 구현:

분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 각 BC별로 대변되는 마이크로 서비스들을 스프링부트와 파이선으로 구현하였다. 구현한 각 서비스를 로컬에서 실행하는 방법은 아래와 같다 (각자의 포트넘버는 8081 ~ 808n 이다)

```

cd mypage
mvn spring-boot:run

cd reservation
mvn spring-boot:run

cd payment
mvn spring-boot:run 

cd management
mvn spring-boot:run  

cd seat
python policy-handler.py 
```

## DDD 의 적용

- 각 서비스내에 도출된 핵심 Aggregate Root 객체를 Entity 로 선언하였다: (예시는 pay 마이크로 서비스). 이때 가능한 현업에서 사용하는 언어 (유비쿼터스 랭귀지)를 그대로 사용하려고 노력했다. 하지만, 일부 구현에 있어서 영문이 아닌 경우는 실행이 불가능한 경우가 있기 때문에 계속 사용할 방법은 아닌것 같다. (Maven pom.xml, Kafka의 topic id, FeignClient 의 서비스 id 등은 한글로 식별자를 사용하는 경우 오류가 발생하는 것을 확인하였다)

```
package reservation.system;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Payment_table")
public class Payment {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String payType;
    private Long reserveId;
    private String reserveDate;
    private String exitDate;
    private String name;
    private Long seatId;

    @PostPersist
    public void onPostPersist(){
        Paid paid = new Paid();
        BeanUtils.copyProperties(this, paid);
        paid.publishAfterCommit();


    }


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


```
- Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 다양한 데이터소스 유형 (RDB or NoSQL) 에 대한 별도의 처리가 없도록 데이터 접근 어댑터를 자동 생성하기 위하여 Spring Data REST 의 RestRepository 를 적용하였다
```
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
    
```
- 예약 후 결과 (reservation)
![image](https://user-images.githubusercontent.com/34739884/124387984-9050c500-dd1b-11eb-8ae8-54f2ec7c1935.JPG)

- 예약 후 결과 (payment)
![image](https://user-images.githubusercontent.com/34739884/124387998-a3639500-dd1b-11eb-9f07-30ecf2fbf96e.JPG)

- 예약 후 결과 (management)
![image](https://user-images.githubusercontent.com/34739884/124388023-c5f5ae00-dd1b-11eb-8705-474b0adbfea2.JPG)

- 예약 후 결과 (seat)
![image](https://user-images.githubusercontent.com/34739884/124388031-d148d980-dd1b-11eb-9e28-0fb848bee560.JPG)

## Gateway

- Gateway를 통해 서비스들의 진입점을 통일했다.

```
server:
  port: 8088

---

spring:
  profiles: default
  cloud:
    gateway:
      routes:
        - id: mypage
          uri: http://localhost:8081
          predicates:
            - Path= /mypages/**
        - id: reservation
          uri: http://localhost:8082
          predicates:
            - Path=/reservations/**, /env/**, /reserve/** 
        - id: payment
          uri: http://localhost:8083
          predicates:
            - Path=/payments/**, /pay/**
        - id: management
          uri: http://localhost:8084
          predicates:
            - Path=/managements/** 
        - id: seat
          uri: http://localhost:8085
          predicates:
            - Path=/seats/** 
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins:
              - "*"
            allowedMethods:
              - "*"
            allowedHeaders:
              - "*"
            allowCredentials: true


---

spring:
  profiles: docker
  cloud:
    gateway:
      routes:
        - id: mypage
          uri: http://mypage:8080
          predicates:
            - Path= /mypages/**
        - id: reservation
          uri: http://reservation:8080
          predicates:
            - Path=/reservations/**, /env/**, /reserve/** 
        - id: payment
          uri: http://payment:8080
          predicates:
            - Path=/payments/**, /pay/** 
        - id: management
          uri: http://management:8080
          predicates:
            - Path=/managements/** 
        - id: seat
          uri: http://seat:8080
          predicates:
            - Path=/seats/** 
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins:
              - "*"
            allowedMethods:
              - "*"
            allowedHeaders:
              - "*"
            allowCredentials: true

server:
  port: 8080
```
- gateway port 확인
![image](https://user-images.githubusercontent.com/34739884/124408972-5a92f700-dd82-11eb-98da-c4dfa91cfb57.JPG)


## 폴리글랏 퍼시스턴스

앱프런트 (app) 는 서비스 특성상 많은 사용자의 유입과 상품 정보의 다양한 콘텐츠를 저장해야 하는 특징으로 인해 H2 DB와 HSQL DB에 부착시켰다. Reservation : HSQL

- reservation pom.xml
![image](https://user-images.githubusercontent.com/34739884/124388583-7c5a9280-dd1e-11eb-99bc-1bd6b4e53514.JPG)

- 정상동작 확인
![image](https://user-images.githubusercontent.com/34739884/124388733-17536c80-dd1f-11eb-96ed-c238f7a900a8.JPG)


## 동기식 호출 과 Fallback 처리

분석단계에서의 조건 중 하나로 예약(reservation)->결제(payment) 간의 호출은 동기식 일관성을 유지하는 트랜잭션으로 처리하기로 하였다. 호출 프로토콜은 이미 앞서 Rest Repository 에 의해 노출되어있는 REST 서비스를 FeignClient 를 이용하여 호출하도록 한다. 

- 결제서비스를 호출하기 위하여 Stub과 (FeignClient) 를 이용하여 Service 대행 인터페이스 (Proxy) 를 구현 

```
# (reservation) PaymentService.java

package reservation.system.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@FeignClient(name="payment", url="${api.payment.url}")
public interface PaymentService {

    @RequestMapping(method= RequestMethod.POST, path="/payments")
    public void pay(@RequestBody Payment payment);

}
```

- 주문을 받은 직후(@PostPersist) 결제를 요청하도록 처리
```
# Reservation.java (Entity)

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
```

- 동기식 호출에서는 호출 시간에 따른 타임 커플링이 발생하며, 결제 시스템이 장애가 나면 주문도 못받는다는 것을 확인:

```
# 결제 (pay) 서비스를 잠시 내려놓음 (ctrl+c)
```
![image](https://user-images.githubusercontent.com/34739884/124388914-ede71080-dd1f-11eb-8d6d-43ee0c7674d1.JPG)

```
#주문처리 실패
http POST localhost:8082/reservations name="Han" reserveDate="1" exitDate="2" payType="card" seatId=1   #Fail
```
![image](https://user-images.githubusercontent.com/34739884/124389078-8c737180-dd20-11eb-979c-9d2fc3bb4315.JPG)

```
#결제서비스 재기동
cd 결제
mvn spring-boot:run
```
![image](https://user-images.githubusercontent.com/34739884/124389251-48cd3780-dd21-11eb-966d-daadf1abffcc.JPG)


```
#주문처리
http POST localhost:8082/reservations name="Han" reserveDate="1" exitDate="2" payType="card" seatId=1   #Success
```
![image](https://user-images.githubusercontent.com/34739884/124389299-71553180-dd21-11eb-9c85-c1e75775ae41.JPG)

- 또한 과도한 요청시에 서비스 장애가 도미노 처럼 벌어질 수 있다. (서킷브레이커, 폴백 처리는 운영단계에서 설명한다.)




## 비동기식 호출 / 시간적 디커플링 / 장애격리 / 최종 (Eventual) 일관성 테스트


결제가 이루어진 후에 상점시스템으로 이를 알려주는 행위는 동기식이 아니라 비 동기식으로 처리하여 상점 시스템의 처리를 위하여 결제주문이 블로킹 되지 않아도록 처리한다.
 
- 이를 위하여 결제이력에 기록을 남긴 후에 곧바로 결제승인이 되었다는 도메인 이벤트를 카프카로 송출한다(Publish)
 
```
package reservation.system;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Payment_table")
public class Payment {

 ...
    @PostPersist
    public void onPostPersist(){
        Paid paid = new Paid();
        BeanUtils.copyProperties(this, paid);
        paid.publishAfterCommit();
    }
```
- 상점 서비스에서는 결제승인 이벤트에 대해서 이를 수신하여 자신의 정책을 처리하도록 PolicyHandler 를 구현한다:

```
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

        Management management = new Management();
        management.setSeatId(seatId);
        // Management management = managementRepository.findBySeatId(seatId);
        // if (management != null) {
        //     if(management.getSeatStatus().equals("Emptied"))
        //     {
                management.setReserveId(reserveId);
                management.setName(name);
                management.setReserveDate(reserveDate);
                management.setExitDate(exitDate);
                management.setPayType(payType);
                management.setSeatStatus("Reserved");
                managementRepository.save(management);
                
                System.out.println("##### seat accepted by reservation reserve #####");
                System.out.println("reserveId : " + reserveId);
        //     }
        //     else {
        //         System.out.println("##### seat number is not emptied #####");
        //         System.out.println("seatId : " + seatId);
        //     }
        // }
            
    }
           
```

상점 시스템은 주문/결제와 완전히 분리되어있으며, 이벤트 수신에 따라 처리되기 때문에, 상점시스템이 유지보수로 인해 잠시 내려간 상태라도 주문을 받는데 문제가 없다:
```
# 수신 서비스 (management) 를 잠시 내려놓음 (ctrl+c)
```

```
#주문처리
http POST localhost:8083/payments name="Han" reserveDate="1" reserveId=2 exitDate="2" payType="card" seatId=4   #Success
```
![image](https://user-images.githubusercontent.com/34739884/124390422-86808f00-dd26-11eb-9037-f70aca12f0e8.JPG)

```
#상점 서비스 기동
cd management
mvn spring-boot:run
```

```
#예약상태 확인
http localhost:8080/orders     # 예약이 정상적으로 들어옴
```
![image](https://user-images.githubusercontent.com/34739884/124390496-e2e3ae80-dd26-11eb-80c6-fc91d211a164.JPG)

## CQRS
- mypage 서비스를 구현하여 아래와 같이 view 가 출력된다.

```
#예약 후 mypage 확인
```
![image](https://user-images.githubusercontent.com/34739884/124390830-43bfb680-dd28-11eb-994e-b028f7bb7cb3.JPG)

```
#좌석 반납 후 mypage 확인
```
![image](https://user-images.githubusercontent.com/34739884/124390835-52a66900-dd28-11eb-8694-ef3a6acf4ead.JPG)



# 운영

## CI/CD 설정

- git clone

```
git clone https://github.com/HanSungHo/reservation-system.git
```

- 각 폴더에서 Dockerlizing, ACR(Azure Container Registry에 Docker Image Push하기
```
az acr build --registry user21 --image user21.azurecr.io/reservation:latest .
az acr build --registry user21 --image user21.azurecr.io/payment:latest .
az acr build --registry user21 --image user21.azurecr.io/gateway:latest .
az acr build --registry user21 --image user21.azurecr.io/mypage:latest .
az acr build --registry user21 --image user21.azurecr.io/management:latest .
az acr build --registry user21 --image user21.azurecr.io/seat:latest .
```

- deploy 하기
```
kubectl create deploy reservation --image=user21.azurecr.io/reservation:latest -n ns-seatsystem
kubectl create deploy payment --image=user21.azurecr.io/payment:latest -n ns-seatsystem
kubectl create deploy gateway --image=user21.azurecr.io/gateway:latest -n ns-seatsystem
kubectl create deploy mypage --image=user21.azurecr.io/mypage:latest -n ns-seatsystem
kubectl create deploy management --image=user21.azurecr.io/management:latest -n ns-seatsystem
kubectl create deploy seat --image=user21.azurecr.io/seat:latest -n ns-seatsystem
```

- 서비스 생성 하기
```
kubectl expose deploy reservation --type="ClusterIP" --port=8080 -n ns-seatsystem
kubectl expose deploy payment --type="ClusterIP" --port=8080 -n ns-seatsystem
kubectl expose deploy mypage --type="ClusterIP" --port=8080 -n ns-seatsystem
kubectl expose deploy gateway --type="LoadBalancer" --port=8080 -n ns-seatsystem
kubectl expose deploy management --type="ClusterIP" --port=8080 -n ns-seatsystem
kubectl expose deploy seat --type="ClusterIP" --port=8080 -n ns-seatsystem
```

- 결과
![image](https://user-images.githubusercontent.com/34739884/124408972-5a92f700-dd82-11eb-98da-c4dfa91cfb57.JPG)


## ConfigMap

- reservation의 deployment.yml

![image](https://user-images.githubusercontent.com/34739884/124409253-e7d64b80-dd82-11eb-9578-3cf637ae2c5b.JPG)


- 결과 확인

![image](https://user-images.githubusercontent.com/34739884/124409286-ffadcf80-dd82-11eb-8dce-dcd5fe734d4c.JPG)



## 서킷 브레이킹

- 1.Istio 설치

```
$ curl -L https://istio.io/downloadIstio | ISTIO_VERSION=1.7.1 TARGET_ARCH=x86_64 sh -
$ cd istio-1.7.1
$ export PATH=$PWD/bin:$PATH
$ istioctl install --set profile=demo
1-1.  설치확인
$ kubectl get pod -n istio-system

```

- 2.Istio 확인

![image](https://user-images.githubusercontent.com/34739884/124413927-9d59cc80-dd8c-11eb-969c-a34bad396ff5.JPG)


- 3.namespace 및 label 설정

```
kubectl create namespace istio-test-ns
kubectl label namespace istio-test-ns istio-injection=enabled

label에 istio-injection enabled 확인
kubectl get namespace istio-test-ns -o yaml

```

- 4.namespace로 재배포

```
kubectl create deploy gateway --image=user21.azurecr.io/gateway:latest -n istio-test-ns
kubectl create deploy reservation --image=user21.azurecr.io/reservation:latest -n istio-test-ns
kubectl create deploy mypage --image=user21.azurecr.io/mypage:latest -n istio-test-ns
kubectl create deploy payment --image=user21.azurecr.io/payment:latest -n istio-test-ns
kubectl create deploy management --image=user21.azurecr.io/management:latest -n istio-test-ns
kubectl create deploy seat --image=user21.azurecr.io/seat:latest -n istio-test-ns


kubectl expose deploy gateway --type="LoadBalancer" --port=8080 -n istio-test-ns
kubectl expose deploy reservation --type="ClusterIP" --port=8080 -n istio-test-ns
kubectl expose deploy mypage --type="ClusterIP" --port=8080 -n istio-test-ns
kubectl expose deploy payment --type="ClusterIP" --port=8080 -n istio-test-ns
kubectl expose deploy management --type="ClusterIP" --port=8080 -n istio-test-ns
kubectl expose deploy seat --type="ClusterIP" --port=8080 -n istio-test-ns

```

- 결과확인
![image](https://user-images.githubusercontent.com/34739884/124414506-dba3bb80-dd8d-11eb-82b5-e645a0a0323c.JPG)

- 5.Circuit Breaker Destination Rule 생성

```
kubectl apply -f - <<EOF
  apiVersion: networking.istio.io/v1alpha3
  kind: DestinationRule
  metadata:
    name: dr-httpbin
    namespace: istio-test-ns
  spec:
    host: gateway
    trafficPolicy:
      connectionPool:
        http:
          http1MaxPendingRequests: 1
          maxRequestsPerConnection: 1
EOF
```
- 5-1. Siege Client 접속

```
kubectl exec -it siege-88f7fdd8d-ktn5h -n istio-test-ns -- /bin/bash
```

- 정상 동작일떄 확인
- siege -c1 -t10S -v --content-type "application/json" 'http://20.194.53.163:8080/reservations POST {"seatId": "1", "name": "han"}'

![image](https://user-images.githubusercontent.com/34739884/124415436-d5164380-dd8f-11eb-963e-2d3b4cf7ea75.JPG)

- 서킷 브레이커 동작일떄 확인
- siege -c3 -t40S -v --content-type "application/json" 'http://20.194.53.163:8080/reservations POST {"seatId": "1", "name": "han"}'

![image](https://user-images.githubusercontent.com/34739884/124416063-1d823100-dd91-11eb-9e96-bb38fb4157c4.JPG)

- kali 결과

![image](https://user-images.githubusercontent.com/34739884/124416306-a9945880-dd91-11eb-89cd-25f8d8a172fe.JPG)


### 오토스케일 아웃
앞서 CB 는 시스템을 안정되게 운영할 수 있게 해줬지만 사용자의 요청을 100% 받아들여주지 못했기 때문에 이에 대한 보완책으로 자동화된 확장 기능을 적용하고자 한다. 

- Reservation Deloypment.yaml 의 오토스케일을 위한 pod 초기 cpu, max cpu 설정 적용

![image](https://user-images.githubusercontent.com/34739884/124417669-a189e800-dd94-11eb-900a-624f6ff54cbb.JPG)


- Reservation 새롭게 deploy

```
kubectl delete deploy/reservation -n ns-seatsystem
kubectl apply -f deployment.yml -n ns-seatsystem
```

- recipe 시스템에 replica를 자동으로 늘려줄 수 있도록 HPA를 설정한다. 설정은 CPU 사용량이 50%를 넘어서면 replica를 10개까지 늘려준다.

```
kubectl autoscale deploy reservation --cpu-percent=50 --min=1 --max=10 -n ns-seatsystem
```

- 부하(siege) 배포
```
kubectl apply -f - <<EOF
  apiVersion: v1
  kind: Pod
  metadata:
    name: siege
    namespace: ns-seatsystem
  spec:
    containers:
    - name: siege
      image: apexacme/siege-nginx
EOF


```
- 오토스케일이 어떻게 되고 있는지 모니터링을 걸어둔다:
```
watch -n 1 kubectl get pod -n ns-seatsystem

```
- 어느정도 시간이 흐른 후 (약 30초) 스케일 아웃이 벌어지는 것을 확인할 수 있다

![pod 늘어난거 확인](https://user-images.githubusercontent.com/34739884/124418056-88ce0200-dd95-11eb-9544-83bb17e00f41.JPG)


- siege 의 로그를 보아도 전체적인 성공률이 높아진 것을 확인 할 수 있다. 
![성공률](https://user-images.githubusercontent.com/34739884/124418085-9daa9580-dd95-11eb-8ec7-eebdd17a16e7.JPG)


## Self-healing (Liveness Probe)

* 먼저 무정지 재배포가 100% 되는 것인지 확인하기 위해서 Autoscaler 이나 CB 설정을 제거함

- liveness 설정.

![liveness](https://user-images.githubusercontent.com/34739884/124418301-2295af00-dd96-11eb-9f3e-185a62ccff52.JPG)


- seige 로 배포작업 직전에 워크로드를 모니터링 함.

```
siege -c60 -t60S -r10 -v http get http://reservation:8080/reservations
```

- 100% 밑으로 떨어진 결과.

![liveness 결과](https://user-images.githubusercontent.com/34739884/124419108-d9465f00-dd97-11eb-9322-24395de026e2.JPG)


- Restart 증가 확인

![restart 증가](https://user-images.githubusercontent.com/34739884/124419150-f2e7a680-dd97-11eb-9d81-8b3ab7a29bd5.JPG)



## 무정지 배포 Readiness

- Readiness  설정.

![readiness](https://user-images.githubusercontent.com/34739884/124419705-021b2400-dd99-11eb-8ca3-893a53489990.JPG)


- 재배포

```
kubectl apply -f deployment.yml -n ns-seatsystem
```

- seige에서 확인

```
siege -c1 -t60S -r10 -v http get http://reservation:8080/reservations
```

- 기존의 pod 종료

![하나종료](https://user-images.githubusercontent.com/34739884/124419859-4dcdcd80-dd99-11eb-8667-fca1ae429581.JPG)

- 새 pod로 교체

![새것만 남음](https://user-images.githubusercontent.com/34739884/124419867-50302780-dd99-11eb-9ece-87cce2bd0ca6.JPG)

- 100% 수행결과

![100%결과](https://user-images.githubusercontent.com/34739884/124419875-532b1800-dd99-11eb-97c7-43529f6c427e.JPG)


배포기간 동안 Availability 가 변화없기 때문에 무정지 재배포가 성공한 것으로 확인됨.


