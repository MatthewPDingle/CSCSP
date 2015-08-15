package workers;

import java.util.ArrayList;
import java.util.Calendar;

import dbio.QueryManager;

public class UpdateStocksInBar {

	private static int NUM_THREADS = 16;
	
	public static void main(String[] args) {
		System.out.println("Starting at " + Calendar.getInstance().getTime().toString());
		
		// Delete stocks in bar table
		QueryManager.deleteStocksFromBar();
		
		// Get symbol list from the index table
		ArrayList<String> symbols = QueryManager.getUniqueListOfSymbols();
		symbols.clear();
		symbols.add("SPY");
		
		// Make a number of threads for parallel processing
		ArrayList<ArrayList<String>> symbolBlockList = new ArrayList<ArrayList<String>>();
		ArrayList<UpdateBasicThreadHelper> threadList = new ArrayList<UpdateBasicThreadHelper>();
		for (int a = 0; a < NUM_THREADS; a++) {
			ArrayList<String> symbolBlock = new ArrayList<String>();
			symbolBlockList.add(symbolBlock);
		}
		for (int a = 0; a < symbols.size(); a++) {
			symbolBlockList.get(a % NUM_THREADS).add(symbols.get(a)); 
		}
		for (ArrayList<String> symbolBlock:symbolBlockList) {
			UpdateBasicThreadHelper t = new UpdateBasicThreadHelper(symbolBlock);
			threadList.add(t);
			System.out.println("Staring thread with " + symbolBlock.size() + " symbols.");
			t.start();
		}
		for (UpdateBasicThreadHelper t:threadList) {
			try {
				t.join();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		// Cleanup any junk that Yahoo gave on holidays
		QueryManager.deleteHolidaysForStocksFromBar();
	}
}