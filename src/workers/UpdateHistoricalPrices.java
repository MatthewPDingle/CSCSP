package workers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Stack;

import constants.Constants;


public class UpdateHistoricalPrices extends Thread {
	
	private ArrayList<String> symbols = new ArrayList<String>();
	
	private String sp500Table = "";
	private String sp500StatsTable = "";
	
	public UpdateHistoricalPrices(ArrayList<String> symbols, String sp500Table, String sp500StatsTable) {
		this.symbols = symbols;
		this.sp500Table = sp500Table;
		this.sp500StatsTable = sp500StatsTable;
	}
	
	public UpdateHistoricalPrices(String sp500Table, String sp500StatsTable) {
		this.sp500Table = sp500Table;
		this.sp500StatsTable = sp500StatsTable;
	}
	
	public ArrayList<String> getSymbols() {
		return symbols;
	}

	public void setSymbols(ArrayList<String> symbols) {
		this.symbols = symbols;
	}
	
	public static void main(String[] args) {
		String closeString = "\"N/A - <b>14.36</b>\"";
		
		closeString = closeString.substring(closeString.indexOf(">") + 1);
	  	closeString = closeString.substring(0,closeString.indexOf("<"));
	  	
	  	System.out.println(closeString);
	}
	
	/**
	 * Real time version for intra-day updates.
	 */
	public static void inRealTime() {
		try {
			System.out.println("Starting at " + Calendar.getInstance().getTime().toString());
			
			// First delete all any "partial" intra-day records in sp500 and sp500statistics
			try {
				Class.forName("org.postgresql.Driver").newInstance();
				
				Connection conn = DriverManager.getConnection(Constants.URL, Constants.USERNAME, Constants.PASSWORD);
				if (!conn.isClosed()) {
					String dQuery = "DELETE FROM sp500statsp " +
									"WHERE (sp500statsp.date, sp500statsp.symbol) IN " +
									"( " + 
									"	SELECT sp500statsp.date, sp500statsp.symbol " +
									"	FROM sp500statsp " +
									"	INNER JOIN sp500p " +
									"	ON sp500p.date = sp500statsp.date AND sp500p.symbol = sp500statsp.symbol " +
									"	WHERE sp500p.partial = true " +
									")";
					Statement dStatement = conn.createStatement();
					dStatement.executeUpdate(dQuery);
													
					String deleteQuery = "DELETE FROM sp500p WHERE partial = true";
					Statement deleteStatement = conn.createStatement();
					deleteStatement.executeUpdate(deleteQuery);
				}
				conn.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			
			// Connect to Yahoo Finance
			// http://download.finance.yahoo.com/d/quotes.csv?s= (symbols separated by +) &f=sk1d1t1c1ohgv&e=.csv
			String url[] = new String[10];
			url[0] = "http://download.finance.yahoo.com/d/quotes.csv?s=URI+VLY+WAT+MLHR+FRX+AMAG+DPTR+HNT+CQB+SSD+PVA+JNPR+SAN+AIZ+SUG+GAME+LINC+MF+CAVM+MNKD+MNRO+SONC+HNZ+TRH+AMG+SPWRA+HAL+ANDE+SGY+CPA+EQIX+DCI+AFG+CPN+GOL+DB+THOR+CERN+FTR+ISRG+VMED+TOL+RHB+CHSI+TEG+CVE+IT+M+SIVB+SPN+PETD+BBT+SHLD+MED+TV+TSL+WTW+CPHD+LLL+CXO+DE+UNS+GR+VVC+NMR+SNY+PX+MTX+CHT+ERF+PCAR+PBI+CBI+MRH+CTRP+PUK+SVR+GS+DSW+TSTC+VOD+ORCL+EXBD+IBKC+APWR+TDC+MSM+UTIW+SYMC+BPO+FAF+PDS+EXXI+RL+WGOV+DNB+URBN+UPL+JOSB+XRTX+DPL+ADI+RSG+AMX+KFN+CPB+AZN+DRI+ITW+CMP+JCG+RAX+SHAW+ADCT+FIS+AVA+CYOU+MAT+CBL+DISH+WERN+PSE+MGA+ICO+KR+NLY+FTI+SAI+ARRS+FNSR+IFF+ESS+CI+ONB+GIS+BK+DF+ROP+MHS+ELNK+HOT+SOLR+NVS+ENS+LZ+ATML+EXEL+FMC+CATY+MMS+LKQX+UTHR+VNO+CHL+HAS+HOG+PLXS+STLD+MANT+RIGL+SSS+TGI+MPEL+MR+TRW+GOV+WLP+PSEC+NDAQ+TNS+DCT+ABFS+UMBF+VRSK+USTR+CBT+SON+IRE+CACI+HOV+PSSI+EBAY+SBUX+CCC+CPTS+REXX+CS+MU+CBE+AAP+BHP+KSU+PFWD+IDTI+DO&f=sk1d1t1c1ohgvc1&e=.csv";
			url[1] = "http://download.finance.yahoo.com/d/quotes.csv?s=RA+FDO+IVZ+MWV+NIHD+ISIS+XNPT+LPNT+NDSN+TK+BID+CVA+RSH+COL+MRK+BMO+EW+NKE++AGP+LFC+MGLN+KO+KLIC+CLW+SXL+ATHR+MDU+MW+HXM+BEBE+AVB+IVN+ART+BKC+SLGN+AEM+FORM+HBAN+APL+ARO+BX+OII+STEC+HD+ANH+FRPT+KEP+WX+AGU+INT+RST+WBD+LEG+GHL+POOL+DIS+NOV+CCI+UDR+PCLN+ELS+SHG+NOVL+HNP+CLWR+RIMM+CAM+QCOM+AHL+MTZ+UNM+BG+ODP+BTE+YSI+O+ROK+BIDU+NUE+EGN+ADBE+TDS+MYL+CLC+BCO+NJR+KEY+GCI+HS+SWN+LPL+AUY+SOHU+CEL+IM+HIBB+WDC+BRC+KOF+RRI+TXN+BPL+CBRL+HITT+SKX+QNST+COCO+ANSS+FLO+BWP+MTG+POR+HP+FTE+SUN+AMR+CYN+LEA+FITB+HTLD+MWA+MFC+NST+AXL+FRO+SCHN+MMM+AVY+JLL+SXT+DRC+EL+MCO+WFR+EPD+SJM+PXP+BCE+AN+HAR+COO+LINE+PAA+PDCO+TTEK+GVA+ITMN+RJF+SUSQ+NVO+VZ+LIHR+UBSI+PKG+MOT+V+MCHP+CVBF+BP+TFX+WBSN+PRU+ME+TNE+UBS+ARIA+HAIN+CSCO+LHCG+ACC+MBI+CHRS+CSR+TJX+FMER+VSAT+GMCR+CISG+TLB+DOV+MTB+WNR+BWLD+AVP+TNB+VRX+HOTT+ISIL+SBIB+TWTC+OLN+CELG+VFC+SAPE+LAMR+HON&f=sk1d1t1c1ohgvc1&e=.csv";
			url[2] = "http://download.finance.yahoo.com/d/quotes.csv?s=RTN+SNDA+UMPQ+ADM+CVH+LAZ+AKR+TUP+EBR+GAS+CIE+HSY+KRC+BLK+GPRO+DHR+FEIC+LSI+BKE+BGG+ALEX+RYAAY+CETV+PGR+UVV+BKH+ARW+WFMI+ETY+AF+LFT+PAYX+POWI+ASEI+WOOF+TMO+CALM+SGMS+BF-B+CPNO+PTEN+EXPE+HMA+ENOC+SFSF+PEP+WL+NTES+EV+ORN+ALKS+NI+ETH+MFA+BRCM+POT+GMR+PPG+NTRI+DELL+SBAC+VRTX+HOGS+PFG+CTRN+ENH+MAC+EFX+PHI+CRIC+RYN+CPWR+ABC+MAR+MKL+HSP+FTO+INSU+BTU+MON+DBRN+SRE+KLAC+MRVL+ICON+OVTI+KNX+CSGP+NRGY+BXS+ATLS+CTV+SWI+BCSI+FWLT+RFMD+EQT+ARMH+BEN+ANF+ATI+ACV+RGNC+TXRH+LGF+EDU+ULTA+MNI+BIO+MFE+NEU+TAP+PWE+GFA+MPW+ACAS+AVAV+IMAX+VIV+WTM+DTV+NWSA+SCG+OMG+EQR+CIM+MGM+JPM+AIB+BBBY+INTU+WRE+ALE+ALU+NTY+GDP+PMCS+LL+MMR+TWC+COF+WTI+SWKS+AOS+EZPW+RRGB+FCN+R+SKS+CSTR+RRC+PMI+IDC+GGB+ALTH+WY+NCR+EMC+CF+STX+CL+AMB+JASO+ILMN+CVS+PHH+EMR+FMX+SCCO+TIN+MEE+MCY+TROW+REP+MENT+FIRE+DLB+CPKI+LRCX+EBIX+TBL+BMRN+GYMB+AVT+SF+FHN+VIP+SKYW+STT+CVC+PPS+LOPE+NYX&f=sk1d1t1c1ohgvc1&e=.csv";
			url[3] = "http://download.finance.yahoo.com/d/quotes.csv?s=BBG+PPL+SLF+CRK+KBR+COST+NEM+RAD+NS+MS+TEF+AOD+PAG+TW+POM+CKH+SSRI+RHI+CEC+GOLD+RBN+UA+RPM+ACH+BOKF+TWX+DRQ+LTD+BAP+NFLX+VCI+WSM+BBBB+ARM+OKE+WST+ABX+CFN+RECN+FUL+OI+GMT+STI+KEG+SAH+BOBE+ZEUS+MDVN+TECH+TRLG+SHW+KT+LOGI+CELL+IPG+GFI+ENZN+GTLS+AYI+FMBI+SQNM+TKR+HOLX+VAL+PBR+BNS+WPI+AMZN+CUK+PENN+FNB+MDAS+TGT+DNDN+LYG+CME+ESRX+NBG+ORB+GRS+WMGI+DCM+TDW+CHK+IR+GES+ARG+NWS+BAC+NYT+SLT+ZQK+CHS+MOS+STP+DECK+TRI+UAUA+SLB+PSYS+PKI+JOYG+GWR+PIR+NUAN+AKS+CMS+VECO+FNFG+L+NXY+CBSH+CLI+PJC+FE+CSX+OCR+IBM+GPC+MRX+WEC+FBR+AMN+ASBC+AVGO+TKLC+ZRAN+ITG+VYFC+WHR+CHRW+EMN+CTB+GAP+ACXM+WTNY+DST+PHK+CMTL+WAG+KBH+AXP+OCN+HSIC+MET+MAA+PXD+SHOO+THG+D+WYNN+XLNX+BSBR+CLH+CAAS+ERTS+PZZA+AGL+IO+RDK+TLM+SEIC+LO+SNPS+QLGC+BEC+AGNC+RRD+ERJ+FLEX+RCII+IDA+ORLY+ATHN+EK+CAT+IDXX+NWBI+OGE+VSH+ETR+WMB+BR+MTH+SYKE+MIR+WPZ+AIG+CP+SGEN+HRL+GNW+ABV+MAN+PLCM+XOM+ADY+BRS+PII&f=sk1d1t1c1ohgvc1&e=.csv";
			url[4] = "http://download.finance.yahoo.com/d/quotes.csv?s=SOMX+XTO+AIXG+SOA+KWK+CNX+QGEN+AET+ACOR+ASH+HAE+EXC+WPO+QSII+HOS+AUXL+AFL+ELX+ALR+DVR+MKSI+BCR+AWH+NHP+WRC+LPS+LXP+PTR+TIE+ARD+TIF+CMED+DKS+AME+NVR+CTAS+CRZO+CRBC+GNTX+PGH+SKIL+OC+UNT+AMLN+JDAS+FNF+CMCSK+CDE+DLX+NTT+LNT+DEI+MCD+FISV+DAL+HEW+TII+CINF+ALB+ETE+BHI+ACM+BRY+TLK+CTCM+NTRS+GE+MDZ+PRAA+TIVO+ANN+DIOD+CMG+AM+PVTB+AU+MDRX+CAB+TEL+COLM+CE+APOL+BC+SAY+WLL+GLW+SWHC+ACE+UN+KMP+FINL+PSA+VLO+TMX+MO+BBY+HSC+SID+CFR+DTG+TSU+WCG+WOR+T+STRA+PCP+CAGC+TFSL+DAI+CREE+TPX+MKC+ASIA+AEO+SNH+ANR+BIG+PL+SRCL+TSN+EXP+PNK+SNI+PFCB+CSL+EM+WCN+ITT+VMC+AMMD+PG+SPG+LDK+OKS+CPL+PDE+REG+WRI+YHOO+JBHT+HTS+OEH+NU+IBN+CAKE+HMIN+TQNT+HNI+CHKP+DV+EMS+HPT+PNC+LMT+SY+AMD+WWW+MIL+PNRA+NICE+CHE+NTGR+ALL+EOG+FII+PRXL+BMS+HPQ+DLR+AONE+DTE+EVVV+HCP+WSH+WABC+IEX+NLC+KIM+BHE+DPS+NITE+MAS+ATW+PMTC+MATK+ENB+BRCD+^GSPC+DVA+NNN+CRS+GGC+WFT+GSIC+BVN+DHI+VR+F+VRGY+TSS+AGO&f=sk1d1t1c1ohgvc1&e=.csv";
			url[5] = "http://download.finance.yahoo.com/d/quotes.csv?s=AIR+GET+CML+JCI+STE+TIBX+EP+GT+VMW+ENTG+WSO+KFT+WRB+DWA+EPB+GTIV+LANC+GPI+BYI+ALGN+BXP+CPLA+GNK+AFAM+ESE+CSIQ+NYB+LUK+SNE+AEP+INFN+HUBG+WMT+HRS+SNA+THRX+PRGS+SNP+AON+PM+THO+PWR+LFL+BCS+AMCC+MOLX+DOLE+THQI+NSM+PBCT+SBH+MPG+MCRS+CVD+HCBK+SEE+HXL+ITC+SLG+G+BNE+E+TCB+PTV+SYY+ASTE+MTD+SFL+ALGT+LSTZA+TEVA+SHO+EXR+SNV+GILD+VMI+LBTYK+FMCN+DGW+YGE+FFIV+SWK+BPT+DNEX+KIRK+BMC+BEE+WBC+IRM+AXE+SYA+IART+AOL+GCO+HBI+MHP+BDC+SSL+ECLP+KGC+PVX+FLS+CYH+TRMB+JEC+PLD+ITRI+SIRO+KMX+CBD+CMC+CZZ+HRC+MTW+WTFC+BECN+WIT+CEG+OFC+LUFK+KDN+DGX+CMI+RDN+IACI+GNA+HANS+DISCK+MSFT+BOH+GMXR+INFY+MT+MYGN+LUV+MCK+ESI+IFN+ERIC+NM+WFSL+APH+ROST+CCJ+IDCC+SVU+WTS+CASY+NVE+SPR+DRIV+IGT+CHH+LIZ+GME+JDSU+FSLR+TOT+XL+HERO+LLY+BWA+HIW+DG+ATPG+TNDM+HES+PHM+NAL+TPC+LLTC+CPRT+MICC+BEAV+KALU+SANM+ESL+PDLI+OXPS+CEO+DDR+GRMN+AYE+TYC+Q+FO+LIFE+NETC+ATO+TTC+SAP+SIAL+OMI+PCS+WIN+PTP+SCI+LEN&f=sk1d1t1c1ohgvc1&e=.csv";
			url[6] = "http://download.finance.yahoo.com/d/quotes.csv?s=CLNE+XRAY+CAL+HMC+APA+BA+IAG+SPIL+SLH+BSX+CM+EQY+IHS+NUS+TRMK+NOK+CPSI+LXK+PVH+VVUS+SU+USB+PLCE+UEPS+BPZ+SFD+CROX+DDS+PSS+THC+URS+FTNT+CAJ+WTR+WG+PWRD+PETS+KMR+KSS+HTZ+UFPI+CRI+STO+SVNT+SQM+CA+AAWW+DGIT+SMG+BKD+RYL+WXS+WEN+EIX+AUO+UIS+RIG+IPI+ADTN+LCAPA+EOC+GEOY+CLB+RGA+COGT+BIIB+PNW+HMSY+AES+JNJ+WU+SNDK+ZMH+WAB+B+GTI+MDT+LYV+VIT+TXI+ABT+OIS+LZB+SXCI+LM+CR+LWSN+ROC+BVF+CBS+BRKR+DYN+VPRT+MUR+OMX+BBVA+TLAB+CIB+TLEO+BEXP+CNW+THI+NGG+AKAM+RDY+TE+WBMD+FICO+TAM+ES+SLXP+RF+C+LHO+RUE+DGI+SPF+RVBD+MMP+XIDE+FDX+CTSH+SLAB+FOE+OXY+HRBN+SO+SCHL+PNR+DD+IP+HRB+CLP+WLT+TEN+VSEA+USG+AA+DSX+PLL+VIA-B+MTL+RGS+FULT+TECD+DEO+FOSL+KERX+LVLT+GSK+ICUI+BPOP+AXS+PCG+WMS+FAST+XEC+PEGA+RKT+TDG+SOL+AMKR+SPLS+MWE+FSYS+ATK+ARUN+S+AZO+COH+NILE+ARB+CW+MWW+KYN+GOOG+NTAP+NVDA+TLCR+TWI+DRWI+EGLE+ADP+ECL+SYK+CAG+AMAT+ICLR+BRE+EEFT+NAV+TD+GGG+RBC+MDC+ADSK&f=sk1d1t1c1ohgvc1&e=.csv";
			url[7] = "http://download.finance.yahoo.com/d/quotes.csv?s=NEE+CMA+ALXN+NETL+APKT+THS+SKM+SKT+KCI+RMD+UFS+STJ+MEOH+AMTD+CENX+SH+CNK+COG+IBOC+JNS+BZ+STM+MIDD+TSO+FNM+TWGP+DPZ+ROSE+UNH+HUN+JKHY+AWK+SPWRB+NOC+PETM+UMC+STR+GRA+TTM+CJT+PNY+JWN+CHA+SYNA+DNR+LH+HMY+YUM+NZ+TS+ALV+ETP+NFX+RTI+OSIP+ALTR+SBS+RGC+TDY+ATU+TSRA+AAI+RHT+BEZ+RNR+PGN+CSC+DOX+CLD+MDR+LNCR+NATI+NDN+JBL+K+DLM+GEO+ABB+NBL+RISK+TRAK+ONNN+RAH+BAM+FDS+COP+XCO+INCY+HCN+MXB+GLBL+BJ+GD+AMT+MSCC+BAX+AAPL+FLR+HOC+EDMC+FLIR+CIG+LEAP+ADS+AGCO+BJRI+WM+TRV+TCO+CBG+JCP+UTX+CIT+MD+HEAT+LCC+JOE+ETFC+LNC+OTEX+JNY+MSG+VTR+APC+WRLD+PAR+ESV+TRN+NPBC+SIG+RINO+CHU+CTCT+SAFM+STZ+WYN+NKTR+CCMP+FL+KND+AGN+PRGO+ONXX+LOW+SVM+SEED+ACF+BGC+TM+RTP+SBNY+UNP+RE+MBFI+ENI+VALE+HBC+MRO+HME+SI+JRCC+GENZ+WBS+LECO+NWE+RS+SJI+CMCSA+HLX+RCL+ASCA+XRX+RT+MELI+BKS+IVC+LTM+DRYS+PACW+USU+EWBC+TSM+VPHM+ANW+CYMI+TTWO+CHD+YZC+ROVI+FCX+MPWR+WR+CBST+PEET+SINA+ACN+CECO+AXA+LRY+IRF&f=sk1d1t1c1ohgvc1&e=.csv";
			url[8] = "http://download.finance.yahoo.com/d/quotes.csv?s=PCH+ENDP+CCL+HBHC+KEX+PRA+RWT+CDNS+EVEP+WATG+OMC+PQ+CX+PRE+VRSN+ALTE+PRX+GWW+TER+CYT+SM+SLM+ATR+SFG+UHS+JBLU+BMR+WDR+EXPD+CNP+SPW+BLUD+SYNT+AEG+KMT+NFG+AJG+CB+JAS+KB+NSR+SE+SCHW+LSTR+DOW+ACL+SNX+DMND+CPX+FUQI+LNN+NAT+ARST+LPX+GBCI+ARE+TTI+PFE+GERN+ELN+GEF+RBA+HUM+KAR+PNM+APEI+QSFT+PCX+HIG+ALK+JACK+LII+SIRI+CEPH+EXM+CMO+GPN+STD+TC+TSCO+DFG+PEG+MI+RDC+MMC+HL+CLX+DBD+SMTC+WFC+BUCY+NE+PLT+PCL+UPS+TRP+SHPGY+JAH+LVS+ENER+MJN+RAI+ITUB+PH+BZH+ARTG+MSTR+CVX+TTEC+FST+EPR+ODFL+GIL+CCE+HLIT+APD+AB+ENR+WCC+ABVT+WGL+MASI+BBD+NVLS+FDP+CY+X+NUVA+TXT+EJ+GDI+HGSI+NWL+RY+BRO+CUZ+INTC+IPXL+BMY+HST+VOLC+ARBA+JCOM+AAN+GG+PPDI+ZION+AIV+TCK+PSUN+JEF+HSNI+AEE+ACGL+BDN+KFY+MBT+SD+CLF+FOR+PHG+SFY+CIEN+MHK+LINTA+CNQR+PAY+SLW+VIVO+DVN+AWI+ACI+TMK+ED+REGN+NGLS+RMBS+RCI+ING+ORI+BBL+IOC+ATVI+ICE+MLM+AMGN+MXIM+IBKR+GPS+XEL+NSC+DISCA+BPI+AMSC+PAAS+NBR&f=sk1d1t1c1ohgvc1&e=.csv";
			url[9] = "http://download.finance.yahoo.com/d/quotes.csv?s=AFFX+MTN+CWTR+OSIS+VCLK+BUD+VLTR+CFL+CTL+HGG+AMED+CVLT&f=sk1d1t1c1ohgvc1&e=.csv";

			Stack<String> inLines = new Stack<String>();
			try {
				Connection conn = DriverManager.getConnection(Constants.URL, Constants.USERNAME, Constants.PASSWORD);
				ArrayList<String> records = new ArrayList<String>();
				for (int a = 0; a < url.length; a++) {
					URL yahoo = new URL(url[a]);
				    URLConnection yahooConnection = yahoo.openConnection();
				    BufferedReader in = new BufferedReader(new InputStreamReader(yahooConnection.getInputStream()));
					
				    // Reverse it so moving averages go in the right direction
				    String inputLine;
				    while ((inputLine = in.readLine()) != null) {
				    	try {
					    	String[] lineValues = inputLine.split(",");
						  	String symbol = lineValues[0];
					    	String date = lineValues[2];
						  	float open = new Float(lineValues[5]);
						  	String closeString = lineValues[1];
						  	float high = new Float(lineValues[6]);
						  	float low = new Float(lineValues[7]);
						  	long volume = new Long(lineValues[8]);
						  	float change = new Float(lineValues[9]);
						  	
						  	symbol = symbol.replaceAll("\"", "");
						  	
						  	// Get rid of the time and xml from the close
						  	closeString = closeString.substring(closeString.indexOf(">") + 1);
						  	closeString = closeString.substring(0,closeString.indexOf("<"));
						  	float close = new Float(closeString);
						  	float gap = open - (close - change);
						  
						  	change = (float)(Math.round(change*100.0f)/100.0f);
						  	gap = (float)(Math.round(gap*100.0f)/100.0f);
						  	
						  	// Convert from 4/8/2010 to 2010-04-08 format
						  	date = date.replaceAll("\"", "");
						    String month = date.substring(0, date.indexOf("/"));
						    String dates = date.substring(date.indexOf("/") + 1, date.indexOf("/201"));
						    String year = date.substring(date.length() - 4);
						    if (month.length() == 1) {
						    	month = "0" + month;
						    }
						    if (dates.length() == 1) {
						    	dates = "0" + dates;
						    }
						    date = year + "-" + month + "-" + dates;
						  	
						  	StringBuilder sb = new StringBuilder();
						  	sb.append("('");
						  	sb.append(symbol);
						  	sb.append("', '");
						  	sb.append(date);
						  	sb.append("', ");
						  	sb.append(volume);
						  	sb.append(", ");
						  	sb.append(open);
						  	sb.append(", ");
						  	sb.append(close);
						  	sb.append(", ");
						  	sb.append(high);
						  	sb.append(", ");
						  	sb.append(low);
						  	sb.append(", ");
						  	sb.append(change);
						  	sb.append(", ");
						  	sb.append(gap);
						  	sb.append(", ");
						  	sb.append("true");
						  	sb.append(")");
						  	
						  	records.add(sb.toString());
				    	}
				    	catch (Exception e) {}
				    }
				    in.close();
				}
				if (records.size() > 0) {
					new UpdateHistoricalPrices("", ""/*Constants.SP500P_TABLE, Constants.SP500STATSP_TABLE*/).insertRecords(records, conn);
				}
				conn.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * End of day version for full cleansing.
	 */
	public static void atEndOfDay(String sp500Table, String sp500StatsTable) {
		System.out.println("Starting at " + Calendar.getInstance().getTime().toString());
		
		// Delete everything in sp500statistics and sp500
		try {
			Class.forName("org.postgresql.Driver").newInstance();
			
			Connection conn = DriverManager.getConnection(Constants.URL, Constants.USERNAME, Constants.PASSWORD);
			if (!conn.isClosed()) {
				String dQuery = "TRUNCATE TABLE " + sp500StatsTable;
				Statement dStatement = conn.createStatement();
				dStatement.executeUpdate(dQuery);
												
				String deleteQuery = "TRUNCATE TABLE " + sp500Table;
				Statement deleteStatement = conn.createStatement();
				deleteStatement.executeUpdate(deleteQuery);
			}
			conn.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		// Symbol list reading from DB
		ArrayList<String> symbols = new ArrayList<String>();
		try {
			Class.forName("org.postgresql.Driver").newInstance();
			
			Connection conn = DriverManager.getConnection(Constants.URL, Constants.USERNAME, Constants.PASSWORD);
			if (!conn.isClosed()) {
				String selectQuery = "SELECT DISTINCT symbol FROM indexlist";
				Statement selectStatement = conn.createStatement();
				ResultSet selectRS = selectStatement.executeQuery(selectQuery);
				
				while (selectRS.next()) {
					symbols.add(selectRS.getString("symbol"));
				}
			}
			conn.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		// Dispatch multiple worker threads to divide up the symbols and hit Yahoo Finance
		ArrayList<String> symbolBlock1 = new ArrayList<String>();
		ArrayList<String> symbolBlock2 = new ArrayList<String>();
		ArrayList<String> symbolBlock3 = new ArrayList<String>();
		ArrayList<String> symbolBlock4 = new ArrayList<String>();
		ArrayList<String> symbolBlock5 = new ArrayList<String>();
		ArrayList<String> symbolBlock6 = new ArrayList<String>();
		ArrayList<String> symbolBlock7 = new ArrayList<String>();
		ArrayList<String> symbolBlock8 = new ArrayList<String>();
		for (int a = 0; a < symbols.size(); a++) {
			if (a % 8 == 0) 
				symbolBlock1.add(symbols.get(a));
			if (a % 8 == 1) 
				symbolBlock2.add(symbols.get(a));
			if (a % 8 == 2) 
				symbolBlock3.add(symbols.get(a));
			if (a % 8 == 3) 
				symbolBlock4.add(symbols.get(a));
			if (a % 8 == 4) 
				symbolBlock5.add(symbols.get(a));
			if (a % 8 == 5) 
				symbolBlock6.add(symbols.get(a));
			if (a % 8 == 6) 
				symbolBlock7.add(symbols.get(a));
			if (a % 8 == 7) 
				symbolBlock8.add(symbols.get(a));
		}
		UpdateHistoricalPrices t1 = new UpdateHistoricalPrices(symbolBlock1, sp500Table, sp500StatsTable);
		t1.start();
		UpdateHistoricalPrices t2 = new UpdateHistoricalPrices(symbolBlock2, sp500Table, sp500StatsTable);
		t2.start();
		UpdateHistoricalPrices t3 = new UpdateHistoricalPrices(symbolBlock3, sp500Table, sp500StatsTable);
		t3.start();
		UpdateHistoricalPrices t4 = new UpdateHistoricalPrices(symbolBlock4, sp500Table, sp500StatsTable);
		t4.start();
		UpdateHistoricalPrices t5 = new UpdateHistoricalPrices(symbolBlock5, sp500Table, sp500StatsTable);
		t5.start();
		UpdateHistoricalPrices t6 = new UpdateHistoricalPrices(symbolBlock6, sp500Table, sp500StatsTable);
		t6.start();
		UpdateHistoricalPrices t7 = new UpdateHistoricalPrices(symbolBlock7, sp500Table, sp500StatsTable);
		t7.start();
		UpdateHistoricalPrices t8 = new UpdateHistoricalPrices(symbolBlock8, sp500Table, sp500StatsTable);
		t8.start();
		
		try {
			t1.join();
			t2.join();
			t3.join();
			t4.join();
			t5.join();
			t6.join();
			t7.join();
			t8.join();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		try {
			Connection conn = DriverManager.getConnection(Constants.URL, Constants.USERNAME, Constants.PASSWORD);
			
			for (String symbol:symbols) {
				System.out.println("Now processing " + symbol + " at " + Calendar.getInstance().getTime().toString());
				
				// Find the most recent date that we already have info for this symbol
				String mostRecentYear = "1999";
				String mostRecentMonth = "01";
				String mostRecentDay = "01";
				if (sp500Table.equals(""/*Constants.SP500P_TABLE*/)) {
					mostRecentYear = new Integer(Calendar.getInstance().get(Calendar.YEAR) - 1).toString(); 
					mostRecentMonth = new Integer(Calendar.getInstance().get(Calendar.MONTH)).toString();
					mostRecentDay = new Integer(Calendar.getInstance().get(Calendar.DATE)).toString();
				}

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
				    boolean first = true;
				    while ((inputLine = in.readLine()) != null) {
				    	if (!first)
				    		inLines.push(inputLine);
				    	first = false;
				    }
				    in.close();
				}
				catch (Exception e) {
					e.printStackTrace();
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
				  
				  	change = (float)(Math.round(change*100.0f)/100.0f);
				  	gap = (float)(Math.round(gap*100.0f)/100.0f);
				  	
				  	StringBuilder sb = new StringBuilder();
				  	sb.append("('");
				  	sb.append(symbol);
				  	sb.append("', '");
				  	sb.append(date);
				  	sb.append("', ");
				  	sb.append(volume);
				  	sb.append(", ");
				  	sb.append(adjOpen);
				  	sb.append(", ");
				  	sb.append(adjClose);
				  	sb.append(", ");
				  	sb.append(adjHigh);
				  	sb.append(", ");
				  	sb.append(adjLow);
				  	sb.append(", ");
				  	sb.append(change);
				  	sb.append(", ");
				  	sb.append(gap);
				  	sb.append(", ");
				  	sb.append("false");
				  	sb.append(")");
				  	
				  	if (a > 0) { // Don't add the first one because the change and gap will be wrong
				  		records.add(sb.toString());
				  	}
				  	
				  	if (counter % 2000 == 1999) {
				  		insertRecords(records, conn);
				  		records.clear();
				  	}
				  	
				  	yesterdayAdjClose = adjClose;
				  	
				  	counter++;
				}
				if (records.size() > 0) {
					insertRecords(records, conn);
					records.clear();
				}
			}
			conn.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void insertRecords(ArrayList<String> records, Connection conn) {
		try {
			Class.forName("org.postgresql.Driver").newInstance();
			
			String insertQuery = "INSERT INTO " + sp500Table + " (symbol, date, volume, adjopen, adjclose, adjhigh, adjlow, change, gap, partial) VALUES ";
			StringBuilder sb = new StringBuilder();
			for (String record:records) {
				sb.append(record);
				sb.append(", ");
			}
			String valuesPart = sb.toString();
			valuesPart = valuesPart.substring(0, valuesPart.length() - 2);
			insertQuery = insertQuery + valuesPart;
			
			Statement insertStatement = conn.createStatement();
			insertStatement.executeUpdate(insertQuery);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}