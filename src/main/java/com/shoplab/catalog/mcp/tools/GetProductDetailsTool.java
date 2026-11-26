package com.shoplab.catalog.mcp.tools;

import com.shoplab.catalog.application.GetProductDetailUseCase;
import com.shoplab.catalog.domain.Product;
import com.shoplab.catalog.mcp.format.McpTextFormatter;
import com.shoplab.catalog.shared.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class GetProductDetailsTool {

    private final GetProductDetailUseCase getProductDetailUseCase;
    private final McpTextFormatter formatter;

    public GetProductDetailsTool(GetProductDetailUseCase getProductDetailUseCase, McpTextFormatter formatter) {
        this.getProductDetailUseCase = getProductDetailUseCase;
        this.formatter = formatter;
    }

    public Map<String, Object> getDefinition() {
        return Map.of(
                "name", "getProductDetails",
                "description", 
                        "Obtiene la ficha técnica, dimensiones, peso, precio de referencia y "
                        + "disponibilidad exacta en stock de un producto por su product_id.",
                "inputSchema", Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "product_id", Map.of("type", "string", "description", 
                                        "Identificador único alfanumérico del producto")
                        ),
                        "required", List.of("product_id")
                )
        );
    }

    public String execute(Map<String, Object> arguments) {
        if (!(arguments.get("product_id") instanceof String productId) || productId.isBlank()) {
            throw new IllegalArgumentException("Debe proporcionar un product_id válido.");
        }

        try {
            Product product = getProductDetailUseCase.execute(productId);
            return formatter.formatProductDetail(product);
        } catch (BusinessException ex) {
            // An unknown id is an answer for the agent, not a failure of the tool.
            return "No se encontró ningún producto con el identificador '" + productId + "' en el catálogo.";
        }
    }
}
