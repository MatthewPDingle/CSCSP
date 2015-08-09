-- Table: metriccalcessentials

-- DROP TABLE metriccalcessentials;

CREATE TABLE metriccalcessentials
(
  name text,
  symbol text,
  duration text,
  start timestamp without time zone,
  varname text,
  varvalue real[]
)
WITH (
  OIDS=FALSE
);
ALTER TABLE metriccalcessentials
  OWNER TO postgres;
