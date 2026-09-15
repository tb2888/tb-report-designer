-- 演示用「外部业务库」脚本：由 H2 的 INIT=RUNSCRIPT 在每条连接建立时执行。
-- 必须幂等（重启/多次连接不能重复插数据），因此统一用 WHERE NOT EXISTS 兜底。

CREATE TABLE IF NOT EXISTS biz_region_sales (
  region VARCHAR(64),
  manager VARCHAR(64),
  amount DECIMAL(14,2),
  stat_month VARCHAR(16)
);

COMMENT ON TABLE biz_region_sales IS '演示外部业务库：各大区销售业绩';
COMMENT ON COLUMN biz_region_sales.region IS '大区名称';
COMMENT ON COLUMN biz_region_sales.manager IS '负责人';
COMMENT ON COLUMN biz_region_sales.amount IS '销售额（元）';
COMMENT ON COLUMN biz_region_sales.stat_month IS '统计月份，格式 yyyy-MM';

INSERT INTO biz_region_sales(region, manager, amount, stat_month)
SELECT * FROM (
  SELECT '华南' AS region, '谭波' AS manager, 128000.00 AS amount, '2026-08' AS stat_month
  UNION ALL SELECT '华东', '李静', 96000.00, '2026-08'
  UNION ALL SELECT '华北', '王强', 78000.00, '2026-08'
  UNION ALL SELECT '西南', '赵敏', 45200.00, '2026-08'
) t
WHERE NOT EXISTS (SELECT 1 FROM biz_region_sales);
