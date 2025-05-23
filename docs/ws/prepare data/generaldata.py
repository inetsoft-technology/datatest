import csv
import random
from datetime import datetime, timedelta, date, time
import calendar


def generate_time_boundaries():
    base_date = datetime(2025, 2, 15)
    boundaries = []

    def add_boundary(name, dt):
        boundaries.append(dt.replace(hour=0, minute=0, second=0))
        boundaries.append(dt.replace(hour=23, minute=59, second=59))

    # Year boundaries
    add_boundary("Last year", base_date - timedelta(days=365))
    add_boundary("This year", base_date.replace(month=1, day=1))

    # Quarter boundaries
    quarters = [
        (1, (1, 3)), (2, (4, 6)), (3, (7, 9)), (4, (10, 12))
    ]
    for q, months in quarters:
        add_boundary(f"{q}st quarter this year", datetime(2025, months[0], 1))
        add_boundary(f"{q}st quarter last year", datetime(2024, months[0], 1))

    # Half year boundaries
    add_boundary("1st half this year", datetime(2025, 1, 1))
    add_boundary("2nd half this year", datetime(2025, 7, 1))
    add_boundary("1st half last year", datetime(2024, 1, 1))
    add_boundary("2nd half last year", datetime(2024, 7, 1))

    # Month boundaries
    for month in range(1, 13):
        add_boundary(f"This {calendar.month_name[month]}", datetime(2025, month, 1))
        add_boundary(f"Last {calendar.month_name[month]}", datetime(2024, month, 1))

    # Week/day boundaries
    add_boundary("This week", base_date - timedelta(days=base_date.weekday()))
    add_boundary("Last week", base_date - timedelta(days=base_date.weekday() + 7))
    add_boundary("Today", base_date)
    add_boundary("Tomorrow", base_date + timedelta(days=1))
    add_boundary("Yesterday", base_date - timedelta(days=1))

    # Date ranges
    for months in [1, 3, 6, 12, 18, 24, 36, 48, 60, 72, 84, 96, 108, 120]:
        add_boundary(f"Last {months} months", base_date - timedelta(days=30 * months))

    return boundaries


def generate_data():
    boundaries = generate_time_boundaries()
    data = []

    for _ in range(500):
        if random.random() < 0.3 and boundaries:
            order_datetime = random.choice(boundaries)
        else:
            start_date = datetime(2020, 1, 1)
            end_date = datetime(2025, 2, 15)
            random_days = random.randint(0, (end_date - start_date).days)
            random_seconds = random.randint(0, 86399)
            order_datetime = start_date + timedelta(days=random_days, seconds=random_seconds)

        data.append([
            order_datetime,
            order_datetime.date(),
            order_datetime.time(),
            random.randint(1, 35),
            round(random.uniform(0.00, 0.25), 2),
            random.randint(1, 4),
            random.randint(12682, 12700),
            random.choice([True, False])
        ])

    return data


def save_to_csv(data, filename='orders_data.csv'):
    headers = [
        'order_datetime', 'order_date', 'order_time',
        'customer_id', 'discount', 'employee_id',
        'order_id', 'paid'
    ]

    with open(filename, 'w', newline='') as f:
        writer = csv.writer(f)
        writer.writerow(headers)
        writer.writerows(data)


if __name__ == '__main__':
    data = generate_data()
    save_to_csv(data)
    print("订单数据已生成到orders_data.csv")
