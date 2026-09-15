ALTER TABLE tb_report_datasource
  ADD COLUMN url VARCHAR(512) NULL;

ALTER TABLE tb_report_datasource
  ADD COLUMN driver_class_name VARCHAR(128) NULL;

ALTER TABLE tb_report_dataset
  MODIFY COLUMN fields LONGTEXT;

ALTER TABLE tb_report_dataset
  MODIFY COLUMN json_text LONGTEXT;
