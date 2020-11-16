SELECT t1."email", (SELECT COUNT(*) FROM "orders" s1 WHERE s1."customer_email" = t1."email") ordersCount, EXTRACT( YEAR from t1."registration_date"),
(SELECT COUNT(*) FROM "order_items" s1 JOIN "orders" s2 ON s1."order_id" = s2."order_id" WHERE t1."email" = s2."customer_email") itemsCount
FROM "customers" t1
WHERE EXTRACT( YEAR from t1."registration_date") > 2011
ORDER BY 3 DESC