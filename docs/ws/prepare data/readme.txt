Inrange case数据生成
一.在mysql数据库orders1下创建表order2

CREATE TABLE orders2 (
    order_datetime DATETIME,
    order_date DATE,
    order_time TIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

二.用ai生成数据
1.用python原生类型生成8列500行的数据。
2.一列名字是order_datetime，类型是timeinstan类型。当前日期是2025-2-15日，生成数据包含以下计算的边界值Last year, This year, This quarter, Last quarter, This quarter last year, Last quarter last year, 1st quarter this year, 2nd quarter this year, 3rd quarter this year, 4th quarter this year, 1st quarter last year, 2nd quarter last year, 3rd quarter last year, 4th quarter last year, 1st half of this year, 2nd half of this year, 1st half of last year, 2nd half of last year, This month, Last month, Last 1 months, Last 3 months, Last 6 months, Last 12 months, Last 18 months, Last 24 months, Last 36 months, Last 48 months, Last 60 months, Last 72 months, Last 84 months, Last 96 months, Last 108 months, Last 120 months, This month last year, Last month last year, This January, This February, This March, This April, This May, This June, This July, This August, This September, This October, This November, This December, Last January, Last February, Last March, Last April, Last May, Last June, Last July, Last August, Last September, Last October, Last November, Last December, This week, Last week, Week before last week, Last 4 weeks, Last 5-8 weeks, Last 7 days, Last 8-14 days, Last 30 days, Last 31-60 days, Today, Tomorrow, Yesterday, Year to date, Year to date last year, Quarter to date, Quarter to date last year, Quarter to date last quarter, Month to date, Month to date last year, Month to date last month
3.一列名字是order_date，类型是date类型。order_date列的数据是order_datetime的日期部分
4.一列名字是order_time，类型是time类型,order_time是order_datetime的时间部分
5.一列名字是customer_id，数据类型是int类型，数据范围是1到35
6.一列名字是discount，数据类型是double类型，数据范围0.00到0.25
7.一列名字是employee_id，数据类型是int类型，数据范围是1-4
8.一列名字是order_id，数据类型是int类型，数据范围是12682-12700
9.一列名字是paid，数据类型是boolean类型
10.输出到csv

三.导入到mysql数据库order2表






