package com.xxicon.code.reactor.core.websocket;

import com.xxicon.code.reactor.core.codec.MessageCodec;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.LinkedList;
import java.util.List;

public class WebSocketServerHandler implements WebSocketHandler {
    private final MessageCodec<String, String> messageCodec;
    private final List<WebSocketSessionHandler> sessionList;
    private final DirectProcessor<WebSocketSessionHandler> connectedProcessor;

    public WebSocketServerHandler(MessageCodec<String, String> messageCodec) {
        this.messageCodec = messageCodec;
        this.sessionList = new LinkedList<>();
        this.connectedProcessor = DirectProcessor.create();
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        WebSocketSessionHandler sessionHandler = new WebSocketSessionHandler(this.messageCodec);

        sessionHandler.connected()
                .subscribe(value -> this.sessionList.add(sessionHandler));

        sessionHandler.disconnected()
                .subscribe(value -> this.sessionList.remove(sessionHandler));

        this.connectedProcessor.sink().next(sessionHandler);
        return sessionHandler.handle(session);
    }

    public Flux<WebSocketSessionHandler> connected() {
        return this.connectedProcessor;
    }
}
