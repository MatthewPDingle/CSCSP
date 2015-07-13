-- Table: searchresults

-- DROP TABLE searchresults;

CREATE TABLE searchresults
(
  bullscore real NOT NULL,
  bearscore real NOT NULL,
  rundate date NOT NULL,
  buy1 character varying(64) NOT NULL,
  buy2 character varying(64) NOT NULL,
  sell character varying(64) NOT NULL,
  sellop character varying(16) NOT NULL,
  sellvalue real NOT NULL,
  stop character varying(64),
  stopvalue real,
  fromdate date NOT NULL,
  todate date NOT NULL,
  xres integer NOT NULL,
  yres integer NOT NULL,
  liquidity real NOT NULL,
  volatility real NOT NULL,
  price real NOT NULL,
  sector character varying(64) NOT NULL,
  industry character varying(64) NOT NULL,
  nyse boolean NOT NULL,
  nasdaq boolean NOT NULL,
  djia boolean NOT NULL,
  sp500 boolean NOT NULL,
  etf boolean NOT NULL,
  bitcoin boolean NOT NULL
)
WITH (
  OIDS=FALSE
);
ALTER TABLE searchresults
  OWNER TO postgres;
