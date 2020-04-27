package com.xxicon.code.reactor.config;

import com.xxicon.code.reactor.websocket.WebSocketServerHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class WebSocketConfig {
    @Bean
    public WebSocketServerHandler webSocketServerHandler() {
        return new WebSocketServerHandler();
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

//    @Bean
//    public RouterFunction<ServerResponse> routes() {
//        return RouterFunctions.route()
//                .GET("/", request -> ServerResponse.ok().body(BodyInserters.fromResource(new ClassPathResource("static/index.html"))))
//                .GET("/app.js", request -> ServerResponse.ok().body(BodyInserters.fromResource(new ClassPathResource("static/app.js"))))
//                .GET("/semantic.min.css", request -> ServerResponse.ok().body(BodyInserters.fromResource(new ClassPathResource("static/semantic.min.css"))))
//                .build();
//    }
}
