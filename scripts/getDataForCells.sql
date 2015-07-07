-- This query gets all the data needed for every map cell
SELECT b.*, m1.value AS m1Value, m2.value AS m2Value 
FROM bar b
LEFT OUTER JOIN metrics m1 ON b.symbol = m1.symbol AND b.start = m1.start AND b.duration = m1.duration
LEFT OUTER JOIN metrics m2 ON b.symbol = m2.symbol AND b.start = m2.start AND b.duration = m2.duration
LEFT OUTER JOIN metrics m3 ON b.symbol = m3.symbol AND b.start = m3.start AND b.duration = m3.duration
WHERE b.symbol IN (SELECT DISTINCT symbol FROM indexlist WHERE index = 'Index' ) -- GUI index filter
AND m1.name = 'mfi14' 
AND m2.name = 'cci20'
AND m3.name = 'priceboll20' -- GUI volatility filter
AND ABS(m3.value) <= 10
AND b.close >= 3.0 -- GUI price filter
AND b.start >= '2015-01-26' AND b.start < '2015-07-05' AND b.start < '2015-07-03' -- GUI date filter