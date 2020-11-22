UPDATE "orders" O SET
        "total_price" = "total_price" - (SELECT (SUM("quantity") * SUM("price")) FROM "order_items" OI JOIN "books" B ON OI."isbn"=B."isbn"
        WHERE
            "order_id"=O."order_id"
        AND
            B."publisher_id" IN (SELECT "publisher_id" FROM "publishers" WHERE "country" = 'Russia')
        AND
            B."isbn" IN (SELECT "isbn" FROM "order_items" WHERE "order_id" IN (SELECT "order_id" FROM "orders" WHERE "status"='ORDER_PLACED')))
    WHERE
        "order_id" IN (SELECT "order_id" FROM "orders" WHERE "status"='ORDER_PLACED')
    AND
        "order_id" IN (SELECT "order_id" FROM "order_items" WHERE "isbn" IN ( SELECT "isbn" FROM "books" WHERE "publisher_id" IN (SELECT "publisher_id" FROM "publishers" WHERE "country" = 'Russia') ))
