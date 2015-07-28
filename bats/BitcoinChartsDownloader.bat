REM Run maybe once per day
REM This will download the archive files from bitcoincharts, insert the tick data, process the ticks into bars, and insert the bar data.
REM The DB is checked for the latest date with tick data before importing any new tick data.  Only new tick data will be imported.
REM It will only overwrite bar data already in the DB if the bar is marked as partial.
java -jar BitcoinChartsDownloader.jar bitfinexUSD.csv.gz BAR_15M bitstampUSD.csv.gz BAR_15M btceUSD.csv.gz BAR_15M krakenEUR.csv.gz BAR_15M krakenUSD.csv.gz BAR_15M okcoinCNY.csv.gz BAR_15M