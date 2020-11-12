UPDATE "books" SET("quantity_sold", "quantity_in_stock") = (SELECT "quantity_sold" + COUNT(*), "quantity_in_stock" - COUNT(*) FROM "orders" O
                                                            WHERE O."status"='ORDER_PLACED'
                                                                AND O."customer_email" IN
                                                                (
                                                                    SELECT "email" FROM "customers" C
                                                                    WHERE
                                                                        (SELECT DISTINCT COUNT("isbn")
                                                                         FROM "order_items" OI JOIN "orders" O ON OI."order_id"=O."order_id"
                                                                         WHERE O."customer_email"= C."email") > 30
                                                                            AND C."email" NOT IN (SELECT "customer_email"
                                                                                                    FROM "orders" O
                                                                                                    JOIN "order_items" OI
                                                                                                    ON O."order_id"=OI."order_id"
                                                                                                    WHERE OI."isbn"='637638465-X')))
WHERE "isbn" = '637638465-X'
