package com.xxicon.code.reactor.http.service;

import com.xxicon.code.reactor.http.entity.User;
import com.xxicon.code.reactor.http.repository.UserRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Flux<User> getAllUser() {
        return this.userRepository.findAll();
    }

    public Mono<User> addUser(User user) {
        return this.userRepository.save(user);
    }

    public Mono<User> getUser(long id) {
        return this.userRepository.findById(id);
    }

    public Mono<User> updateUser(User req) {
        return this.getUser(req.getId())
                .flatMap(u -> {
                    u.update(req);
                    return this.userRepository.save(u);
                });
    }

    public Mono<Void> deleteUser(long id) {
        return this.getUser(id).flatMap(this.userRepository::delete);
    }
}
