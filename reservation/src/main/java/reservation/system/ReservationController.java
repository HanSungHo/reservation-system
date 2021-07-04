package reservation.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.PrintWriter;
import java.util.List;

 @RestController
 public class ReservationController {

    @Autowired
    ReservationRepository reservationRepository;

    @RequestMapping(value = "/env",
    method = RequestMethod.GET,
    produces = "application/json;charset=UTF-8")

    public void env(HttpServletRequest request, HttpServletResponse response)
        throws Exception {
        System.out.println("##### /env  called #####");

        String env_DB_IP = System.getenv("DB_IP");
        String env_DB_SERVICE_NAME = System.getenv("DB_SERVICE_NAME");

        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");

        PrintWriter writer = response.getWriter();
        writer.write("DB IP : " + env_DB_IP +"\r\n");
        writer.write("DB SERVICE_NAME : " + env_DB_SERVICE_NAME);
        writer.flush();
        writer.close();
    }

 }
