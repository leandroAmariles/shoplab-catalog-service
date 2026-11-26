package com.shoplab.catalog.shared.filter;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationFilter implements WebFilter {

    public static final String CORRELATION_ID_HEADER = "X-Request-Id";
    public static final String CORRELATION_ID_MDC_KEY = "requestId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String correlationId = exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        final String finalCorrelationId = correlationId;
        exchange.getResponse().getHeaders().set(CORRELATION_ID_HEADER, finalCorrelationId);

        return chain.filter(exchange)
                .doOnEach(signal -> MDC.put(CORRELATION_ID_MDC_KEY, finalCorrelationId))
                .contextWrite(context -> context.put(CORRELATION_ID_MDC_KEY, finalCorrelationId));
    }
}
