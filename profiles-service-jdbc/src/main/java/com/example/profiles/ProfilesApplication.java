package com.example.profiles;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@SpringBootApplication
public class ProfilesApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProfilesApplication.class, args);
    }
}


interface ProfileRepository extends CrudRepository<Profile, Integer> {
}


record Profile(@Id Integer id, String name) {
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
        return ResponseEntity.ok(this.repository.save(profile));
    }
}
