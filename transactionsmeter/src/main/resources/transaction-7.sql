UPDATE "books" b1
SET "price" = "price" + 1
WHERE (SELECT i from
    (SELECT AVG("rating"), b2."isbn" i from "reviews" r
     INNER JOIN "books" b2 ON b2."isbn" = r."isbn" where  b2."isbn" = b1."isbn" having AVG("rating") > 9 group by b2."isbn")
    ) =  b1."isbn"