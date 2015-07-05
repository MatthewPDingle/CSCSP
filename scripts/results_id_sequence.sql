-- Sequence: results_id_sequence

-- DROP SEQUENCE results_id_sequence;

CREATE SEQUENCE results_id_sequence
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE results_id_sequence OWNER TO postgres;
