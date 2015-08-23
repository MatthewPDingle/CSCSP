-- Table: models

-- DROP TABLE models;

CREATE TABLE models
(
  id serial NOT NULL,
  modelfile text,
  algo text,
  params text,
  symbol text,
  duration text,
  metrics text[],
  trainstart timestamp without time zone,
  trainend timestamp without time zone,
  teststart timestamp without time zone,
  testend timestamp without time zone,
  sellmetric text,
  sellmetricvalue real,
  stopmetric text,
  stopmetricvalue real,
  numbars integer,
  traindatasetsize integer,
  traintruenegatives integer,
  trainfalsenegatives integer,
  trainfalsepositives integer,
  traintruepositives integer,
  traintruepositiverate real,
  trainfalsepositiverate real,
  traincorrectrate real,
  trainkappa real,
  trainmeanabsoluteerror real,
  trainrootmeansquarederror real,
  trainrelativeabsoluteerror real,
  trainrootrelativesquarederror real,
  trainrocarea real,
  testdatasetsize integer,
  testtruenegatives integer,
  testfalsenegatives integer,
  testfalsepositives integer,
  testtruepositives integer,
  testtruepositiverate real,
  testfalsepositiverate real,
  testcorrectrate real,
  testkappa real,
  testmeanabsoluteerror real,
  testrootmeansquarederror real,
  testrelativeabsoluteerror real,
  testrootrelativesquarederror real,
  testrocarea real,
  CONSTRAINT models_pkey PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE models
  OWNER TO postgres;
