package com.xxicon.code.reactor.websocket;

import com.xxicon.code.reactor.message.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;

@Slf4j
@Service
public class WebSocketServer {
    private final WebSocketServerHandler webSocketServerHandler;

    public WebSocketServer(WebSocketServerHandler webSocketServerHandler) {
        this.webSocketServerHandler = webSocketServerHandler;
    }

    @PostConstruct
    public void init() {
        this.webSocketServerHandler.connected().subscribe(sessionHandler -> {
            sessionHandler.connected().subscribe(session -> log.info("Connected [{}]", session.getId()));
            sessionHandler.disconnected().subscribe(session -> log.info("Disconnected [{}]", session.getId()));

            Flux<Message> receive = sessionHandler.receive().subscribeOn(Schedulers.elastic()).doOnNext(message -> {
                log.info("Receive [{}] {}", sessionHandler.getSessionId(), message);
                sessionHandler.send(message);
            });

            Mono<Message> receiveFirst = sessionHandler.receive().subscribeOn(Schedulers.elastic()).next();
            Flux<Message> push = Flux.interval(Duration.ofSeconds(1))
                    .subscribeOn(Schedulers.elastic())
                    .takeUntil(v -> !sessionHandler.isConnected())
                    .map(String::valueOf)
                    .map(message -> new Message("H", message))
                    .doOnNext(sessionHandler::send)
                    .doOnNext(message -> log.info("Push [{}] {}", sessionHandler.getSessionId(), message));

            receive.subscribe();
            receiveFirst.thenMany(push).subscribe();
        });
    }

    @PreDestroy
    public void shutdown() {

    }
}
