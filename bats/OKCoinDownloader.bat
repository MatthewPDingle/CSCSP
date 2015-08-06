REM Run whenever you need map tracking
REM This will download bar data from okcoin and insert the bar data.
REM It will only overwrite bar data already in the DB if the bar is marked as partial.
java -jar OKCoinDownloader.jar btc_usd BAR_15M 4 btc_cny BAR_5M 12 btc_cny BAR_15M 4 btc_cny BAR_30M 2 btc_cny BAR_1H 2 ltc_usd BAR_15M 4 ltc_cny BAR_15M 4