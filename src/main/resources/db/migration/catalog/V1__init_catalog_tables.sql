-- Tables of the 'catalog' schema (LAB-113, ADR-0031).
-- Nothing here references another schema: products, categories and sellers are self-contained.

CREATE TABLE product_categories (
    name    text PRIMARY KEY,
    name_en text
);

CREATE TABLE products (
    product_id         text PRIMARY KEY,
    category_name      text REFERENCES product_categories (name) ON UPDATE CASCADE ON DELETE SET NULL,
    name_length        integer,
    description_length integer,
    photos_count       integer,
    weight_g           integer CHECK (weight_g >= 0),
    length_cm          integer CHECK (length_cm >= 0),
    height_cm          integer CHECK (height_cm >= 0),
    width_cm           integer CHECK (width_cm >= 0),
    reference_price    numeric(10,2) CHECK (reference_price >= 0),
    stock              integer NOT NULL DEFAULT 0 CHECK (stock >= 0),
    created_at         timestamptz NOT NULL DEFAULT now(),
    updated_at         timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX products_category_name_idx ON products (category_name);
CREATE INDEX products_stock_idx ON products (stock);
CREATE INDEX products_reference_price_idx ON products (reference_price);

CREATE TABLE sellers (
    seller_id       text PRIMARY KEY,
    zip_code_prefix text,
    city            text,
    state           text
);
