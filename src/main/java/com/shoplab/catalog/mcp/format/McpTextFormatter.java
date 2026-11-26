package com.shoplab.catalog.mcp.format;

import com.shoplab.catalog.domain.Product;
import com.shoplab.catalog.domain.ProductDimensions;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class McpTextFormatter {

    public String formatProductList(List<Product> products) {
        if (products.isEmpty()) {
            return "No se encontraron productos en el catálogo que coincidan con la búsqueda.";
        }

        StringBuilder sb = new StringBuilder("Se encontraron los siguientes productos en el catálogo de ShopLab:\n\n");
        for (Product p : products) {
            String category = p.categoryNameEn() != null ? p.categoryNameEn() : p.categoryName();
            String price = p.referencePrice() != null ? "$" + p.referencePrice().toPlainString() : 
                    "Precio no disponible";
            String availability = p.stock() > 0 ? p.stock() + " unidades disponibles" : "AGOTADO";

            sb.append("- **ID**: ").append(p.productId())
              .append(" | **Categoría**: ").append(category)
              .append(" | **Precio Ref**: ").append(price)
              .append(" | **Stock**: ").append(availability)
              .append("\n");
        }
        return sb.toString();
    }

    public String formatProductDetail(Product p) {
        String category = p.categoryNameEn() != null ? p.categoryNameEn() : p.categoryName();
        String price = p.referencePrice() != null ? "$" + p.referencePrice().toPlainString() : "No disponible";
        String stock = p.stock() > 0 ? p.stock() + " unidades" : "AGOTADO (0 unidades)";

        StringBuilder sb = new StringBuilder();
        sb.append("Ficha técnica del producto (ID: ").append(p.productId()).append("):\n")
          .append("- Categoría: ").append(category).append("\n")
          .append("- Precio de referencia: ").append(price).append("\n")
          .append("- Stock disponible: ").append(stock).append("\n");

        if (p.weightG() != null) {
            sb.append("- Peso: ").append(p.weightG()).append(" g\n");
        }
        ProductDimensions size = p.dimensions();
        if (size != null && size.lengthCm() != null && size.heightCm() != null && size.widthCm() != null) {
            sb.append("- Dimensiones: ")
              .append(size.lengthCm()).append("x")
              .append(size.heightCm()).append("x")
              .append(size.widthCm()).append(" cm\n");
        }
        return sb.toString();
    }
}
