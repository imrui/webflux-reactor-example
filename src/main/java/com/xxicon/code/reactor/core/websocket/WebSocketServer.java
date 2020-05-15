package com.xxicon.code.reactor.core.websocket;

import com.xxicon.code.reactor.core.Message;
import com.xxicon.code.reactor.core.action.Action;
import com.xxicon.code.reactor.core.action.ActionContext;
import com.xxicon.code.reactor.core.action.ActionFactory;
import com.xxicon.code.reactor.message.response.C001_AliveRespMsg;
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
    private final ActionFactory actionFactory;

    public WebSocketServer(WebSocketServerHandler webSocketServerHandler, ActionFactory actionFactory) {
        this.webSocketServerHandler = webSocketServerHandler;
        this.actionFactory = actionFactory;
    }

    @PostConstruct
    public void init() {
        this.webSocketServerHandler.connected().subscribe(sessionHandler -> {
            sessionHandler.connected().subscribe(session -> log.info("Connected [{}]", session.getId()));
            sessionHandler.disconnected().subscribe(session -> log.info("Disconnected [{}]", session.getId()));

            Flux<Message> receive = sessionHandler.receive().subscribeOn(Schedulers.elastic()).doOnNext(message -> {
                log.info("Receive [{}] {}", sessionHandler.getSessionId(), message);
                String cmdId = message.unique();
                Action action = this.actionFactory.getAction(cmdId);
                if (action != null) {
                    ActionContext context = new ActionContext();
                    context.setActionKey(cmdId);
                    context.setAction(action);
                    context.setChannelSession(sessionHandler);
                    context.setMessage(message);
                    Message result = action.execute(context);
                    context.setResult(result);
                    if (result != null) {
                        sessionHandler.send(result);
                    }
                } else {
                    log.warn("Action is null: cmdId = {}", cmdId);
                }
            });

            Mono<Message> receiveFirst = sessionHandler.receive().subscribeOn(Schedulers.elastic()).next();
            Flux<Message> push = Flux.interval(Duration.ofSeconds(5))
                    .subscribeOn(Schedulers.elastic())
                    .takeUntil(v -> !sessionHandler.isConnected())
                    .map(String::valueOf)
                    .map(message -> (Message) new C001_AliveRespMsg())
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
