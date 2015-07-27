package data.downloaders.bitcoincharts;

import java.io.File;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;

import constants.Constants.BAR_SIZE;
import data.Converter;

public class BitcoinChartsDownloader {

	public static void main(String[] args) {

//		downloadArchive(BitcoinChartsConstants.FILE_TICK_HISTORY_KRAKEN_BTC_EUR);
//		Converter.processArchiveFileIntoTicks(BitcoinChartsConstants.FILE_TICK_HISTORY_KRAKEN_BTC_EUR);
		Converter.processTickDataIntoBars("krakenBTCEUR", BAR_SIZE.BAR_15M);
	}

	public static void downloadArchive(String fileName) {
		try {
			FileUtils.copyURLToFile(new URL(BitcoinChartsConstants.URL + fileName), new File("data/" + fileName));
			
			LogManager.getLogger("data.downloader").info("Downloaded file " + fileName + " from BitcoinCharts");
		}
		catch (Exception e) {
			e.printStackTrace();
			LogManager.getLogger("data.downloader").error("Error downloading file " + fileName + " from BitcoinCharts");
			LogManager.getLogger("data.downloader").error(e.getStackTrace().toString());
		}
	}	
}