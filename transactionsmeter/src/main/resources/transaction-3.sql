SELECT t2."city", ROUND(AVG(t4."rating"),2)
FROM "orders" t1
JOIN "addresses" t2 ON t1."invoice_address_id" = t2."address_id"
JOIN "order_items" t3 ON t1."order_id" = t3."order_id"
JOIN "reviews" t4 ON t3."isbn" = t4."isbn"
JOIN "books" t6 ON t3."isbn" = t6."isbn"
WHERE  (SELECT COUNT(*) FROM "addresses" s1 WHERE s1."city" = t2."city") > 3
GROUP BY t2."city"