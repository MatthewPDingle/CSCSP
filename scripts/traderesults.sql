-- Table: traderesults

-- DROP TABLE traderesults;

CREATE TABLE traderesults
(
  resultsid integer,
  buydate date,
  selldate date,
  startvalue double precision,
  endvalue double precision,
  profit double precision
)
WITH (
  OIDS=FALSE
);
ALTER TABLE traderesults OWNER TO postgres;

-- Index: traderesults_resultsid_index

-- DROP INDEX traderesults_resultsid_index;

CREATE INDEX traderesults_resultsid_index
  ON traderesults
  USING btree
  (resultsid);

