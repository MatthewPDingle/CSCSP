-- Table: results

-- DROP TABLE results;

CREATE TABLE results
(
  id integer NOT NULL DEFAULT nextval('results_id_sequence'::regclass),
  batch text,
  buystrategysummary text,
  sellstrategysummary text,
  rundate date,
  endingvalue numeric,
  symbol text,
  initialinvestment numeric,
  totalnetprofit numeric,
  totalnetprofitpercent numeric,
  baselineperformancepercent numeric,
  alphapercent numeric,
  grossprofit numeric,
  grossloss numeric,
  numtrades integer,
  numlongtrades integer,
  numshorttrades integer,
  numwinningtrades integer,
  percentwinningtrades numeric,
  numlosingtrades integer,
  percentlosingtrades numeric,
  maxnumconsecutivewins integer,
  maxnumconsecutivelosses integer,
  startdate date,
  enddate date,
  averagetradeduration numeric,
  averagegainpertrade numeric,
  averagegainperlongtrade numeric,
  averagegainpershorttrade numeric,
  averagegainpertradepercent numeric,
  averagegainperlongtradepercent numeric,
  averagegainpershorttradepercent numeric,
  averagegainperwinningtradepercent numeric,
  averagegainperlosingtradepercent numeric,
  ratioaveragewinloss numeric,
  largestwinningtrade numeric,
  largestlosingtrade numeric,
  maxdrawdown numeric,
  maxdrawdownpercent numeric,
  profitfactor numeric,
  CONSTRAINT results6_pkey PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE results OWNER TO postgres;

-- Index: results4_alphapercent_index

-- DROP INDEX results4_alphapercent_index;

CREATE INDEX results4_alphapercent_index
  ON results
  USING btree
  (alphapercent);

-- Index: results4_averagetradeduration_index

-- DROP INDEX results4_averagetradeduration_index;

CREATE INDEX results4_averagetradeduration_index
  ON results
  USING btree
  (averagetradeduration);

-- Index: results4_batch_index

-- DROP INDEX results4_batch_index;

CREATE INDEX results4_batch_index
  ON results
  USING btree
  (batch);

-- Index: results4_buystrategysummary

-- DROP INDEX results4_buystrategysummary;

CREATE INDEX results4_buystrategysummary
  ON results
  USING btree
  (buystrategysummary);

-- Index: results4_maxdrawdownpercent_index

-- DROP INDEX results4_maxdrawdownpercent_index;

CREATE INDEX results4_maxdrawdownpercent_index
  ON results
  USING btree
  (maxdrawdownpercent);

-- Index: results4_numtrades_index

-- DROP INDEX results4_numtrades_index;

CREATE INDEX results4_numtrades_index
  ON results
  USING btree
  (numtrades);

-- Index: results4_numwinningtrades_index

-- DROP INDEX results4_numwinningtrades_index;

CREATE INDEX results4_numwinningtrades_index
  ON results
  USING btree
  (numwinningtrades);

-- Index: results4_sellstrategysummary

-- DROP INDEX results4_sellstrategysummary;

CREATE INDEX results4_sellstrategysummary
  ON results
  USING btree
  (sellstrategysummary);

-- Index: results4_symbol

-- DROP INDEX results4_symbol;

CREATE INDEX results4_symbol
  ON results
  USING btree
  (symbol);

-- Index: results4_symbol_index

-- DROP INDEX results4_symbol_index;

CREATE INDEX results4_symbol_index
  ON results
  USING btree
  (symbol);

