package com.xxicon.code.reactor.core.websocket;

import com.xxicon.code.reactor.core.Message;
import com.xxicon.code.reactor.core.channel.ChannelSession;
import com.xxicon.code.reactor.core.codec.MessageCodec;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;
import reactor.core.publisher.ReplayProcessor;
import reactor.netty.channel.AbortedException;

import java.nio.channels.ClosedChannelException;

public class WebSocketSessionHandler implements ChannelSession {
    private final MessageCodec<String, String> messageCodec;
    private final ReplayProcessor<Message> receiveProcessor;
    private final MonoProcessor<WebSocketSession> connectedProcessor;
    private final MonoProcessor<WebSocketSession> disconnectedProcessor;
    private boolean webSocketConnected;
    private WebSocketSession session;

    public WebSocketSessionHandler(MessageCodec<String, String> messageCodec) {
        this(messageCodec, 25);
    }

    public WebSocketSessionHandler(MessageCodec<String, String> messageCodec, int historySize) {
        this.messageCodec = messageCodec;
        this.receiveProcessor = ReplayProcessor.create(historySize);
        this.connectedProcessor = MonoProcessor.create();
        this.disconnectedProcessor = MonoProcessor.create();
        this.webSocketConnected = false;
    }

    protected Mono<Void> handle(WebSocketSession session) {
        this.session = session;

        Flux<Message> receive = session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .map(this.messageCodec::decode)
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

    @Override
    public void send(Message message) {
        if (this.webSocketConnected) {
            this.session.send(Mono.just(this.session.textMessage(this.messageCodec.encode(message))))
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
