REM Run whenever you need map tracking
REM This will download bar data from okcoin and insert the bar data.
REM It will only overwrite bar data already in the DB if the bar is marked as partial.
java -jar OKCoinDownloader.jar btc_cny BAR_15M 4