package com.taskqueue.producer.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskqueue.producer.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class StatsWebSocketHandler extends TextWebSocketHandler {
    private final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();
    private final ObjectMapper mapper = new ObjectMapper();
    private final StatsService stats;

    @Autowired
    public StatsWebSocketHandler(StatsService stats) { this.stats = stats; }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) { sessions.add(session); }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) { sessions.remove(session); }

    @Scheduled(fixedRate = 2000)
    public void broadcast() throws Exception {
        if (sessions.isEmpty()) return;
        String json = mapper.writeValueAsString(stats.getStats());
        TextMessage msg = new TextMessage(json);
        for (WebSocketSession s : sessions) {
            if (s.isOpen()) try { s.sendMessage(msg); } catch (Exception ignored) {}
        }
    }
}
