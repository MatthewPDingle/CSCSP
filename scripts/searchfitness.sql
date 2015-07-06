-- Table: searchfitness

-- DROP TABLE searchfitness;

CREATE TABLE searchfitness
(
  type character varying(64) NOT NULL,
  metric character varying(64) NOT NULL,
  value real,
  fitness real NOT NULL
)
WITH (
  OIDS=FALSE
);
ALTER TABLE searchfitness
  OWNER TO postgres;
