UPDATE "books" B
    SET "quantity_in_stock" = B."quantity_in_stock" + (SELECT SUM("quantity") FROM "order_items" WHERE
            "isbn" = B."isbn"
            AND
            "order_id" IN (SELECT "order_id" FROM "orders" WHERE "status"='ORDER_PLACED') ),
            "quantity_sold" = B."quantity_sold" - (SELECT SUM("quantity") FROM "order_items" WHERE
                "isbn" = B."isbn"
                AND
                "order_id" IN (SELECT "order_id" FROM "orders" WHERE "status"='ORDER_PLACED') )
    WHERE
        B."publisher_id" IN (SELECT "publisher_id" FROM "publishers" WHERE "city" = 'Yutou')
    AND
        B."isbn" IN (SELECT "isbn" FROM "order_items" WHERE "order_id" IN (SELECT "order_id" FROM "orders" WHERE "status"='ORDER_PLACED'))



