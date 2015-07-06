-- Table: sectorandindustry

-- DROP TABLE sectorandindustry;

CREATE TABLE sectorandindustry
(
  symbol character varying(8),
  sector character varying(128),
  industry character varying(128)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE sectorandindustry
  OWNER TO postgres;

-- Index: index_symbol

-- DROP INDEX index_symbol;

CREATE INDEX index_symbol
  ON sectorandindustry
  USING btree
  (symbol COLLATE pg_catalog."default");

