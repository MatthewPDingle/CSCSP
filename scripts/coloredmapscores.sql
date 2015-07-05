-- Table: coloredmapscores

-- DROP TABLE coloredmapscores;

CREATE TABLE coloredmapscores
(
  mapname text,
  x real,
  y real,
  geomean real,
  winningpercentage real,
  samplesize integer,
  buyzone bit(1)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE coloredmapscores OWNER TO postgres;

-- Index: cms_2_index

-- DROP INDEX cms_2_index;

CREATE INDEX cms_2_index
  ON coloredmapscores
  USING btree
  (x, y);

-- Index: index_x

-- DROP INDEX index_x;

CREATE INDEX index_x
  ON coloredmapscores
  USING btree
  (x);

-- Index: index_y

-- DROP INDEX index_y;

CREATE INDEX index_y
  ON coloredmapscores
  USING btree
  (y);

