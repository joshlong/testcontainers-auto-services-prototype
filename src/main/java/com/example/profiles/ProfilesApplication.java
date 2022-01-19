package com.example.profiles;

import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@SpringBootApplication
public class ProfilesApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProfilesApplication.class, args);
    }
}

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/profiles", produces = MediaType.APPLICATION_JSON_VALUE)
class ProfileRestController {

    private final MediaType mediaType = MediaType.APPLICATION_JSON;
    private final CustomerRepository repository;

    @DeleteMapping
    Publisher<Void> deleteAll() {
        return this.repository.deleteAll();
    }

    @GetMapping("/{id}")
    Publisher<Customer> getByid(@PathVariable Integer id) {
        return this.repository.findById(id);
    }

    @GetMapping
    Publisher<Customer> all() {
        return this.repository.findAll();
    }

    @PostMapping
    Publisher<ResponseEntity<Customer>> create(@RequestBody Customer profile) {
        System.out.println("Received: " + profile);
        return this.repository
                .save(profile)
                .flatMap(saved -> this.repository.findById(saved.id()))
                .map(p -> ResponseEntity
                        .created(URI.create("/profiles/" + p.id()))
                        .contentType(this.mediaType)
                        .body(p)
                );
    }
}

interface CustomerRepository extends ReactiveCrudRepository<Customer, Integer> {
}

@Table("customer")
record Customer(@Id Integer id, String name) {
}
