-- 演示数据（幂等：重启不重复插入）
INSERT INTO sample_order(order_no, customer_name, order_date, status)
SELECT * FROM (VALUES
  ('SO-2026-0801', '华南医药集团', '2026-08-01', '已发货'),
  ('SO-2026-0802', '华东器械贸易', '2026-08-02', '待发货'),
  ('SO-2026-0803', '华北连锁药房', '2026-08-03', '已签收')
) AS v(order_no, customer_name, order_date, status)
WHERE NOT EXISTS (SELECT 1 FROM sample_order WHERE order_no = v.order_no);

INSERT INTO sample_order_item(order_no, product_name, qty, price, amount)
SELECT * FROM (VALUES
  ('SO-2026-0801', '阿莫西林胶囊 0.25g*24', 200, 8.50, 1700.00),
  ('SO-2026-0801', '布洛芬缓释胶囊 0.3g*20', 300, 12.00, 3600.00),
  ('SO-2026-0802', '一次性医用外科口罩', 1000, 0.35, 350.00),
  ('SO-2026-0802', '医用防护服 L', 120, 68.00, 8160.00),
  ('SO-2026-0802', '红外额温枪', 60, 89.90, 5394.00),
  ('SO-2026-0803', '维生素C咀嚼片 100片', 500, 22.50, 11250.00)
) AS v(order_no, product_name, qty, price, amount)
WHERE NOT EXISTS (
  SELECT 1 FROM sample_order_item WHERE order_no = v.order_no AND product_name = v.product_name
);
