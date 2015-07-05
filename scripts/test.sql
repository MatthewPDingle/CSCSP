-- Table: test

-- DROP TABLE test;

CREATE TABLE test
(
  symbol character varying(10) NOT NULL,
  date date NOT NULL,
  "value" real,
  CONSTRAINT metric_pk PRIMARY KEY (symbol, date)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE test OWNER TO postgres;

-- Index: test_date_index

-- DROP INDEX test_date_index;

CREATE INDEX test_date_index
  ON test
  USING btree
  (date);

-- Index: test_date_symbol_index

-- DROP INDEX test_date_symbol_index;

CREATE INDEX test_date_symbol_index
  ON test
  USING btree
  (symbol, date);

-- Index: test_date_symbol_value_index

-- DROP INDEX test_date_symbol_value_index;

CREATE INDEX test_date_symbol_value_index
  ON test
  USING btree
  (symbol, value, date);

-- Index: test_date_value_index

-- DROP INDEX test_date_value_index;

CREATE INDEX test_date_value_index
  ON test
  USING btree
  (date, value);

-- Index: test_symbol_index

-- DROP INDEX test_symbol_index;

CREATE INDEX test_symbol_index
  ON test
  USING btree
  (symbol);

-- Index: test_symbol_value_index

-- DROP INDEX test_symbol_value_index;

CREATE INDEX test_symbol_value_index
  ON test
  USING btree
  (symbol, value);

-- Index: test_value_index

-- DROP INDEX test_value_index;

CREATE INDEX test_value_index
  ON test
  USING btree
  (value);

