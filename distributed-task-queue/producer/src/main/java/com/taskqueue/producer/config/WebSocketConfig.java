package com.taskqueue.producer.config;

import com.taskqueue.producer.websocket.StatsWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final StatsWebSocketHandler handler;

    @Autowired
    public WebSocketConfig(StatsWebSocketHandler handler) { this.handler = handler; }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/ws/stats").setAllowedOrigins("*");
    }
}
