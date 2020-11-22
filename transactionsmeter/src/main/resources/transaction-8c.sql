DELETE FROM "order_items"
    WHERE
        "isbn" IN (SELECT "isbn" FROM "books"
            WHERE
            "publisher_id" IN (SELECT "publisher_id" FROM "publishers" WHERE "country" = 'Russia'))
    AND
        "order_id" IN (SELECT "order_id" FROM "orders" WHERE "status"='ORDER_PLACED')