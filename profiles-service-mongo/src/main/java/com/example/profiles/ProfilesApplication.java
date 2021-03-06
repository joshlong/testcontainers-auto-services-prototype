package com.example.profiles;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@SpringBootApplication
public class ProfilesApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProfilesApplication.class, args);
    }
}


interface ProfileRepository extends CrudRepository<Profile, Integer> {
}


@Document
record Profile(@Id String id, String name) {
}


@RestController
@RequestMapping(value = "/profiles", produces = MediaType.APPLICATION_JSON_VALUE)
class ProfileRestController {

    private final ProfileRepository repository;

    ProfileRestController(ProfileRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    Iterable<Profile> all() {
        return this.repository.findAll();
    }

    @PostMapping
    ResponseEntity<Profile> create(@RequestBody Profile profile) {
        var saved = this.repository.save(profile);
        return ResponseEntity.ok(saved);
    }
}
