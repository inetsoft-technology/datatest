# Crosscheck: VS_CalcField_Spec.groovy

23 viewsheets in zip, 22 spec features with a viewsheet

Viewsheets with NO spec feature: ['vs-calcfield/aggregate_trendcomparison']
Spec features whose viewsheet is NOT in the zip: none

| feature | baseline dir | txt | png | calc fields | in baseline txt | not in baseline |
|---|---|---|---|---|---|---|
| aggregate_date | yes | 8 | 5 | 4 | 4/4 |  |
| aggregate_discrete | yes | 6 | 7 | 4 | 4/4 |  |
| aggregate_distinct count | yes | 3 | 1 | 2 | 2/2 |  |
| aggregate_nthpth | yes | 6 | 3 | 8 | 8/8 |  |
| aggregate_time | yes | 8 | 3 | 1 | 1/1 |  |
| aggregate_with | yes | 8 | 5 | 7 | 7/7 |  |
| calc field1 | yes | 12 | 7 | 8 | 8/8 |  |
| calc_data group | yes | 6 | 3 | 6 | 3/6 | CalcField2, Calc_mindate, calc_date |
| calc_drillfilter | yes | 8 | 2 | 2 | 2/2 |  |
| chart | yes | 12 | 12 | 2 | 2/2 |  |
| donut chart | yes | 2 | 3 | 3 | 3/3 |  |
| filter | yes | 2 | 1 | 5 | 4/5 | max_time |
| funnel chart | yes | 4 | 5 | 4 | 4/4 |  |
| gantt chart | yes | 4 | 5 | 4 | 4/4 |  |
| parameter in calc field | yes | 3 | 1 | 2 | 2/2 |  |
| parent-child tree | yes | 2 | 1 | 1 | 0/1 | CalcField1 |
| testDB2 | yes | 5 | 3 | 6 | 5/6 | c_date |
| testModel | yes | 3 | 2 | 7 | 6/7 | NthSmallest |
| testQuery | yes | 6 | 2 | 8 | 7/8 | edate |
| testWorksheet | yes | 8 | 3 | 4 | 4/4 |  |
| tree chart | yes | 4 | 5 | 5 | 5/5 |  |
| treemap chart | yes | 4 | 5 | 2 | 2/2 |  |

"not in baseline" is a substring match: a calc field used only as a condition, or shown under another header, is a false positive. Confirm by hand.
