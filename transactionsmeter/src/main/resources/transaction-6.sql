DECLARE
  selected_book VARCHAR2(13);
  to_sell NUMBER(5);
BEGIN
    SELECT * INTO selected_book FROM ( SELECT "isbn" FROM "books" ORDER BY "quantity_in_stock" DESC ) WHERE ROWNUM = 1;

    SELECT COUNT(*) INTO to_sell FROM "orders" O
    WHERE
    O."status"='ORDER_PLACED'
    AND
    O."customer_email" IN
        (
            SELECT "email" FROM "customers" C
                WHERE
                    (SELECT DISTINCT COUNT("isbn") FROM "order_items" OI JOIN "orders" O ON OI."order_id"=O."order_id" WHERE O."customer_email"= C."email") > 30
                    AND
                    C."email" NOT IN (SELECT "customer_email" FROM "orders" O JOIN "order_items" OI ON O."order_id"=OI."order_id" WHERE OI."isbn"=selected_book)
        );

    INSERT INTO "order_items" ("isbn", "order_id", "quantity")
    SELECT selected_book, "order_id", 1 FROM "orders" O
    WHERE
        O."status"='ORDER_PLACED'
        AND
        O."customer_email" IN
            (
                SELECT "email" FROM "customers" C
                    WHERE
                        (SELECT DISTINCT COUNT("isbn") FROM "order_items" OI JOIN "orders" O ON OI."order_id"=O."order_id" WHERE O."customer_email"= C."email") > 30
                        AND
                        C."email" NOT IN (SELECT "customer_email" FROM "orders" O JOIN "order_items" OI ON O."order_id"=OI."order_id" WHERE OI."isbn"=selected_book)
            );

    UPDATE "books" SET "quantity_sold" = "quantity_sold" + to_sell, "quantity_in_stock" = "quantity_in_stock" - to_sell WHERE "isbn" = selected_book;
END;
