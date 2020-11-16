SELECT COUNT(*) FROM (
    SELECT COUNT(*) FROM "orders" o
    JOIN "order_items" oi ON o."order_id" = oi."order_id"
    WHERE (
        SELECT AVG(rate) FROM (
            SELECT r."rating" rate, b."isbn" isb
            FROM "reviews" r, "books" b
            WHERE  b."isbn" = r."isbn"
            AND b."publication_year" IN (2000, 2001)
        )
        WHERE isb = oi."isbn"
        GROUP BY isb
    ) > 5
    AND o."status" = 'ORDER_SHIPPED'
    GROUP BY o."order_id"
)