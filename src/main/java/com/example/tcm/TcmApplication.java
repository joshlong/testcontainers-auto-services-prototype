package com.example.tcm;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@SpringBootApplication
public class TcmApplication {

    public static void main(String[] args) {
        SpringApplication.run(TcmApplication.class, args);
    }

    @Bean
    RouterFunction<ServerResponse> routes(CustomerRepository customerRepository) {
        return route()
                .GET("/customers", request -> ok().body(customerRepository.findAll(), Customer.class))
                .build();
    }

    @Bean
    ApplicationRunner applicationRunner(CustomerRepository repository) {
        return args -> Flux.just("Oleg", "Josh", "Michelle", "Sergei", "Richard", "Sam", "Madhura")
                .map(name -> new Customer(null, name))
                .flatMap(repository::save)
                .thenMany(repository.findAll())
                .subscribe(System.out::println);
    }
}


interface CustomerRepository extends ReactiveCrudRepository<Customer, Integer> {

}

record Customer(@Id String id, String name) {
}