CREATE USER backupuser IDENTIFIED BY pw_backup;

GRANT CREATE SESSION, ALTER SESSION, CREATE DATABASE LINK,
  CREATE MATERIALIZED VIEW, CREATE PROCEDURE, CREATE PUBLIC SYNONYM,
  CREATE ROLE, CREATE SEQUENCE, CREATE SYNONYM, CREATE TABLE,
  CREATE TRIGGER, CREATE TYPE, CREATE VIEW, UNLIMITED TABLESPACE
  to backupuser;

/*  
DROP TABLE "backup_order_items";
DROP TABLE "backup_orders" ;
DROP TABLE "backup_addresses" ;
DROP TABLE "backup_customers" ;
DROP TABLE "backup_reviews" ;
DROP TABLE "backup_books" ;
DROP TABLE "backup_authors" ;
DROP TABLE "backup_publishers" ;
DROP TABLE "backup_categories";
*/

ALTER SESSION SET CURRENT_SCHEMA = backupuser;
CREATE TABLE "backup_categories"
(
    "category_id" NUMBER(5) NOT NULL,
    "name" VARCHAR2(255) NOT NULL,
    "short_name" VARCHAR2(10) NOT NULL,
    "description" VARCHAR2(1000),
    CONSTRAINT "backup_categories_pk" PRIMARY KEY ("category_id")
);

CREATE TABLE "backup_publishers"
(
    "publisher_id" NUMBER(5) NOT NULL,
    "name" VARCHAR2(255) NOT NULL,
    "year_of_foundation" NUMBER(4),
    "description" VARCHAR2(1000),
    "website" VARCHAR2(255),
    "city" VARCHAR2(255),
    "country" VARCHAR2(255),
    CONSTRAINT "backup_publishers_pk" PRIMARY KEY ("publisher_id")
);

CREATE TABLE "backup_authors"
(
    "author_id" NUMBER(5) NOT NULL,
    "names" VARCHAR2(255) NOT NULL,
    "surname" VARCHAR2(255) NOT NULL,
    "nationality" VARCHAR2(255),
    "nickname" VARCHAR2(255),
    "bio" VARCHAR2(1000),
    CONSTRAINT "backup_authors_pk" PRIMARY KEY ("author_id")
);

CREATE TABLE "backup_books"
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
    CONSTRAINT "backup_books_pk" PRIMARY KEY ("isbn"),
    CONSTRAINT "backup_fk_category" FOREIGN KEY ("category_id") REFERENCES "backup_categories"("category_id"),
    CONSTRAINT "backup_fk_publisher" FOREIGN KEY ("publisher_id") REFERENCES "backup_publishers"("publisher_id"),
    CONSTRAINT "backup_fk_author" FOREIGN KEY ("author_id") REFERENCES "backup_authors"("author_id"),
    CONSTRAINT "backup_price_gt_0" CHECK ("price" > 0),
    CONSTRAINT "backup_quantity_in_stock_gtq_0" CHECK ("quantity_in_stock" >= 0),
    CONSTRAINT "backup_quantity_sold_gtq_0" CHECK ("quantity_sold" >= 0)
);

CREATE TABLE "backup_reviews"
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
    CONSTRAINT "backup_reviews_pk" PRIMARY KEY ("review_id"),
    CONSTRAINT "backup_fk_book" FOREIGN KEY ("isbn") REFERENCES "backup_books"("isbn")
);

CREATE TABLE "backup_customers"
(
    "email" VARCHAR2(255) NOT NULL,
    "username" VARCHAR2(255) NOT NULL,
    "password" VARCHAR2(255) NOT NULL,
    "phone_number" VARCHAR2(15),
    "gender" CHAR(1),
    "birth_date" DATE,
    "registration_date" DATE NOT NULL,
    CONSTRAINT "backup_customers_pk" PRIMARY KEY ("email")
);

CREATE TABLE "backup_addresses"
(
    "address_id" NUMBER(5) NOT NULL,
    "street" VARCHAR2(255) NOT NULL,
    "postcode" VARCHAR2(6) NOT NULL,
    "city" VARCHAR2(255) NOT NULL,
    "house_number" VARCHAR2(10) NOT NULL,
    "apartment_number" VARCHAR2(10),
    CONSTRAINT "backup_addresses_pk" PRIMARY KEY ("address_id")
);

CREATE TABLE "backup_orders"
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
    CONSTRAINT "backup_orders_pk" PRIMARY KEY ("order_id"),
    CONSTRAINT "backup_fk_customer" FOREIGN KEY ("customer_email") REFERENCES "backup_customers"("email"),
    CONSTRAINT "backup_fk_delivery_address" FOREIGN KEY ("delivery_address_id") REFERENCES "backup_addresses"("address_id"),
    CONSTRAINT "backup_fk_invoice_address" FOREIGN KEY ("invoice_address_id") REFERENCES "backup_addresses"("address_id"),
    CONSTRAINT "backup_dates_check" CHECK ("modification_date" >= "creation_date"),
    CONSTRAINT "backup_status_enum" CHECK ("status" IN ('ORDER_PLACED', 'ORDER_CONFIRMED', 'ORDER_SHIPPED' ,'ORDER_CANCELLED'))
);

CREATE TABLE "backup_order_items"
(
    "isbn" VARCHAR(13) NOT NULL,
    "order_id" NUMBER(5) NOT NULL,
    "quantity" NUMBER(5) NOT NULL,
    CONSTRAINT "backup_order_items_pk" PRIMARY KEY ("isbn", "order_id"),
    CONSTRAINT "backup_fk_isbn" FOREIGN KEY ("isbn") REFERENCES "backup_books"("isbn"),
    CONSTRAINT "backup_fk_order" FOREIGN KEY ("order_id") REFERENCES "backup_orders"("order_id")
);

insert into "backup_categories" ( select * from zsbduser."categories");
insert into "backup_publishers" ( select * from zsbduser."publishers");
insert into "backup_authors" ( select * from zsbduser."authors");
insert into "backup_books" ( select * from zsbduser."books");
insert into "backup_reviews" ( select * from zsbduser."reviews");
insert into "backup_customers" ( select * from zsbduser."customers");
insert into "backup_addresses" ( select * from zsbduser."addresses");
insert into "backup_orders" ( select * from zsbduser."orders");
insert into "backup_order_items" ( select * from zsbduser."order_items");

ALTER SESSION SET CURRENT_SCHEMA = system;
