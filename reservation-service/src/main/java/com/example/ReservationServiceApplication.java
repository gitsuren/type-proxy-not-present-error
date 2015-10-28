package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.sleuth.Sampler;
import org.springframework.cloud.sleuth.sampler.AlwaysSampler;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.stream.Stream;

@EnableBinding(Sink.class)
@EnableDiscoveryClient
@SpringBootApplication
public class ReservationServiceApplication {

    @Bean
    Sampler sampler (){
        return new AlwaysSampler() ;
    }

    @Bean
    CommandLineRunner runner(ReservationRepository rr) {
        return args -> Stream.of("James", "Josh", "Dave", "Phil", "Cote")
                .forEach(x -> rr.save(new Reservation(x)));
    }

    @Bean
    HealthIndicator healthIndicator() {
        return () -> Health.status("I <3 James Ward!").build();
    }

    public static void main(String[] args) {
        SpringApplication.run(ReservationServiceApplication.class, args);
    }
}

@MessageEndpoint
class ReservationMessageEndpoint {

    @ServiceActivator(inputChannel = Sink.INPUT)
    public void acceptNewReservations(String rn) {
        this.reservationRepository.save(new Reservation(rn));
    }

    @Autowired
    private ReservationRepository reservationRepository;
}

@RestController
@RefreshScope
class MessageRestController {

    @Value("${message}")
    private String msg;

    @RequestMapping("/message")
    String msg() {
        return this.msg;
    }
}

@Entity
class Reservation {

    @Override
    public String toString() {
        return "Reservation{" + "id=" + id + ", reservationName='" + reservationName + '\'' + '}';
    }

    Reservation() { // why JPA why??
    }

    public Reservation(String reservationName) {

        this.reservationName = reservationName;
    }

    @Id
    @GeneratedValue

    private Long id;

    private String reservationName;

    public Long getId() {
        return id;
    }

    public String getReservationName() {
        return reservationName;
    }
}
