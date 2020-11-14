SELECT t1."email", (SELECT COUNT(*) FROM "orders" s1 WHERE s1."customer_email" = t1."email") ordersCount, EXTRACT( YEAR from t1."registration_date")
FROM "customers" t1
WHERE EXTRACT( YEAR from t1."registration_date") > 2011
ORDER BY 3 DESC