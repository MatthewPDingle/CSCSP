package web;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import constants.Constants;
import dbio.QueryManager;

public class SymbolListUpdater {

	public static void main(String[] args) {
		QueryManager.truncateIndexList();
		
		QueryManager.updateIndexList(nyseUpdate(Constants.NYSE_SYMBOL_URL), "NYSE"); // Yahoo NYSE page no longer lists components
		QueryManager.updateIndexList(yahooUpdate(Constants.YAHOO_NASDAQ_SYMBOL_URL), "Nasdaq");
		QueryManager.updateIndexList(yahooUpdate(Constants.YAHOO_DJIA_SYMBOL_URL), "DJIA");
		QueryManager.updateIndexList(okfnUpdate(Constants.OKFN_SP500_SYMBOL_URL), "SP500"); // Wiki page changed a bit and broke
		QueryManager.updateIndexList(yahooETFUpdate(), "ETF"); // Yahoo ETF page was broken at last check
		QueryManager.updateIndexList(indexUpdate(), "Index");
		System.exit(0);
	}

	public static ArrayList<String> yahooUpdate(String url) {
		ArrayList<String> symbols = new ArrayList<String>();
		try {
			int page = 0;
			while (true) {
				URL yahooURL = new URL(url + page);
				URLConnection conn = yahooURL.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

				// Parse html
				StringBuilder sb = new StringBuilder();
				String inputLine;
			    while ((inputLine = in.readLine()) != null) {
			    	sb.append(inputLine);
			    }
			    String wholePage = sb.toString();
			    wholePage = wholePage.substring(wholePage.indexOf("yfnc_tablehead1"));
			    
			    // Check to see if we've past the last page
			    if (!wholePage.contains("/q?s=") || page > 100)
			    	break;
			    
			    String[] pieces = wholePage.split("/q\\?s=");
			    for (int a = 1; a < pieces.length; a++) {
			    	try {
			    		String symbol = pieces[a].substring(0, pieces[a].indexOf("\""));
			    		if (symbol.length() >= 1 && symbol.length() <= 6) {// Extra safe, probably not needed
			    			symbols.add(symbol);
			    			System.out.println("Adding " + symbol);
			    		}
			    	}
			    	catch (Exception e) {} // Try next symbol
			    }
			    
			    Thread.sleep(0); // Don't look too suspicious...
			    page++; // Go to next page
			}

		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return symbols;
	}
	
	public static ArrayList<String> nyseUpdate(String url) {
		ArrayList<String> symbols = new ArrayList<String>();
		try {
			URL nyseURL = new URL(url);
			URLConnection conn = nyseURL.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

			// Parse csv
			String inputLine;
			int lineNumber = 1;
		    while ((inputLine = in.readLine()) != null) {
		    	if (lineNumber >= 3) {
			    	String[] lineParts = inputLine.split(",");
			    	try {
			    		String symbol = lineParts[1];
			    		symbol = symbol.replaceAll("\"", "");
			    		symbols.add(symbol);
			    	}
			    	catch (Exception e) {}
		    	}
		    	lineNumber++;
		    }
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return symbols;
	}
	
	public static ArrayList<String> yahooETFUpdate() {
		ArrayList<String> symbols = new ArrayList<String>();
		try {
			// Try going through up to 10 pages of 100 ETFs each.
			for (int page = 1; page <= 10; page++) {
			
				URL realtorURL = new URL(Constants.YAHOO_ETF_SYMBOL_URL + "&page=" + page);
				URLConnection conn = realtorURL.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	
				// Parse html
				StringBuilder sb = new StringBuilder();
				String inputLine;
			    while ((inputLine = in.readLine()) != null) {
			    	sb.append(inputLine);
			    }
			    String wholePage = sb.toString();
			    wholePage = wholePage.replaceAll("\\\\", "---");

			    String[] pieces = wholePage.split("---/q\\?s=");
			    for (int a = 1; a < pieces.length; a++) {
			    	try {
			    		String piece = pieces[a].replace("---/q?s=", "");
			    		String symbol = piece.substring(0, piece.indexOf("---"));
			    		if (symbol.length() >= 1 && symbol.length() <= 6) // Extra safe, probably not needed
			    			if (!symbols.contains(symbol)) {
			    				symbols.add(symbol);
			    				System.out.println("Adding " + symbol);
			    			}
			    	}
			    	catch (Exception e) {} // Try next symbol
			    }
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return symbols;
	}
	
	public static ArrayList<String> wikiETFUpdate() {
		ArrayList<String> symbols = new ArrayList<String>();
		try {
			URL realtorURL = new URL(Constants.WIKI_ETF_SYMBOL_URL);
			URLConnection conn = realtorURL.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			
			// Parse html
			StringBuilder sb = new StringBuilder();
			String inputLine;
		    while ((inputLine = in.readLine()) != null) {
		    	sb.append(inputLine);
		    }
		    String wholePage = sb.toString();

		    String[] pieces = wholePage.split("ticker=");
		    for (int a = 1; a < pieces.length; a++) {
		    	try {
		    		String symbol = pieces[a].substring(0, pieces[a].indexOf("\"")).toUpperCase();
		    		if (symbol.length() >= 1 && symbol.length() <= 6) // Extra safe, probably not needed
		    			if (!symbols.contains(symbol)) {
		    				symbols.add(symbol);
		    				System.out.println("Adding " + symbol);
		    			}
		    	}
		    	catch (Exception e) {} // Try next symbol
		    }
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return symbols;
	}
	
	public static ArrayList<String> wikiUpdate(String url) {
		ArrayList<String> symbols = new ArrayList<String>();
		try {
			URL realtorURL = new URL(url);
			URLConnection conn = realtorURL.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

			// Parse html
			StringBuilder sb = new StringBuilder();
			String inputLine;
		    while ((inputLine = in.readLine()) != null) {
		    	sb.append(inputLine);
		    }
		    String wholePage = sb.toString();
		    // Narrow it down to the html between "Ticker symbol" and "Wikipedia:Citing_sources"
		    wholePage = wholePage.substring(wholePage.indexOf("Ticker symbol"));
		    wholePage = wholePage.substring(0, wholePage.indexOf("Wikipedia:Citing_sources"));
		    
		    String[] pieces = wholePage.split("external text");
		    for (int a = 1; a < pieces.length; a++) {
		    	try {
		    		String workPiece = pieces[a].substring(pieces[a].indexOf(">") + 1);
		    		String symbol = workPiece.substring(0, workPiece.indexOf("<"));
		    		if (symbol.equals("reports"))
		    			symbol = "";
		    		if (symbol.length() >= 1 && symbol.length() <= 6) {// Extra safe, probably not needed
		    			symbols.add(symbol);
		    			System.out.println("Adding " + symbol);
		    		}
		    	}
		    	catch (Exception e) {} // Try next symbol
		    }
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return symbols;
	}
	
	public static ArrayList<String> okfnUpdate(String url) {
		ArrayList<String> symbols = new ArrayList<String>();
		try {
			URL okfnurl = new URL(url);
			URLConnection conn = okfnurl.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

			// Parse csv
			String inputLine;
			int lineNumber = 1;
		    while ((inputLine = in.readLine()) != null) {
		    	if (lineNumber >= 2) {
			    	String[] lineParts = inputLine.split(",");
			    	try {
			    		String symbol = lineParts[0];
			    		symbol = symbol.replaceAll("\"", "");
			    		symbols.add(symbol);
			    	}
			    	catch (Exception e) {}
		    	}
		    	lineNumber++;
		    }
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return symbols;
	}
	
	public static ArrayList<String> indexUpdate() {
		ArrayList<String> symbols = new ArrayList<String>();
		
		symbols.add("^GSPC");
		symbols.add("^OEX");
		symbols.add("^MID");
		symbols.add("^SML");
		symbols.add("^IXIC");
		symbols.add("^IXBK");
		symbols.add("^NBI");
		symbols.add("^IXK");
		symbols.add("^IXF");
		symbols.add("^IXID");
		symbols.add("^IXIS");
		symbols.add("^IXFN");
		symbols.add("^IXUT");
		symbols.add("^IXTR");
		symbols.add("^NDX");
		symbols.add("^DJI");
		symbols.add("^DJA");
		symbols.add("^DJT");
		symbols.add("^DJU");
		symbols.add("^FVX");
		symbols.add("^TNX");
		symbols.add("^TYX");
		symbols.add("^RUI");
		symbols.add("^RUT");
		symbols.add("^RUA");
		symbols.add("^NIN");
		symbols.add("^NUS");
		symbols.add("^HSI");
		symbols.add("^N225");
		symbols.add("^GDAXI");
		symbols.add("^FTSE");
		
		return symbols;
	}
}