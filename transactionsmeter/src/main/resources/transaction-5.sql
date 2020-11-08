UPDATE "books" t1 SET "price" = "price" + (SELECT COUNT(*)/10 FROM "books" s1 WHERE t1."author_id" = s1."author_id")
WHERE (
    SELECT SUM(s2."quantity") FROM "orders" s1
    JOIN "order_items" s2 ON s1."order_id" = s2."order_id"
    WHERE s2."isbn" = t1."isbn"
    GROUP BY s2."isbn"
    HAVING COUNT(*) > 2
) > 5