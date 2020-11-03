UPDATE "books"
SET "price" = "price" + 1
WHERE "isbn" IN (SELECT i from
  (
     SELECT AVG("rating"), b."isbn" i from "reviews" r
     INNER JOIN "books" b ON b."isbn" = r."isbn" having AVG("rating") > 7 group by b."isbn")
  )