SELECT t1."email", (SELECT COUNT(*) FROM "orders" s1 WHERE s1."customer_email" = t1."email") ordersCount
FROM "customers" t1
ORDER BY 2 DESC
