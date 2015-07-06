-- Table: bitcoinbar

-- DROP TABLE bitcoinbar;

CREATE TABLE bitcoinbar
(
  symbol text,
  open real,
  close real,
  high real,
  low real,
  vwap real,
  volume real,
  numtrades integer,
  change real,
  gap real,
  start timestamp without time zone,
  "end" timestamp without time zone,
  duration text
)
WITH (
  OIDS=FALSE
);
ALTER TABLE bitcoinbar
  OWNER TO postgres;

-- Index: bitcoinbar_duration_index

-- DROP INDEX bitcoinbar_duration_index;

CREATE INDEX bitcoinbar_duration_index
  ON bitcoinbar
  USING btree
  (duration COLLATE pg_catalog."default");

