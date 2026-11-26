package com.shoplab.catalog.mcp.tools;

import com.shoplab.catalog.application.GetProductsPageUseCase;
import com.shoplab.catalog.mcp.format.McpTextFormatter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SearchProductsTool {

    private final GetProductsPageUseCase getProductsPageUseCase;
    private final McpTextFormatter formatter;

    public SearchProductsTool(GetProductsPageUseCase getProductsPageUseCase, McpTextFormatter formatter) {
        this.getProductsPageUseCase = getProductsPageUseCase;
        this.formatter = formatter;
    }

    public Map<String, Object> getDefinition() {
        return Map.of(
                "name", "searchProducts",
                "description", 
                        "Busca productos en el catálogo de ShopLab por término de búsqueda, categoría "
                        + "y disponibilidad en stock. Devuelve precios de referencia y unidades "
                        + "disponibles.",
                "inputSchema", Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "query", Map.of("type", "string", "description", 
                                        "Palabras de la categoría en portugués o inglés (ej. "
                                        + "'watches', 'relogios') o ID del producto. "
                                        + "El catálogo no tiene títulos: traduce el término antes de buscar"),
                                "category", Map.of("type", "string", "description", "Nombre de la categoría (opcional)"),
                                "in_stock_only", Map.of("type", "boolean", "description", 
                                        "Si es true, solo devuelve productos con stock > 0 (opcional, "
                                        + "default true)"),
                                "limit", Map.of("type", "integer", "description", 
                                        "Cantidad máxima de productos a devolver (opcional, default "
                                        + "5, max 20)")
                        ),
                        "required", List.of("query")
                )
        );
    }

    public String execute(Map<String, Object> arguments) {
        if (!(arguments.get("query") instanceof String query) || query.isBlank()) {
            throw new IllegalArgumentException("Debe proporcionar el texto de búsqueda en query.");
        }
        String category = arguments.get("category") instanceof String value ? value : null;
        Boolean inStockOnly = arguments.containsKey("in_stock_only")
                ? Boolean.valueOf(arguments.get("in_stock_only").toString())
                : Boolean.TRUE;
        int limit = arguments.get("limit") instanceof Number number
                ? Math.min(20, Math.max(1, number.intValue()))
                : 5;

        var page = getProductsPageUseCase.execute(0, limit, category, query, inStockOnly);
        return formatter.formatProductList(page.content());
    }
}
