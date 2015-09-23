-- Table: trades

-- DROP TABLE trades;

CREATE TABLE trades
(
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
  actualexitprice real,
  exitreason text,
  commission real,
  netprofit real,
  grossprofit real,
  model text,
  sell text,
  sellvalue real,
  stop text,
  stopvalue real
)
WITH (
  OIDS=FALSE
);
ALTER TABLE trades
  OWNER TO postgres;
