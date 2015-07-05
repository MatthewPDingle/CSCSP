-- Table: percentchangeanalysis

-- DROP TABLE percentchangeanalysis;

CREATE TABLE percentchangeanalysis
(
  symbol text,
  date date,
  adjclose real,
  tomorrowpercentchange double precision,
  dayaftertomorrowpercentchange double precision
)
WITH (
  OIDS=FALSE
);
ALTER TABLE percentchangeanalysis OWNER TO postgres;

-- Index: pca_adjclose_index

-- DROP INDEX pca_adjclose_index;

CREATE INDEX pca_adjclose_index
  ON percentchangeanalysis
  USING btree
  (adjclose);

-- Index: pca_dayaftertomorrowpercentchange_index

-- DROP INDEX pca_dayaftertomorrowpercentchange_index;

CREATE INDEX pca_dayaftertomorrowpercentchange_index
  ON percentchangeanalysis
  USING btree
  (dayaftertomorrowpercentchange);

-- Index: pca_torowpercentchange_index

-- DROP INDEX pca_torowpercentchange_index;

CREATE INDEX pca_torowpercentchange_index
  ON percentchangeanalysis
  USING btree
  (tomorrowpercentchange);

