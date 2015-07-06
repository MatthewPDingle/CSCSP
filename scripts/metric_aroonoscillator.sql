-- Table: metric_aroonoscillator

-- DROP TABLE metric_aroonoscillator;

CREATE TABLE metric_aroonoscillator
(
  symbol character varying(10) NOT NULL,
  date date NOT NULL,
  value real,
  CONSTRAINT metric_aroonoscillator_pk PRIMARY KEY (symbol, date)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE metric_aroonoscillator
  OWNER TO postgres;
