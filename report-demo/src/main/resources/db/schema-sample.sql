-- 演示宿主业务表：订单主表 + 明细表（用于 MAIN 主数据源 SQL 数据集）
-- H2 的 CREATE TABLE 不支持内联 COMMENT，注释统一用 COMMENT ON
CREATE TABLE IF NOT EXISTS sample_order (
    order_no   VARCHAR(32) PRIMARY KEY,
    customer_name VARCHAR(64),
    order_date DATE,
    status     VARCHAR(16)
);

CREATE TABLE IF NOT EXISTS sample_order_item (
    item_id     INT PRIMARY KEY AUTO_INCREMENT,
    order_no    VARCHAR(32),
    product_name VARCHAR(128),
    qty         INT,
    price       DECIMAL(10, 2),
    amount      DECIMAL(10, 2)
);

COMMENT ON TABLE sample_order IS '演示宿主业务表：销售订单主表';
COMMENT ON COLUMN sample_order.order_no IS '订单号，主键';
COMMENT ON COLUMN sample_order.customer_name IS '客户名称';
COMMENT ON COLUMN sample_order.order_date IS '下单日期';
COMMENT ON COLUMN sample_order.status IS '订单状态：已发货/待发货/已完成等';

COMMENT ON TABLE sample_order_item IS '演示宿主业务表：销售订单明细';
COMMENT ON COLUMN sample_order_item.item_id IS '明细主键，自增';
COMMENT ON COLUMN sample_order_item.order_no IS '所属订单号，关联 sample_order.order_no';
COMMENT ON COLUMN sample_order_item.product_name IS '商品名称';
COMMENT ON COLUMN sample_order_item.qty IS '数量';
COMMENT ON COLUMN sample_order_item.price IS '单价（元）';
COMMENT ON COLUMN sample_order_item.amount IS '金额（元）= 数量 × 单价';
