package com.xxicon.code.reactor.config;

import com.xxicon.code.reactor.core.action.ActionFactory;
import com.xxicon.code.reactor.core.action.AutoLoadActionFactory;
import com.xxicon.code.reactor.core.codec.AutoLoadMessageMapping;
import com.xxicon.code.reactor.core.codec.JsonMessageCodec;
import com.xxicon.code.reactor.core.codec.MessageCodec;
import com.xxicon.code.reactor.core.codec.MessageMapping;
import com.xxicon.code.reactor.core.websocket.WebSocketServerHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class WebSocketConfig {
    @Bean
    public WebSocketServerHandler webSocketServerHandler() {
        return new WebSocketServerHandler(messageCodec());
    }

    @Bean
    public HandlerMapping webSocketMapping() {
        Map<String, WebSocketHandler> map = new HashMap<>(1);
        map.put("/ws", webSocketServerHandler());

        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setOrder(Ordered.HIGHEST_PRECEDENCE);
        mapping.setUrlMap(map);
        return mapping;
    }

    @Bean
    public WebSocketHandlerAdapter webSocketHandlerAdapter() {
        return new WebSocketHandlerAdapter();
    }

    @Bean
    public ActionFactory actionFactory() {
        AutoLoadActionFactory factory = new AutoLoadActionFactory();
        factory.setPkgList(Collections.singletonList("com.xxicon.code.reactor.action"));
        return factory;
    }

    @Bean
    public MessageMapping messageMapping() {
        AutoLoadMessageMapping messageMapping = new AutoLoadMessageMapping();
        messageMapping.setPkgList(Arrays.asList("com.xxicon.code.reactor.message.request", "com.xxicon.code.reactor.message.response"));
        return messageMapping;
    }

    @Bean
    public MessageCodec<String, String> messageCodec() {
        JsonMessageCodec codec = new JsonMessageCodec();
        codec.setMessageMapping(messageMapping());
        return codec;
    }

}
