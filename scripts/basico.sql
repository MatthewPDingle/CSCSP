-- Table: basico

-- DROP TABLE basico;

CREATE TABLE basico
(
  symbol text NOT NULL,
  date date NOT NULL,
  frontmonthcallvolume integer,
  frontmonthputvolume integer,
  frontmonthcallopenint integer,
  frontmonthputopenint integer,
  CONSTRAINT basico_primarykey PRIMARY KEY (symbol, date)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE basico
  OWNER TO postgres;

-- Index: basico_date_index

-- DROP INDEX basico_date_index;

CREATE INDEX basico_date_index
  ON basico
  USING btree
  (date);

-- Index: basico_date_symbol_index

-- DROP INDEX basico_date_symbol_index;

CREATE INDEX basico_date_symbol_index
  ON basico
  USING btree
  (date, symbol COLLATE pg_catalog."default");

-- Index: basico_symbol_index

-- DROP INDEX basico_symbol_index;

CREATE INDEX basico_symbol_index
  ON basico
  USING btree
  (symbol COLLATE pg_catalog."default");

