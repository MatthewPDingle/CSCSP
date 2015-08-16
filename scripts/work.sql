SELECT COUNT(*) FROM bitcointick

SELECT * FROM bitcointick 
ORDER BY timestamp
LIMIT 1000

-- Distribution of hours from bitcointick
SELECT hour, COUNT(*)
FROM (
SELECT date_part('hour', timestamp) AS hour
FROM bitcointick
) t
GROUP BY hour
ORDER BY hour

-- Distribution of minute from bitcointick
SELECT minute, COUNT(*)
FROM (
SELECT date_part('minute', timestamp) AS minute
FROM bitcointick
) t
GROUP BY minute
ORDER BY minute

-- Select ticks that go into a bar
SELECT bt.*, t.* FROM bitcointick bt
LEFT OUTER JOIN (
	SELECT price AS previousclose FROM bitcointick
	WHERE timestamp < '2013-05-31 06:00:00'
	ORDER BY timestamp DESC
	LIMIT 1
) t
ON 1 = 1
WHERE timestamp >= '2013-05-31 06:00:00'
AND timestamp < '2013-05-31 07:00:00'

SELECT * FROM bitcoinbar ORDER BY start DESC

SELECT duration, COUNT(*) c FROM bitcoinbar
GROUP BY duration
ORDER BY c

-- Distribution of hours from bitcoinbar
SELECT hour, COUNT(*)
FROM (
SELECT date_part('hour', start) AS hour
FROM bitcoinbar
) t
GROUP BY hour
ORDER BY hour

-- Distribution of day from bitcoinbar
SELECT d, COUNT(*)
FROM (
SELECT date_part('day', start) AS d
FROM bitcoinbar
) t
GROUP BY d
ORDER BY d

SELECT * FROM bitcoinbar LIMIT 10

SELECT * FROM indexlist

SELECT COUNT(*) FROM bar

SELECT symbol, to_char(MAX(start::date) + INTEGER '-400', 'YYYY-MM-DD') AS baseDate FROM bar GROUP BY symbol

SELECT name, COUNT(*) c 
FROM metrics
GROUP BY name

SELECT * FROM metrics WHERE symbol = 'INTC' ORDER BY start 

SELECT *  FROM metrics 
WHERE symbol = 'bitfinexUSD' 
AND name = 'rsi50ema'
ORDER BY start

SELECT * FROM bar WHERE symbol = 'bitfinexUSD' ORDER BY start -- 79002


UPDATE searchfitness SET bullfitness = 100, bearfitness = 100

SELECT * FROM searchfitness ORDER BY type, metric

SELECT * FROM searchresults WHERE rundate >= '2015-07-23' ORDER BY bullscore DESC

SELECT name, MIN(value), MAX(value)
FROM metrics
GROUP BY name
ORDER BY name

SELECT COUNT(DISTINCT symbol) FROM bar WHERE symbol = 'bitfinexUSD' OR symbol = 'SPY'

SELECT * FROM bar WHERE symbol = 'okcoinBTCCNY' AND duration = 'BAR_15M' ORDER BY start DESC

DELETE FROM bar WHERE symbol = 'krakenBTCEUR'

SELECT symbol, MIN(start), MAX(start), COUNT(*) 
FROM bar
GROUP BY symbol ORDER BY symbol

SELECT symbol, COUNT(*)
FROM metrics
GROUP BY symbol ORDER BY symbol

SELECT * FROM bar b
INNER JOIN indexlist i
ON b.symbol = i.symbol
WHERE i.index = 'NYSE' OR i.index = 'Nasdaq' OR i.index = 'DJIA' OR i.index = 'SP500' OR i.index = 'ETF' OR i.index = 'Stock Index'

SELECT  * FROM bitcointick limit 1000

SELECT symbol, COUNT(*)
FROM bitcointick
GROUP BY symbol ORDER BY symbol

SELECT * FROM indexlist

SELECT b.symbol, b.duration, COUNT(b.*) AS barcount
FROM bar b
INNER JOIN indexlist i ON b.symbol = i.symbol
WHERE i.index IN ('Bitcoin', 'SP500')
GROUP BY b.symbol, b.duration
ORDER BY b.symbol, b.duration

SELECT b.*, m1.value AS m1v, m2.value AS m2v, m4.value AS m4v, 
(SELECT close FROM bar WHERE symbol = 'SPY' AND start <= b.start ORDER BY start DESC LIMIT 1) AS alphaclose 
FROM bar b 
LEFT OUTER JOIN metrics m1 ON b.symbol = m1.symbol and b.start = m1.start AND b.duration = m1.duration 
LEFT OUTER JOIN metrics m2 ON b.symbol = m2.symbol and b.start = m2.start AND b.duration = m2.duration 
LEFT OUTER JOIN metrics m3 ON b.symbol = m3.symbol and b.start = m3.start AND b.duration = m3.duration 
LEFT OUTER JOIN metrics m4 ON b.symbol = m4.symbol and b.start = m4.start AND b.duration = m4.duration 
WHERE b.symbol IN (SELECT DISTINCT symbol FROM indexlist WHERE index = 'Bitcoin' /*AND symbol IN ('bitfinexBTCUSD')*/ ) 
AND b.symbol IN ('btceBTCUSD') 
AND m1.name = 'volumeboll50' AND m2.name = 'williamsralpha20' AND m3.name = 'priceboll20' AND m4.name = 'rsi5' 
AND ABS(m3.value) <= 100.0 AND b.close >= 3.0 AND b.start >= '2015-01-01' AND b.start < '2015-07-29' AND b.start < '2015-07-28' ORDER BY b.start

SELECT * FROM metrics
WHERE symbol = 'btceBTCUSD'
AND start >= '2015-01-01'
AND start < '2015-01-02'
AND name = 'priceboll20'


SELECT * FROM bar
WHERE symbol = 'btceBTCUSD'

SELECT symbol, to_char(MAX(start::date) + INTEGER '-400', 'YYYY-MM-DD') AS baseDate FROM bar GROUP BY symbol

SELECT DISTINCT symbol, duration, 
(SELECT MIN(start) FROM (SELECT symbol, start FROM bar WHERE (symbol = 'okcoinBTCUSD' AND duration = 'BAR_15M') OR (symbol = 'okcoinBTCCNY' AND duration = 'BAR_15M')  ORDER BY start DESC LIMIT 300) t) AS baseDate 
FROM bar WHERE (symbol = 'okcoinBTCUSD' AND duration = 'BAR_15M') OR (symbol = 'okcoinBTCCNY' AND duration = 'BAR_15M') 

SELECT b.*,
(SELECT close FROM bar WHERE symbol = 'SPY' AND start <= b.start ORDER BY start DESC LIMIT 1) AS alphaclose, 
(SELECT change FROM bar WHERE symbol = 'SPY' AND start <= b.start ORDER BY start DESC LIMIT 1) AS alphachange 
FROM bar b 
WHERE (b.start >= (SELECT COALESCE((SELECT MAX(start) FROM metrics WHERE symbol = 'okcoinBTCUSD' AND duration = 'BAR_5M' AND  name = 'dv10ema'), '2010-01-01 00:00:00'))
OR b.partial = true)
AND b.symbol = 'okcoinBTCUSD' AND b.duration = 'BAR_5M' ORDER BY b.start

SELECT duration, COUNT(*) FROM bar GROUP BY duration

SELECT COALESCE((SELECT MAX(start) FROM metrics WHERE symbol = 'okcoinBTCUSD' AND duration = 'BAR_5M' AND  name = 'dv2'), '2010-01-01 00:00:00')

SELECT * FROM bar
WHERE symbol = 'okcoinBTCUSD' AND duration = 'BAR_5M'
ORDER BY start DESC

SELECT * FROM metrics
WHERE symbol = 'okcoinBTCUSD' AND duration = 'BAR_5M' AND name = 'rsi5' 
ORDER BY start DESC

SELECT m.*, b.close
FROM bar b
INNER JOIN metrics m ON b.start = m.start AND b.duration = m.duration AND b.symbol = m.symbol
WHERE b.symbol = 'okcoinBTCUSD' AND b.duration = 'BAR_5M' AND m.name = 'morningstar'
ORDER BY b.start DESC

SELECT name, COUNT(*) FROM metrics GROUP BY name ORDER BY name

UPDATE metrics set name = 'cdlmorningstar' WHERE name = 'morningstar'

SELECT name, MIN(value), MAX(value) FROM metrics GROUP BY name ORDER BY name

SELECT COUNT(*) FROM metrics

SELECT symbol, duration, COUNT(*) FROM bar GROUP BY symbol, duration ORDER BY symbol, duration

SELECT * FROM bar WHERE symbol = 'okcoinBTCCNY' AND duration = 'BAR_15M' ORDER BY start DESC

SELECT * FROM searchfitness ORDER BY type, metric

SELECT COUNT(*) FROM metrics WHERE name = 'macd12_26_9'

-- 1% extremes
SELECT * FROM (SELECT value FROM metrics WHERE name = 'macd12_26_9' ORDER BY value DESC LIMIT (SELECT COUNT(*) / 100 FROM metrics WHERE name = 'macd12_26_9')) t ORDER BY value ASC LIMIT 1
SELECT * FROM (SELECT value FROM metrics WHERE name = 'macd12_26_9' ORDER BY value ASC LIMIT (SELECT COUNT(*) / 100 FROM metrics WHERE name = 'macd12_26_9')) t ORDER BY value DESC LIMIT 1

-- 5% extremes
SELECT * FROM (SELECT value FROM metrics WHERE name = 'macd12_26_9' ORDER BY value DESC LIMIT (SELECT COUNT(*) / 20 FROM metrics WHERE name = 'macd12_26_9')) t ORDER BY value ASC LIMIT 1
SELECT * FROM (SELECT value FROM metrics WHERE name = 'macd12_26_9' ORDER BY value ASC LIMIT (SELECT COUNT(*) / 20 FROM metrics WHERE name = 'macd12_26_9')) t ORDER BY value DESC LIMIT 1

-- 10% extremes
SELECT * FROM (SELECT value FROM metrics WHERE name = 'macd12_26_9' ORDER BY value DESC LIMIT (SELECT COUNT(*) / 10 FROM metrics WHERE name = 'macd12_26_9')) t ORDER BY value ASC LIMIT 1
SELECT * FROM (SELECT value FROM metrics WHERE name = 'macd12_26_9' ORDER BY value ASC LIMIT (SELECT COUNT(*) / 10 FROM metrics WHERE name = 'macd12_26_9')) t ORDER BY value DESC LIMIT 1

-- 20% extremes
SELECT * FROM (SELECT value FROM metrics WHERE name = 'macd12_26_9' ORDER BY value DESC LIMIT (SELECT COUNT(*) / 5 FROM metrics WHERE name = 'macd12_26_9')) t ORDER BY value ASC LIMIT 1
SELECT * FROM (SELECT value FROM metrics WHERE name = 'macd12_26_9' ORDER BY value ASC LIMIT (SELECT COUNT(*) / 5 FROM metrics WHERE name = 'macd12_26_9')) t ORDER BY value DESC LIMIT 1

-- 33% extremes
SELECT * FROM (SELECT value FROM metrics WHERE name = 'macd12_26_9' ORDER BY value DESC LIMIT (SELECT COUNT(*) / 3 FROM metrics WHERE name = 'macd12_26_9')) t ORDER BY value ASC LIMIT 1
SELECT * FROM (SELECT value FROM metrics WHERE name = 'macd12_26_9' ORDER BY value ASC LIMIT (SELECT COUNT(*) / 3 FROM metrics WHERE name = 'macd12_26_9')) t ORDER BY value DESC LIMIT 1

-- 50% extremes
SELECT * FROM (SELECT value FROM metrics WHERE name = 'macd12_26_9' ORDER BY value DESC LIMIT (SELECT COUNT(*) / 2 FROM metrics WHERE name = 'macd12_26_9')) t ORDER BY value ASC LIMIT 1
SELECT * FROM (SELECT value FROM metrics WHERE name = 'macd12_26_9' ORDER BY value ASC LIMIT (SELECT COUNT(*) / 2 FROM metrics WHERE name = 'macd12_26_9')) t ORDER BY value DESC LIMIT 1