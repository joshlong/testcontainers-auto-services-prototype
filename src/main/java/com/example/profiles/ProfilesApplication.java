package com.example.profiles;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Stream;

@Slf4j
@SpringBootApplication
public class ProfilesApplication {

    @Bean
    ApplicationRunner applicationRunner(CustomerRepository repository) {
        return args -> Stream.of("Sergei", "Olga", "Oliver", "Jens", "Josh")
                .map(c -> new Customer(null, c))
                .map(repository::save)
                .forEach(System.out::println);
    }

    public static void main(String[] args) {
        SpringApplication.run(ProfilesApplication.class, args);
    }
}


interface CustomerRepository extends CrudRepository<Customer, Integer> {
}


record Customer(@Id Integer id, String name) {
}


@RestController
@RequestMapping(value = "/profiles", produces = MediaType.APPLICATION_JSON_VALUE)
class ProfileRestController {

    private final CustomerRepository repository;

    ProfileRestController(CustomerRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    Iterable<Customer> all() {
        return this.repository.findAll();
    }

    @PostMapping
    ResponseEntity<Customer> create(@RequestBody Customer profile) {
        var saved = this.repository.save(profile);
        return ResponseEntity.ok(saved);
    }
}
