-- Table: trades

-- DROP TABLE trades;

CREATE TABLE trades
(
  id integer,
  status text,
  entry timestamp without time zone,
  exit timestamp without time zone,
  type text,
  symbol text,
  duration text,
  shares real,
  suggestedentryprice real,
  actualentryprice real,
  suggestedexitprice real,
  suggestedstopprice real,
  actualexitprice real,
  exitreason text,
  commission real,
  netprofit real,
  grossprofit real,
  model text,
  expiration timestamp without time zone
)
WITH (
  OIDS=FALSE
);
ALTER TABLE trades
  OWNER TO postgres;
