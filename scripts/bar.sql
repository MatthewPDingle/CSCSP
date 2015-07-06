-- Table: bar

-- DROP TABLE bar;

CREATE TABLE bar
(
  symbol text NOT NULL,
  open real,
  close real,
  high real,
  low real,
  vwap real,
  volume real,
  numtrades integer,
  change real,
  gap real,
  start timestamp without time zone NOT NULL,
  "end" timestamp without time zone,
  duration text NOT NULL,
  partial boolean,
  CONSTRAINT bar_pk PRIMARY KEY (symbol, start, duration)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE bar
  OWNER TO postgres;

-- Index: bar_close_index

-- DROP INDEX bar_close_index;

CREATE INDEX bar_close_index
  ON bar
  USING btree
  (close);

-- Index: bar_duration_index

-- DROP INDEX bar_duration_index;

CREATE INDEX bar_duration_index
  ON bar
  USING btree
  (duration COLLATE pg_catalog."default");

-- Index: bar_gap_index

-- DROP INDEX bar_gap_index;

CREATE INDEX bar_gap_index
  ON bar
  USING btree
  (gap);

-- Index: bar_partial_index

-- DROP INDEX bar_partial_index;

CREATE INDEX bar_partial_index
  ON bar
  USING btree
  (partial);

-- Index: bar_start_index

-- DROP INDEX bar_start_index;

CREATE INDEX bar_start_index
  ON bar
  USING btree
  (start);

-- Index: bar_symbol_index

-- DROP INDEX bar_symbol_index;

CREATE INDEX bar_symbol_index
  ON bar
  USING btree
  (symbol COLLATE pg_catalog."default");

-- Index: bar_symbol_start_index

-- DROP INDEX bar_symbol_start_index;

CREATE INDEX bar_symbol_start_index
  ON bar
  USING btree
  (symbol COLLATE pg_catalog."default", start);

-- Index: bar_volume_index

-- DROP INDEX bar_volume_index;

CREATE INDEX bar_volume_index
  ON bar
  USING btree
  (volume);

