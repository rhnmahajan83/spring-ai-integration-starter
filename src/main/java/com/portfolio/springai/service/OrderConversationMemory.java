package com.portfolio.springai.service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Component
public class OrderConversationMemory {
    private static final Pattern ORDER_ID = Pattern.compile("(?i)\\bORD-[0-9]+\\b");
    private static final int MAX_TURNS = 10;
    private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();

    public String newConversation() {
        String id = UUID.randomUUID().toString();
        sessions.put(id, new Session());
        return id;
    }

    public boolean exists(String id) {
        return id != null && sessions.containsKey(id);
    }

    public List<String> resolveOrderIds(String id, String question) {
        Session session = require(id);
        List<String> explicit = ORDER_ID.matcher(question).results()
                .map(m -> m.group().toUpperCase(Locale.ROOT)).distinct().toList();
        synchronized (session) {
            if (!explicit.isEmpty()) return explicit;
            String lower = question.toLowerCase(Locale.ROOT);
            // Only resolve an implicit reference when the request refers to prior orders.
            if (lower.matches("(?s).*(which|compare|both|those|them|previous|earlier|of these|of the two).*")) {
                return List.copyOf(session.recentOrderIds);
            }
            if (lower.matches("(?s).*(it|that order|this order|its status).*")
                    && !session.recentOrderIds.isEmpty()) {
                return List.of(session.recentOrderIds.get(session.recentOrderIds.size() - 1));
            }
            return List.of();
        }
    }

    public void addTurn(String id, String question, String response, List<String> orderIds) {
        Session session = require(id);
        synchronized (session) {
            session.turns.add(new Turn(question, response));
            if (session.turns.size() > MAX_TURNS) session.turns.remove(0);
            if (!orderIds.isEmpty()) {
                session.recentOrderIds.clear();
                session.recentOrderIds.addAll(orderIds);
            }
        }
    }

    public List<Turn> history(String id) {
        Session session = require(id);
        synchronized (session) {
            return List.copyOf(session.turns);
        }
    }

    public void delete(String id) {
        if (sessions.remove(id) == null) throw new IllegalArgumentException("Unknown conversation ID");
    }

    private Session require(String id) {
        Session session = id == null ? null : sessions.get(id);
        if (session == null) throw new IllegalArgumentException("Unknown conversation ID");
        return session;
    }

    public record Turn(String question, String response) {}

    private static class Session {
        final List<Turn> turns = new ArrayList<>();
        final List<String> recentOrderIds = new ArrayList<>();
    }
}
