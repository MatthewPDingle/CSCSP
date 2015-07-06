-- Table: basicr

-- DROP TABLE basicr;

CREATE TABLE basicr
(
  symbol text NOT NULL,
  date date NOT NULL,
  volume bigint,
  adjopen real,
  adjclose real,
  adjhigh real,
  adjlow real,
  change real,
  gap real,
  partial boolean,
  CONSTRAINT basicr_primarykey PRIMARY KEY (symbol, date)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE basicr
  OWNER TO postgres;

-- Index: basicr_adjclose_index

-- DROP INDEX basicr_adjclose_index;

CREATE INDEX basicr_adjclose_index
  ON basicr
  USING btree
  (adjclose);

-- Index: basicr_date_index

-- DROP INDEX basicr_date_index;

CREATE INDEX basicr_date_index
  ON basicr
  USING btree
  (date);

-- Index: basicr_gap_index

-- DROP INDEX basicr_gap_index;

CREATE INDEX basicr_gap_index
  ON basicr
  USING btree
  (gap);

-- Index: basicr_partial_index

-- DROP INDEX basicr_partial_index;

CREATE INDEX basicr_partial_index
  ON basicr
  USING btree
  (partial);

-- Index: basicr_symbol_date_index

-- DROP INDEX basicr_symbol_date_index;

CREATE INDEX basicr_symbol_date_index
  ON basicr
  USING btree
  (symbol COLLATE pg_catalog."default", date);

-- Index: basicr_symbol_index

-- DROP INDEX basicr_symbol_index;

CREATE INDEX basicr_symbol_index
  ON basicr
  USING btree
  (symbol COLLATE pg_catalog."default");

-- Index: basicr_volume_index

-- DROP INDEX basicr_volume_index;

CREATE INDEX basicr_volume_index
  ON basicr
  USING btree
  (volume);

