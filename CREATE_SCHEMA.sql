CREATE TABLE "categories"
(
    "category_id" NUMBER(5) NOT NULL,
    "name" VARCHAR2(255) NOT NULL,
    "short_name" VARCHAR2(10) NOT NULL,
    "description" VARCHAR2(1000),
    CONSTRAINT "categories_pk" PRIMARY KEY ("category_id")
);

CREATE TABLE "publishers"
(
    "publisher_id" NUMBER(5) NOT NULL,
    "name" VARCHAR2(255) NOT NULL,
    "year_of_foundation" NUMBER(4),
    "description" VARCHAR2(1000),
    "website" VARCHAR2(255),
    "city" VARCHAR2(255),
    "country" VARCHAR2(255),
    CONSTRAINT "publishers_pk" PRIMARY KEY ("publisher_id")
);

CREATE TABLE "authors"
(
    "author_id" NUMBER(5) NOT NULL,
    "names" VARCHAR2(255) NOT NULL,
    "surname" VARCHAR2(255) NOT NULL,
    "nationality" VARCHAR2(255),
    "nickname" VARCHAR2(255),
    "bio" VARCHAR2(1000),
    CONSTRAINT "authors_pk" PRIMARY KEY ("author_id")
);

CREATE TABLE "books"
(
    "isbn" VARCHAR2(13) NOT NULL,
    "title" VARCHAR2(255) NOT NULL,
    "description" VARCHAR2(1000),
    "publication_year" NUMBER(4) NOT NULL,
    "number_of_pages" NUMBER(4) NOT NULL,
    "hard_cover" NUMBER(1) NOT NULL,
    "language" VARCHAR(255),
    "price" NUMBER(5, 2) NOT NULL,
    "quantity_in_stock" NUMBER(5) NOT NULL,
    "quantity_sold" NUMBER(5) NOT NULL,
    "format" VARCHAR2(5),
    "category_id" NUMBER(5) NOT NULL,
    "publisher_id" NUMBER(5) NOT NULL,
    "author_id" NUMBER(5) NOT NULL,
    CONSTRAINT "books_pk" PRIMARY KEY ("isbn"),
    CONSTRAINT "fk_category" FOREIGN KEY ("category_id") REFERENCES "categories"("category_id"),
    CONSTRAINT "fk_publisher" FOREIGN KEY ("publisher_id") REFERENCES "publishers"("publisher_id"),
    CONSTRAINT "fk_author" FOREIGN KEY ("author_id") REFERENCES "authors"("author_id"),
    CONSTRAINT "price_gt_0" CHECK ("price" > 0),
    CONSTRAINT "quantity_in_stock_gtq_0" CHECK ("quantity_in_stock" >= 0),
    CONSTRAINT "quantity_sold_gtq_0" CHECK ("quantity_sold" >= 0)
);

CREATE TABLE "reviews"
(
    "review_id" NUMBER(5) NOT NULL, 
    "author" VARCHAR2(255) NOT NULL,
    "rating" NUMBER(2) NOT NULL,
    "content" VARCHAR2(1000) NOT NULL,
    "creation_date" DATE NOT NULL,
    "pros" VARCHAR2(255),
    "cons" VARCHAR2(255),
    "recommended" NUMBER(1) NOT NULL,
    "isbn" VARCHAR2(13) NOT NULL, 
    CONSTRAINT "reviews_pk" PRIMARY KEY ("review_id"),
    CONSTRAINT "fk_book" FOREIGN KEY ("isbn") REFERENCES "books"("isbn")
);

CREATE TABLE "customers"
(
    "email" VARCHAR2(255) NOT NULL,
    "username" VARCHAR2(255) NOT NULL,
    "password" VARCHAR2(255) NOT NULL,
    "phone_number" VARCHAR2(15),
    "gender" CHAR(1),
    "birth_date" DATE,
    "registration_date" DATE NOT NULL,
    CONSTRAINT "customers_pk" PRIMARY KEY ("email")
);

CREATE TABLE "addresses"
(
    "address_id" NUMBER(5) NOT NULL,
    "street" VARCHAR2(255) NOT NULL,
    "postcode" VARCHAR2(6) NOT NULL,
    "city" VARCHAR2(255) NOT NULL,
    "house_number" VARCHAR2(10) NOT NULL,
    "apartment_number" VARCHAR2(10),
    CONSTRAINT "addresses_pk" PRIMARY KEY ("address_id")
);

CREATE TABLE "orders"
(
    "order_id" NUMBER(5) NOT NULL,  
    "creation_date" DATE NOT NULL,
    "modification_date" DATE NOT NULL,
    "total_price" NUMBER(7, 2) NOT NULL,
    "carrier" VARCHAR2(255) NOT NULL,
    "status" VARCHAR2(16) NOT NULL,
    "comment" VARCHAR2(1000),
    "method_of_payment" VARCHAR2(255) NOT NULL,
    "customer_email" VARCHAR2(255) NOT NULL,
    "delivery_address_id" NUMBER(5) NOT NULL,
    "invoice_address_id" NUMBER(5) NOT NULL,
    CONSTRAINT "orders_pk" PRIMARY KEY ("order_id"),
    CONSTRAINT "fk_customer" FOREIGN KEY ("customer_email") REFERENCES "customers"("email"),
    CONSTRAINT "fk_delivery_address" FOREIGN KEY ("delivery_address_id") REFERENCES "addresses"("address_id"),
    CONSTRAINT "fk_invoice_address" FOREIGN KEY ("invoice_address_id") REFERENCES "addresses"("address_id"),
    CONSTRAINT "dates_check" CHECK ("modification_date" >= "creation_date"),
    CONSTRAINT "status_enum" CHECK ("status" IN ('ORDER_PLACED', 'ORDER_CONFIRMED', 'ORDER_SHIPPED' ,'ORDER_CANCELLED'))
);

CREATE TABLE "order_items"
(
    "isbn" VARCHAR(13) NOT NULL,
    "order_id" NUMBER(5) NOT NULL,
    "quantity" NUMBER(5) NOT NULL,
    CONSTRAINT "order_items_pk" PRIMARY KEY ("isbn", "order_id"),
    CONSTRAINT "fk_isbn" FOREIGN KEY ("isbn") REFERENCES "books"("isbn"),
    CONSTRAINT "fk_order" FOREIGN KEY ("order_id") REFERENCES "orders"("order_id")
);

UPDATE "books" t1 SET "quantity_sold" = (SELECT SUM("quantity") FROM "order_items" t2  WHERE t1."isbn" = t2."isbn" GROUP BY "isbn");

UPDATE "orders" t1 SET "total_price" = (SELECT SUM("price") FROM (SELECT "order_id", "price" * "quantity" AS "price" FROM "books" B JOIN "order_items" OI ON B."isbn"=OI."isbn") t2  WHERE t1."order_id" = t2."order_id" GROUP BY "order_id");
