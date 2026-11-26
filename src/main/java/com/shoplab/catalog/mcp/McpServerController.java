package com.shoplab.catalog.mcp;

import com.shoplab.catalog.mcp.tools.GetProductDetailsTool;
import com.shoplab.catalog.mcp.tools.SearchProductsTool;
import com.shoplab.catalog.shared.exception.ResponseCodeEnum;
import com.shoplab.catalog.shared.logging.SecureLogger;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Model Context Protocol server of the catalog (LAB-128). Stateless JSON-RPC 2.0: a request is answered in the body
 * of its own POST, on {@code /mcp} (Streamable HTTP) or on {@code /mcp/messages}.
 */
@RestController
@RequestMapping("/mcp")
public class McpServerController {

    private static final SecureLogger LOG = SecureLogger.getLogger(McpServerController.class);

    private static final String PROTOCOL_VERSION = "2025-06-18";
    private static final int METHOD_NOT_FOUND = -32601;
    private static final int INVALID_PARAMS = -32602;

    private final SearchProductsTool searchProductsTool;
    private final GetProductDetailsTool getProductDetailsTool;

    public McpServerController(SearchProductsTool searchProductsTool, GetProductDetailsTool getProductDetailsTool) {
        this.searchProductsTool = searchProductsTool;
        this.getProductDetailsTool = getProductDetailsTool;
    }

    /** Announces where to post the messages and then keeps the channel alive. */
    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> sseEndpoint() {
        return Flux.concat(
                Flux.just(ServerSentEvent.<String>builder().event("endpoint").data("/mcp/messages").build()),
                Flux.interval(Duration.ofSeconds(15))
                        .map(tick -> ServerSentEvent.<String>builder().comment("ping").build()));
    }

    @PostMapping(path = {"", "/messages"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Map<String, Object>>> handleJsonRpc(@RequestBody Map<String, Object> request) {
        return Mono.fromCallable(() -> process(request)).subscribeOn(Schedulers.boundedElastic());
    }

    private ResponseEntity<Map<String, Object>> process(Map<String, Object> request) {
        Object id = request.get("id");
        if (id == null) {
            // A notification (for example notifications/initialized) has no answer.
            return ResponseEntity.accepted().build();
        }
        String method = String.valueOf(request.get("method"));
        Map<?, ?> params = request.get("params") instanceof Map<?, ?> map ? map : Map.of();
        return switch (method) {
            case "initialize" -> result(id, Map.of(
                    "protocolVersion", PROTOCOL_VERSION,
                    "capabilities", Map.of("tools", Map.of()),
                    "serverInfo", Map.of("name", "catalog-service", "version", "1.0.0")));
            case "ping" -> result(id, Map.of());
            case "tools/list" -> result(id, Map.of("tools", List.of(
                    searchProductsTool.getDefinition(), getProductDetailsTool.getDefinition())));
            case "tools/call" -> callTool(id, params);
            default -> error(id, METHOD_NOT_FOUND, "Method not supported: " + method);
        };
    }

    private ResponseEntity<Map<String, Object>> callTool(Object id, Map<?, ?> params) {
        if (!(params.get("name") instanceof String toolName)) {
            return error(id, INVALID_PARAMS, "Missing tool name");
        }
        Map<String, Object> arguments = new LinkedHashMap<>();
        if (params.get("arguments") instanceof Map<?, ?> map) {
            map.forEach((key, value) -> arguments.put(String.valueOf(key), value));
        }
        try {
            return switch (toolName) {
                case "searchProducts", "search_products" ->
                        toolResult(id, searchProductsTool.execute(arguments), false);
                case "getProductDetails", "get_product_details" ->
                        toolResult(id, getProductDetailsTool.execute(arguments), false);
                default -> error(id, METHOD_NOT_FOUND, "Tool not found: " + toolName);
            };
        } catch (IllegalArgumentException ex) {
            return toolFailure(id, ResponseCodeEnum.INVALID_PARAMETERS, ex.getMessage());
        } catch (RuntimeException ex) {
            // The agent only learns the code: no trace, SQL or credentials leave the service.
            LOG.error("MCP tool execution failed", ex);
            return toolFailure(id, ResponseCodeEnum.DATABASE_UNAVAILABLE,
                    ResponseCodeEnum.DATABASE_UNAVAILABLE.getDescription());
        }
    }

    private static ResponseEntity<Map<String, Object>> toolFailure(Object id, ResponseCodeEnum code, String detail) {
        return toolResult(id, code.getCode() + ": " + detail, true);
    }

    private static ResponseEntity<Map<String, Object>> toolResult(Object id, String text, boolean isError) {
        return result(id, Map.of(
                "content", List.of(Map.of("type", "text", "text", text)),
                "isError", isError));
    }

    private static ResponseEntity<Map<String, Object>> result(Object id, Map<String, Object> result) {
        return ResponseEntity.ok(Map.of("jsonrpc", "2.0", "id", id, "result", result));
    }

    private static ResponseEntity<Map<String, Object>> error(Object id, int code, String message) {
        return ResponseEntity.ok(Map.of(
                "jsonrpc", "2.0", "id", id, "error", Map.of("code", code, "message", message)));
    }
}
