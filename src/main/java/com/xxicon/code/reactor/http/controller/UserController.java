package com.xxicon.code.reactor.http.controller;

import com.xxicon.code.reactor.http.entity.User;
import com.xxicon.code.reactor.http.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Flux<User> users() {
        return this.userService.getAllUser();
    }

    @PostMapping
    public Mono<User> add(@RequestBody User req) {
        return this.userService.addUser(req);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<User>> get(@PathVariable long id) {
        return this.userService.getUser(id)
                .map(u -> new ResponseEntity<>(u, HttpStatus.OK))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<User>> update(@PathVariable long id, @RequestBody User req) {
        if (id != req.getId()) {
            return Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        }
        return this.userService.updateUser(req)
                .map(u -> new ResponseEntity<>(u, HttpStatus.OK))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable long id) {
        return this.userService.deleteUser(id)
                .then(Mono.just(new ResponseEntity<Void>(HttpStatus.OK)))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
