-- Table: coloredmapstats

-- DROP TABLE coloredmapstats;

CREATE TABLE coloredmapstats
(
  rsitype character varying,
  xvtype character varying,
  rsilower real,
  rsiupper real,
  xvlower real,
  xvupper real,
  count bigint,
  mean double precision,
  geomean double precision,
  winningpercentage double precision
)
WITH (
  OIDS=FALSE
);
ALTER TABLE coloredmapstats OWNER TO postgres;

-- Index: cms_4_index

-- DROP INDEX cms_4_index;

CREATE INDEX cms_4_index
  ON coloredmapstats
  USING btree
  (rsilower, rsiupper, xvlower, xvupper);

-- Index: index_rsilower

-- DROP INDEX index_rsilower;

CREATE INDEX index_rsilower
  ON coloredmapstats
  USING btree
  (rsilower);

-- Index: index_rsiupper

-- DROP INDEX index_rsiupper;

CREATE INDEX index_rsiupper
  ON coloredmapstats
  USING btree
  (rsiupper);

-- Index: index_xvlower

-- DROP INDEX index_xvlower;

CREATE INDEX index_xvlower
  ON coloredmapstats
  USING btree
  (xvlower);

-- Index: index_xvupper

-- DROP INDEX index_xvupper;

CREATE INDEX index_xvupper
  ON coloredmapstats
  USING btree
  (xvupper);

