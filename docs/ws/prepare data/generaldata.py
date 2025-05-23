import csv
import random
from datetime import datetime, timedelta, time
from dateutil.relativedelta import relativedelta


def get_boundary_dates(current_date):
    """生成所有时间边界日期"""
    boundaries = []

    # 年相关
    boundaries.append(('Last year', current_date - relativedelta(years=1)))
    boundaries.append(('This year', current_date.replace(month=1, day=1)))

    # 季度相关
    current_quarter = (current_date.month - 1) // 3 + 1
    boundaries.append(('This quarter', datetime(current_date.year, 3 * (current_quarter - 1) + 1, 1)))
    boundaries.append(('Last quarter', (current_date - relativedelta(months=3)).replace(day=1)))

    # 季度历史数据
    for q in range(1, 5):
        boundaries.append((f'{q}st quarter this year', datetime(current_date.year, 3 * (q - 1) + 1, 1)))
        boundaries.append((f'{q}st quarter last year', datetime(current_date.year - 1, 3 * (q - 1) + 1, 1)))

    # 半年相关
    boundaries.append(('1st half of this year', datetime(current_date.year, 1, 1)))
    boundaries.append(('2nd half of this year', datetime(current_date.year, 7, 1)))
    boundaries.append(('1st half of last year', datetime(current_date.year - 1, 1, 1)))
    boundaries.append(('2nd half of last year', datetime(current_date.year - 1, 7, 1)))

    # 月相关
    boundaries.append(('This month', current_date.replace(day=1)))
    boundaries.append(('Last month', (current_date.replace(day=1) - timedelta(days=1)).replace(day=1)))

    # 月份历史数据
    for m in range(1, 13):
        boundaries.append((f'This {datetime(1900, m, 1).strftime("%B")}', datetime(current_date.year, m, 1)))
        boundaries.append((f'Last {datetime(1900, m, 1).strftime("%B")}', datetime(current_date.year - 1, m, 1)))

    # 时间范围
    for months in [1, 3, 6, 12, 18, 24, 36, 48, 60, 72, 84, 96, 108, 120]:
        boundaries.append((f'Last {months} months', current_date - relativedelta(months=months)))

    # 周相关
    boundaries.append(('This week', current_date - timedelta(days=current_date.weekday())))
    boundaries.append(('Last week', current_date - timedelta(days=current_date.weekday() + 7)))
    boundaries.append(('Week before last week', current_date - timedelta(days=current_date.weekday() + 14)))
    boundaries.append(('Last 4 weeks', current_date - timedelta(weeks=4)))

    # 日相关
    boundaries.append(('Today', current_date))
    boundaries.append(('Yesterday', current_date - timedelta(days=1)))
    boundaries.append(('Tomorrow', current_date + timedelta(days=1)))

    # 累计日期
    boundaries.append(('Year to date', datetime(current_date.year, 1, 1)))
    boundaries.append(('Year to date last year', datetime(current_date.year - 1, 1, 1)))
    boundaries.append(('Quarter to date', datetime(current_date.year, 3 * (current_quarter - 1) + 1, 1)))
    boundaries.append(('Quarter to date last year', datetime(current_date.year - 1, 3 * (current_quarter - 1) + 1, 1)))
    boundaries.append(('Month to date', current_date.replace(day=1)))
    boundaries.append(('Month to date last year', (current_date - relativedelta(years=1)).replace(day=1)))

    return boundaries


def generate_order_data():
    current_date = datetime(2025, 2, 15)
    boundary_dates = get_boundary_dates(current_date)
    data = []

    # 添加边界数据
    for name, dt in boundary_dates:
        data.append({
            'order_datetime': dt,
            'order_date': dt.date(),
            'order_time': dt.time(),
            'customer_id': random.randint(1, 35),
            'discount': round(random.uniform(0, 0.25), 2),
            'employee_id': random.randint(1, 4),
            'order_id': random.randint(12682, 12700),
            'paid': random.choice([True, False])
        })

    # 补充随机数据至500行
    while len(data) < 500:
        days_offset = random.randint(-365 * 5, 365)
        time_offset = timedelta(
            hours=random.randint(0, 23),
            minutes=random.randint(0, 59),
            seconds=random.randint(0, 59)
        )

        dt = current_date + timedelta(days=days_offset) + time_offset

        data.append({
            'order_datetime': dt,
            'order_date': dt.date(),
            'order_time': dt.time(),
            'customer_id': random.randint(1, 35),
            'discount': round(random.uniform(0, 0.25), 2),
            'employee_id': random.randint(1, 4),
            'order_id': random.randint(12682, 12700),
            'paid': random.choice([True, False])
        })

    return data


def save_to_csv(data, filename='orders.csv'):
    with open(filename, 'w', newline='') as f:
        writer = csv.DictWriter(f, fieldnames=[
            'order_datetime', 'order_date', 'order_time',
            'customer_id', 'discount', 'employee_id',
            'order_id', 'paid'
        ])
        writer.writeheader()
        writer.writerows(data)


if __name__ == '__main__':
    order_data = generate_order_data()
    save_to_csv(order_data)
    print(f"已生成包含{len(order_data)}条订单数据的CSV文件")
