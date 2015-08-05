package data.downloaders.bitcoincharts;

import java.io.File;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;

import constants.Constants.BAR_SIZE;
import data.Converter;

public class BitcoinChartsDownloader {

	/**
	 * args must come in pairs.  
	 * First is the filename you want to download from BitcoinCharts
	 * Second is the bar size i.e. BAR_15M
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		for (int a = 0; a < args.length; a += 2) {
			String filename = args[a];
			String barSize = args[a + 1];
			String tickname = BitcoinChartsConstants.FILENAME_TICKNAME_HASH.get(filename);

			if (tickname != null) {
				if (BAR_SIZE.valueOf(barSize) != null) {
					System.out.println("Downloading: " + filename);
					downloadArchive(filename);
					System.out.println("Inserting ticks from file into DB");
					Converter.processArchiveFileIntoTicks(filename);
					System.out.println("Converting ticks into bars and inserting into DB");
					Converter.processTickDataIntoBars(tickname, BAR_SIZE.valueOf(barSize));
					System.out.println("Finished: " + filename);
				}
				else {
					System.out.println("Bad BAR_SIZE: " + barSize);
				}
			}
			else {
				System.out.println("No support for " + filename);
			}
		}
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