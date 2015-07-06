-- Table: basicf

-- DROP TABLE basicf;

CREATE TABLE basicf
(
  symbol text NOT NULL,
  date date NOT NULL,
  marketcap bigint,
  trailingpe real,
  forwardpe real,
  pegratio real,
  pricesales real,
  pricebook real,
  profitmargin real,
  operatingmargin real,
  returnonassets real,
  returnonequity real,
  revenuegrowth real,
  earningsgrowth real,
  bookvaluepershare real,
  insiownp real,
  instownp real,
  shortratio real,
  trailingyield real,
  forwardyield real,
  CONSTRAINT basicf_primarykey PRIMARY KEY (symbol, date)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE basicf
  OWNER TO postgres;

-- Index: basicf_date_index

-- DROP INDEX basicf_date_index;

CREATE INDEX basicf_date_index
  ON basicf
  USING btree
  (date);

-- Index: basicf_date_symbol_index

-- DROP INDEX basicf_date_symbol_index;

CREATE INDEX basicf_date_symbol_index
  ON basicf
  USING btree
  (date, symbol COLLATE pg_catalog."default");

-- Index: basicf_symbol_index

-- DROP INDEX basicf_symbol_index;

CREATE INDEX basicf_symbol_index
  ON basicf
  USING btree
  (symbol COLLATE pg_catalog."default");

