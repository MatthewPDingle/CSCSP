-- GeneticSearcher
UPDATE searchfitness SET bearfitness = 100, bullfitness = 100;
UPDATE searchfitness SET bearfitness = 500, bullfitness = 500 WHERE type = 'stop';
TRUNCATE TABLE searchresults

SELECT * FROM searchfitness ORDER BY type, metric

SELECT * FROM searchresults WHERE rundate >= '2013-03-14 13:00:00' ORDER BY bullscore DESC, bearscore

SELECT AVG(bearscore) FROM searchresults WHERE sellop = '<=' AND bearscore < 0 

-- End GeneticSearcher
-- Trading
SELECT *, shares * entryprice AS value FROM trades ORDER BY id 
SELECT * FROM trades WHERE status = 'open' ORDER BY id
SELECT round(CAST(netprofit / (shares * entryprice) * 100 AS numeric), 2) AS perchange, * FROM trades WHERE status = 'closed' ORDER BY exit

UPDATE trades SET entryprice = 10.78 WHERE id = 247

SELECT * FROM tradingaccount

SELECT * FROM tradingaccounthistory

SELECT symbol, COUNT(*) c FROM trades GROUP BY symbol ORDER BY c DESC

-- Open position performance
SELECT t.symbol, b.date, t.exit,
CASE WHEN t.type = 'long' THEN round(CAST(t.shares * (b.adjclose - t.entryprice) AS numeric), 2) 
ELSE round(CAST(t.shares * (t.entryprice - b.adjclose) AS numeric), 2) END AS change,
CASE WHEN t.type = 'long' THEN round(CAST(t.shares * (b.adjclose - t.entryprice) / (t.shares * t.entryprice) * 100 AS numeric), 2)
ELSE round(CAST(t.shares * (t.entryprice - b.adjclose) / (t.shares * t.entryprice) * 100 AS numeric), 2) END AS perchange,
CASE WHEN t.type = 'long' THEN round(CAST(t.shares * (b.change) AS numeric), 2) 
ELSE round(CAST(t.shares * (-b.change) AS numeric), 2) END AS todaychange,
CASE WHEN t.type = 'long' THEN round(CAST(b.change / (b.adjclose - b.change) * 100 AS numeric), 2)
ELSE round(CAST(-b.change / (b.adjclose - b.change) * 100 AS numeric), 2) END AS todayperchange
FROM trades t
INNER JOIN basicr b
ON t.symbol = b.symbol AND b.date = '2013-04-16'-- date(now())
WHERE t.status = 'open' AND date(t.entry) < '2013-04-16'--date(now())
OR t.status = 'closed' AND date(t.exit) = '2013-04-16'--date(now())

-- Trading account value (long + short + cash)
SELECT (SELECT SUM(b.adjclose * t.shares)
FROM trades t
INNER JOIN basicr b ON t.symbol = b.symbol AND b.date = (SELECT MAX(date) FROM basicr WHERE symbol = t.symbol)
WHERE t.type = 'long' AND status = 'open') 
+ (SELECT SUM(b.adjclose * t.shares)
FROM trades t
INNER JOIN basicr b ON t.symbol = b.symbol AND b.date = (SELECT MAX(date) FROM basicr WHERE symbol = t.symbol)
WHERE t.type = 'short' AND status = 'open') 
+ (SELECT cash FROM tradingaccount) AS value

SELECT DISTINCT symbol FROM basicr WHERE date = date(now())

-- 986 6670 vs 8500
SELECT r.symbol, r.adjclose AS price, m1.value AS m1, m2.value AS m2 
FROM basicr r 
INNER JOIN metric_volumeboll50 m1 ON m1.symbol = r.symbol AND m1.date = r.date 
INNER JOIN metric_dvol25ema m2 ON m2.symbol = r.symbol AND m2.date = r.date 
INNER JOIN metric_priceboll20 sd ON r.symbol = sd.symbol AND r.date = sd.date 
INNER JOIN (SELECT symbol, MAX(date) date FROM basicr GROUP BY symbol) t ON t.symbol = r.symbol AND t.date = r.date
WHERE (r.symbol IN (SELECT DISTINCT symbol FROM indexlist WHERE index = 'NYSE' OR index = 'Nasdaq' OR index = 'DJIA' OR index = 'SP500' ) 
AND r.volume >= 2000000 / r.adjclose 
AND ABS(sd.value) <= 1 AND r.adjclose >= 3)
OR r.symbol IN (SELECT symbol FROM trades WHERE status = 'open')


-- End Trading
-- Volatility & Position Sizing
SELECT t.symbol, t.type, b.value AS beta, t.shares * t.entryprice AS costbasis,
t.shares * r.adjclose AS currentvalue,
CASE WHEN t.type = 'long' THEN (r.adjclose - t.entryprice) * t.shares
ELSE (t.entryprice - r.adjclose) * t.shares
END AS change
FROM trades t
INNER JOIN basicr r
ON r.symbol = t.symbol AND r.date = '2013-02-22'
INNER JOIN metric_beta100 b
ON b.symbol = r.symbol AND b.date = r.date

SELECT r1.date, 
r1.change / (r1.adjclose - r1.change) * 100 AS stock,
r2.change /(r2.adjclose - r2.change) * 100 AS spy
FROM basicr r1
INNER JOIN basicr r2 
ON r1.date = r2.date AND r2.symbol = 'SPY'
WHERE (r1.symbol = 'MR')
ORDER BY r1.date ASC

SELECT * FROM metric_beta100 WHERE date = '2013-02-21' 
AND symbol IN (SELECT symbol FROM indexlist WHERE index = 'NYSE')
ORDER BY value ASC

SELECT b.symbol, b.date, b.adjclose, p3.value AS stockv, p4.value AS spyv,
p3.value / p4.value AS volmult
FROM basicr b
INNER JOIN metric_mvol100 p3
ON b.date = p3.date AND b.symbol = p3.symbol
INNER JOIN metric_mvol100 p4
ON b.date = p4.date AND p4.symbol = 'SPY'
WHERE b.date = '2013-02-27'
ORDER BY b.date DESC
-- End Volatility & Position Sizing
