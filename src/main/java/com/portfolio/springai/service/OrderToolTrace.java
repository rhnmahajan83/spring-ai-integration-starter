package com.portfolio.springai.service;

import org.springframework.stereotype.Component;

@Component
public class OrderToolTrace {
    private final ThreadLocal<Trace> current = new ThreadLocal<>();

    public void begin() {
        current.set(new Trace());
    }

    public void record(String toolName, long durationMs) {
        Trace trace = current.get();
        if (trace != null) {
            trace.invoked = true;
            trace.toolName = toolName;
            trace.durationMs += durationMs;
        }
    }

    public Trace end() {
        Trace trace = current.get();
        current.remove();
        return trace == null ? new Trace() : trace;
    }

    public static class Trace {
        public boolean invoked;
        public String toolName = "none";
        public long durationMs;
    }
}
