# catalog-service

Catálogo e inventario de ShopLab Helpdesk (LAB-113, ADR-0031): productos, categorías y vendedores de Olist en su propia
base `catalog`. Especificación en `shoplab-helpdesk/specs/LAB-113-catalog-service/`.

## API

| Ruta | Uso |
|---|---|
| `GET /api/catalog/products` | `page`, `size` (máx. 200), `category`, `query`, `inStock`; ordenado por `product_id` |
| `GET /api/catalog/products/{id}` | Ficha técnica, precio y stock |
| `GET /api/catalog/products/{id}/stock` | Stock y precio |
| `GET /api/catalog/categories` | Categorías en portugués e inglés |
| `POST /mcp` | Servidor MCP: `searchProducts` y `getProductDetails` (también `/mcp/sse` y `/mcp/messages`) |

Olist no tiene títulos de producto: `query` busca cada palabra en el ID y en la categoría (portugués o inglés). La
búsqueda semántica es de `retrieval-service` (LAB-34). El gateway solo publica `/api/catalog/**`; `/mcp` es interno.

Contrato: `openapi/catalog/v1/catalog-api.yaml` de `shoplab-contracts`.

## Datos

Flyway crea las tablas vacías. Los datos se copian desde la base del monolito con
`bash scripts/migrate-catalog.sh` de `shoplab-helpdesk`.

## Construir

```bash
./mvnw verify   # necesita Docker y acceso a GitHub Packages (servidor `github-shoplab-contracts` en settings.xml)
GITHUB_TOKEN=... docker build --secret id=github_token,env=GITHUB_TOKEN -t catalog-service .
```

## Publicar y desplegar

El repositorio necesita la variable `DEPLOY_APP_ID` y los secretos `DEPLOY_APP_PRIVATE_KEY` y `RELEASE_PLEASE_TOKEN`.
No lleva autenticación hasta que exista el login (LAB-61).
