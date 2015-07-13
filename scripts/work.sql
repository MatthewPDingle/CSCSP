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


SELECT * FROM searchfitness SET bullfitness = 100, bearfitness = 100

SELECT * FROM searchresults WHERE rundate = '2015-07-10' ORDER BY bullscore DESC


