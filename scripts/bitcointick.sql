-- Table: bitcointick

-- DROP TABLE bitcointick;

CREATE TABLE bitcointick
(
  symbol text,
  price real,
  volume real,
  "timestamp" timestamp without time zone
)
WITH (
  OIDS=FALSE
);
ALTER TABLE bitcointick
  OWNER TO postgres;

-- Index: bitcointick_symbol_index

-- DROP INDEX bitcointick_symbol_index;

CREATE INDEX bitcointick_symbol_index
  ON bitcointick
  USING btree
  (symbol COLLATE pg_catalog."default");

-- Index: bitcointick_timestamp_index

-- DROP INDEX bitcointick_timestamp_index;

CREATE INDEX bitcointick_timestamp_index
  ON bitcointick
  USING btree
  ("timestamp");

