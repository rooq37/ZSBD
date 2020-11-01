SELECT t1."author_id", COUNT(t2."isbn")
FROM "authors" t1
JOIN "books" t2 ON t1."author_id" = t2."author_id"
 WHERE  (SELECT COUNT(*) FROM "books" s1 WHERE s1."author_id" = t1."author_id") > 3
GROUP BY t1."author_id"
ORDER BY 2 DESC