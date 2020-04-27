package com.xxicon.code.reactor.websocket;

import com.alibaba.fastjson.JSON;
import com.xxicon.code.reactor.message.Message;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;
import reactor.core.publisher.ReplayProcessor;
import reactor.netty.channel.AbortedException;

import java.nio.channels.ClosedChannelException;

public class WebSocketSessionHandler {
    private final ReplayProcessor<Message> receiveProcessor;
    private final MonoProcessor<WebSocketSession> connectedProcessor;
    private final MonoProcessor<WebSocketSession> disconnectedProcessor;
    private boolean webSocketConnected;
    private WebSocketSession session;

    public WebSocketSessionHandler() {
        this(25);
    }

    public WebSocketSessionHandler(int historySize) {
        this.receiveProcessor = ReplayProcessor.create(historySize);
        this.connectedProcessor = MonoProcessor.create();
        this.disconnectedProcessor = MonoProcessor.create();
        this.webSocketConnected = false;
    }

    protected Mono<Void> handle(WebSocketSession session) {
        this.session = session;

        Flux<Message> receive = session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .map(message -> JSON.parseObject(message, Message.class))
                .doOnNext(this.receiveProcessor::onNext)
                .doOnComplete(this.receiveProcessor::onComplete);

        Mono<Object> connected = Mono.fromRunnable(() -> {
            this.webSocketConnected = true;
            this.connectedProcessor.onNext(session);
        });

        Mono<Object> disconnected = Mono.fromRunnable(() -> {
            this.webSocketConnected = false;
            this.disconnectedProcessor.onNext(session);
        });

        return connected.thenMany(receive).then(disconnected).then();
    }

    public Mono<WebSocketSession> connected() {
        return this.connectedProcessor;
    }

    public Mono<WebSocketSession> disconnected() {
        return this.disconnectedProcessor;
    }

    public Flux<Message> receive() {
        return this.receiveProcessor;
    }

    public void send(Message message) {
        if (this.webSocketConnected) {
            this.session.send(Mono.just(this.session.textMessage(JSON.toJSONString(message))))
                    .doOnError(ClosedChannelException.class, t -> this.connectionClosed())
                    .doOnError(AbortedException.class, t -> this.connectionClosed())
                    .onErrorResume(ClosedChannelException.class, t -> Mono.empty())
                    .onErrorResume(AbortedException.class, t -> Mono.empty())
                    .subscribe();
        }
    }

    private void connectionClosed() {
        if (this.webSocketConnected) {
            this.webSocketConnected = false;
            this.disconnectedProcessor.onNext(session);
        }
    }

    public boolean isConnected() {
        return this.webSocketConnected;
    }

    public String getSessionId() {
        if (this.session == null) {
            return "";
        }
        return this.session.getId();
    }
}
