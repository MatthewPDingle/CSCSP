-- Table: strategies

-- DROP TABLE strategies;

CREATE TABLE strategies
(
  resultsid integer,
  strategy text
)
WITH (
  OIDS=FALSE
);
ALTER TABLE strategies OWNER TO postgres;

-- Index: strategies_resultsid_index

-- DROP INDEX strategies_resultsid_index;

CREATE INDEX strategies_resultsid_index
  ON strategies
  USING btree
  (resultsid);

-- Index: strategies_strategy_index

-- DROP INDEX strategies_strategy_index;

CREATE INDEX strategies_strategy_index
  ON strategies
  USING btree
  (strategy);

