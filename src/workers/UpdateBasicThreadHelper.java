package workers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Stack;

import utils.ConnectionSingleton;
import constants.Constants;
import constants.Constants.BAR_SIZE;

public class UpdateBasicThreadHelper extends Thread {

	private ArrayList<String> symbolList = new ArrayList<String>();

	/**
	 * Constructor 
	 * 
	 * @param symbolList
	 * @param production
	 */
	public UpdateBasicThreadHelper(ArrayList<String> symbolList) {
		super();
		this.symbolList = symbolList;
	}
	
	public void run() {
		// Try all symbols
		ArrayList<String> failedSymbols = process(this.symbolList);
		// Try the failures one last time
		process(failedSymbols);
	}
	
	public ArrayList<String> process(ArrayList<String> symbols) {
		ArrayList<String> failedSymbols = new ArrayList<String>();
		try {
			int symbolCounter = 1;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			for (String symbol:symbols) {
				System.out.println("Now processing " + symbolCounter + " : " + symbol + " at " + Calendar.getInstance().getTime().toString());
				int ms = (int)(Math.random() * 500);
				Thread.sleep(ms);
				symbolCounter++;
				
				// Find the most recent date that we already have info for this symbol
				String mostRecentYear = "2012";
				String mostRecentMonth = "00"; // 0-Based Month
				String mostRecentDay = "01";
//				if (production) {
//					mostRecentYear = new Integer(Calendar.getInstance().get(Calendar.YEAR) - 1).toString(); 
//					mostRecentMonth = new Integer(Calendar.getInstance().get(Calendar.MONTH)).toString();
//					mostRecentDay = new Integer(Calendar.getInstance().get(Calendar.DATE)).toString();
//				}

				// Connect to Yahoo Finance
				// http://ichart.finance.yahoo.com/table.csv?s=F&a=02&b=30&c=2010&g=d (a = 0-based month, b = date, c = year)
				String url = "";
				if (mostRecentYear == null || mostRecentYear.equals(""))
					url = "http://ichart.finance.yahoo.com/table.csv?s=" + symbol;
				else 
					url = "http://ichart.finance.yahoo.com/table.csv?s=" + symbol + "&a=" + mostRecentMonth + "&b=" + mostRecentDay + "&c=" + mostRecentYear + "&g=d";
				
				Stack<String> inLines = new Stack<String>();
				try {
					URL yahoo = new URL(url);
				    URLConnection yahooConnection = yahoo.openConnection();
				    BufferedReader in = new BufferedReader(new InputStreamReader(yahooConnection.getInputStream()));
					
				    // Reverse it so moving averages go in the right direction
				    String inputLine;
				    
				    int lineCounter = 1; // Idiots at yahoo have a bug in their lists - they show the most recent day twice.
				    while ((inputLine = in.readLine()) != null) {
				    	if (lineCounter > 1) {
				    		if (!inLines.contains(inputLine))
				    			inLines.push(inputLine);
				    	}
				    	lineCounter++;
				    }
				    in.close();
				}
				catch (Exception e) {
					e.printStackTrace();
					failedSymbols.add(symbol);
				}
			    
			    // Initialize variables
			    int counter = 0;
			    ArrayList<String> records = new ArrayList<String>();

			    int size = inLines.size();
			    float yesterdayAdjClose = 0;
				for (int a = 0; a < size; a++) {
					String line = inLines.pop();
				  	String[] lineValues = line.split(",");
				  	String date = lineValues[0];
				  	Calendar cStart = Calendar.getInstance();
				  	cStart.set(Calendar.MILLISECOND, 0);
				  	cStart.set(Calendar.SECOND, 0);
				  	cStart.set(Calendar.MINUTE, 0);
				  	cStart.set(Calendar.HOUR_OF_DAY, 0);
				  	cStart.setTime(sdf.parse(date));
				  	Calendar cEnd = Calendar.getInstance();
				  	cEnd.setTime(cStart.getTime());
				  	cEnd.add(Calendar.DATE, 1);
				  	String start = sdf.format(cStart.getTime());
				  	String end = sdf.format(cEnd.getTime());
				  	float open = new Float(lineValues[1]);
				  	float high = new Float(lineValues[2]);
				  	float low = new Float(lineValues[3]);
				  	float close = new Float(lineValues[4]);
				  	long volume = new Long(lineValues[5]);
				  	float adjClose = new Float(lineValues[6]);
				  	
				  	float splitMultiplier = adjClose / close;
				  	float adjOpen = open * splitMultiplier;
				  	float adjHigh = high * splitMultiplier;
				  	float adjLow = low * splitMultiplier;
				  	float change = adjClose - yesterdayAdjClose;
				  	float gap = adjOpen - yesterdayAdjClose;
				  	float vwap = (adjOpen + adjClose + adjHigh + adjLow) / 4f; // Gross approximation without having tick data
				  	Integer numTrades = null;
				  
				  	change = (float)(Math.round(change*100.0f)/100.0f);
				  	gap = (float)(Math.round(gap*100.0f)/100.0f);
				  	
				  	StringBuilder sb = new StringBuilder();
				  	sb.append("('");
				  	sb.append(symbol);
				  	sb.append("', ");
				  	sb.append(adjOpen);
				  	sb.append(", ");
				  	sb.append(adjClose);
				  	sb.append(", ");
				  	sb.append(adjHigh);
				  	sb.append(", ");
				  	sb.append(adjLow);
				  	sb.append(", ");
				  	sb.append(vwap);
				  	sb.append(", ");
				  	sb.append(volume);
				  	sb.append(", ");
				  	sb.append(numTrades);
				  	sb.append(", ");
				  	sb.append(change);
				  	sb.append(", ");
				  	sb.append(gap);
				  	sb.append(", '");
				  	sb.append(start);
				  	sb.append("', '");
				  	sb.append(end);
				  	sb.append("', '");
				  	sb.append(BAR_SIZE.BAR_1D.toString());
				  	sb.append("', ");
				  	sb.append("false");
				  	sb.append(")");
				  	
				  	if (a > 0) { // Don't add the first one because the change and gap will be wrong
				  		records.add(sb.toString());
				  	}
				  	
				  	if (counter % 2000 == 1999) {
				  		insertRecords(records);
				  		records.clear();
				  	}
				  	
				  	System.out.println(sb.toString());
				  	
				  	yesterdayAdjClose = adjClose;
				  	
				  	counter++;
				}
				if (records.size() > 0) {
					insertRecords(records);
					records.clear();
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return failedSymbols;
	}
	
	private void insertRecords(ArrayList<String> records) {
		String insertQuery = "";
		try {	
			Connection c = ConnectionSingleton.getInstance().getConnection();
			
			insertQuery = "INSERT INTO " + Constants.BAR_TABLE + " (symbol, open, close, high, low, vwap, volume, numtrades, change, gap, start, \"end\", duration, partial) VALUES ";
			StringBuilder sb = new StringBuilder();
			for (String record:records) {
				sb.append(record);
				sb.append(", ");
			}
			String valuesPart = sb.toString();
			valuesPart = valuesPart.substring(0, valuesPart.length() - 2);
			insertQuery = insertQuery + valuesPart;
			
			Statement s = c.createStatement();
			s.executeUpdate(insertQuery);
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.err.println(insertQuery);
		}
	}
}