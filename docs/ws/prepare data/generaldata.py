import csv
import random
from datetime import datetime, timedelta
from itertools import chain

# 设置当前日期为2025-02-15
current_date = datetime(2025, 2, 15)

# 定义所有边界条件的时间范围
boundary_conditions = {
    # 年度条件
    "Last year": (datetime(2024, 1, 1), datetime(2024, 12, 31, 23, 59, 59)),
    "This year": (datetime(2025, 1, 1), datetime(2025, 12, 31, 23, 59, 59)),
    
    # 季度条件
    "This quarter": (datetime(2025, 1, 1), datetime(2025, 3, 31, 23, 59, 59)),
    "Last quarter": (datetime(2024, 10, 1), datetime(2024, 12, 31, 23, 59, 59)),
    "This quarter last year": (datetime(2024, 1, 1), datetime(2024, 3, 31, 23, 59, 59)),
    "Last quarter last year": (datetime(2023, 10, 1), datetime(2023, 12, 31, 23, 59, 59)),
    
    # 指定季度
    "1st quarter this year": (datetime(2025, 1, 1), datetime(2025, 3, 31, 23, 59, 59)),
    "2nd quarter this year": (datetime(2025, 4, 1), datetime(2025, 6, 30, 23, 59, 59)),
    "3rd quarter this year": (datetime(2025, 7, 1), datetime(2025, 9, 30, 23, 59, 59)),
    "4th quarter this year": (datetime(2025, 10, 1), datetime(2025, 12, 31, 23, 59, 59)),
    "1st quarter last year": (datetime(2024, 1, 1), datetime(2024, 3, 31, 23, 59, 59)),
    "2nd quarter last year": (datetime(2024, 4, 1), datetime(2024, 6, 30, 23, 59, 59)),
    "3rd quarter last year": (datetime(2024, 7, 1), datetime(2024, 9, 30, 23, 59, 59)),
    "4th quarter last year": (datetime(2024, 10, 1), datetime(2024, 12, 31, 23, 59, 59)),
    
    # 半年度条件
    "1st half of this year": (datetime(2025, 1, 1), datetime(2025, 6, 30, 23, 59, 59)),
    "2nd half of this year": (datetime(2025, 7, 1), datetime(2025, 12, 31, 23, 59, 59)),
    "1st half of last year": (datetime(2024, 1, 1), datetime(2024, 6, 30, 23, 59, 59)),
    "2nd half of last year": (datetime(2024, 7, 1), datetime(2024, 12, 31, 23, 59, 59)),
    
    # 月度条件
    "This month": (datetime(2025, 2, 1), datetime(2025, 2, 28, 23, 59, 59)),
    "Last month": (datetime(2025, 1, 1), datetime(2025, 1, 31, 23, 59, 59)),
    "This month last year": (datetime(2024, 2, 1), datetime(2024, 2, 29, 23, 59, 59)),
    "Last month last year": (datetime(2024, 1, 1), datetime(2024, 1, 31, 23, 59, 59)),
    
    # 特定月份
    "This January": (datetime(2025, 1, 1), datetime(2025, 1, 31, 23, 59, 59)),
    "This February": (datetime(2025, 2, 1), datetime(2025, 2, 28, 23, 59, 59)),
    # 其他月份类似...
    
    # 其他条件
    "Today": (datetime(2025, 2, 15, 0, 0, 0), datetime(2025, 2, 15, 23, 59, 59)),
    "Yesterday": (datetime(2025, 2, 14, 0, 0, 0), datetime(2025, 2, 14, 23, 59, 59)),
    "Tomorrow": (datetime(2025, 2, 16, 0, 0, 0), datetime(2025, 2, 16, 23, 59, 59)),
    
    # 可以继续添加其他条件...
}

# 生成每个月的第一天、最后一天和15日的边界时间点
def generate_month_boundaries(start_year, end_year):
    boundaries = []
    for year in range(start_year, end_year + 1):
        for month in range(1, 13):
            # 每月的第一天
            boundaries.append(datetime(year, month, 1, 0, 0, 0))
            boundaries.append(datetime(year, month, 1, 23, 59, 59))
            
            # 每月的15日
            boundaries.append(datetime(year, month, 15, 0, 0, 0))
            boundaries.append(datetime(year, month, 15, 23, 59, 59))
            
            # 每月的最后一天
            if month == 12:
                next_month = 1
                next_year = year + 1
            else:
                next_month = month + 1
                next_year = year
            
            last_day = (datetime(next_year, next_month, 1) - timedelta(days=1)).day
            boundaries.append(datetime(year, month, last_day, 0, 0, 0))
            boundaries.append(datetime(year, month, last_day, 23, 59, 59))
    
    return boundaries

# 生成边界时间点
boundary_dates = generate_month_boundaries(2024, 2025)

# 为每个边界条件添加开始和结束时间
for condition, (start, end) in boundary_conditions.items():
    boundary_dates.extend([start, end])

# 去重并排序
boundary_dates = sorted(list(set(boundary_dates)))

# 生成随机日期时间填充剩余数据
def generate_random_datetimes(count, start_date, end_date):
    random_dates = []
    time_diff = end_date - start_date
    for _ in range(count):
        random_seconds = random.randint(0, int(time_diff.total_seconds()))
        random_dates.append(start_date + timedelta(seconds=random_seconds))
    return random_dates

# 生成随机日期时间
start_date = datetime(2024, 1, 1)
end_date = datetime(2025, 12, 31, 23, 59, 59)
remaining_count = 500 - len(boundary_dates)
random_dates = generate_random_datetimes(remaining_count, start_date, end_date)

# 合并边界日期和随机日期
all_dates = boundary_dates + random_dates
random.shuffle(all_dates)  # 打乱顺序

# 确保总数为500
all_dates = all_dates[:500]

# 准备数据
data = []
for dt in all_dates:
    data.append({
        "order_datetime": dt,
        "order_date": dt.date(),
        "order_time": dt.time()
    })

# 写入CSV文件
csv_file = "order_dates.csv"
with open(csv_file, mode='w', newline='') as file:
    writer = csv.DictWriter(file, fieldnames=["order_datetime", "order_date", "order_time"])
    writer.writeheader()
    writer.writerows(data)

print(f"数据已成功写入 {csv_file}")
