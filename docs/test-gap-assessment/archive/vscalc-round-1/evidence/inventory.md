# Inventory: vscalc-storage.zip

23 viewsheets; 40 bookmarks in 19 bookmark stores (bookmarks hold saved selection state)

| viewsheet | assemblies | by type | calc fields | bound table |
|---|---|---|---|---|
| vs-calcfield/aggregate_date | 7 | CalcTable 1, Calendar 1, Chart 2, Crosstab 1, CurrentSelection 1, RadioButton 1 | 4 | OrdersAndReturns |
| vs-calcfield/aggregate_discrete | 5 | Calendar 1, Chart 3, TimeSlider 1 | 4 | Order Model |
| vs-calcfield/aggregate_distinct count | 3 | CalcTable 1, Chart 1, Crosstab 1 | 2 | Order Model |
| vs-calcfield/aggregate_nthpth | 4 | CalcTable 1, Chart 1, Crosstab 1, CurrentSelection 1 | 8 | Query1 |
| vs-calcfield/aggregate_time | 5 | CalcTable 1, Chart 1, Crosstab 2, CurrentSelection 1 | 1 | Query2, type |
| vs-calcfield/aggregate_trendcomparison | 3 | Chart 1, Crosstab 1, TimeSlider 1 | 3 | Query1 |
| vs-calcfield/aggregate_with | 5 | CalcTable 1, Calendar 1, Chart 2, Crosstab 1 | 7 | query1 |
| vs-calcfield/calc field1 | 12 | CalcTable 1, Chart 3, Crosstab 2, SelectionList 2, SelectionTree 1, Text 3 | 8 | OrdersAndReturns |
| vs-calcfield/calc_data group | 4 | Chart 1, Crosstab 2, CurrentSelection 1 | 6 | OrdersAndReturns |
| vs-calcfield/calc_drillfilter | 4 | CalcTable 1, Chart 1, Crosstab 1, Table 1 | 2 | Order Model |
| vs-calcfield/chart | 4 | Chart 4 | 2 | OrdersAndReturns |
| vs-calcfield/donut chart | 4 | Chart 1, CurrentSelection 1, SelectionList 2 | 3 | CUSTOMERS |
| vs-calcfield/filter | 6 | Calendar 1, SelectionList 1, SelectionTree 1, Table 1, TimeSlider 2 | 5 | type |
| vs-calcfield/funnel chart | 6 | Chart 2, CurrentSelection 1, SelectionTree 1, TimeSlider 2 | 4 | Order Model |
| vs-calcfield/gantt chart | 4 | Calendar 1, Chart 2, SelectionList 1 | 4 | Query1 |
| vs-calcfield/parameter in calc field | 5 | CheckBox 1, RadioButton 1, SelectionList 1, SelectionTree 1, Table 1 | 2 | customers |
| vs-calcfield/parent-child tree | 3 | SelectionTree 2, Table 1 | 1 | Query1 |
| vs-calcfield/testDB2 | 14 | CalcTable 1, Chart 2, Crosstab 1, CurrentSelection 1, Gauge 1, SelectionList 1, Table 1, TimeSlider 6 | 6 | VsDataModel |
| vs-calcfield/testModel | 5 | CalcTable 1, Chart 1, Crosstab 1, Gauge 1, Text 1 | 7 | VsDataModel |
| vs-calcfield/testQuery | 8 | CalcTable 3, Chart 1, Crosstab 1, Gauge 1, Table 1, Text 1 | 8 | VsDataQuery |
| vs-calcfield/testWorksheet | 6 | CalcTable 1, Chart 1, Crosstab 1, Gauge 1, SelectionList 1, Table 1 | 4 | SQL Query1, snapshot |
| vs-calcfield/tree chart | 7 | Chart 2, CurrentSelection 1, SelectionList 2, TimeSlider 2 | 5 | Query1 |
| vs-calcfield/treemap chart | 5 | Calendar 1, Chart 2, CurrentSelection 1, TimeSlider 1 | 2 | Query1 |

Assembly totals: Chart 34, Crosstab 16, TimeSlider 15, CalcTable 13, SelectionList 11, CurrentSelection 9, Table 7, Calendar 6, SelectionTree 6, Text 5, Gauge 4, RadioButton 2, CheckBox 1

`<script` elements: 0

## Calc fields: kind x dataType (98 definitions)

| kind | boolean | date | double | integer | string | time | timeInstant | total |
|---|---|---|---|---|---|---|---|---|
| aggregate | 0 | 6 | 25 | 5 | 3 | 1 | 0 | 40 |
| detail | 1 | 13 | 9 | 11 | 18 | 2 | 4 | 58 |

kind: `detail` = no baseOnDetail attribute (row-level); `aggregate` = baseOnDetail="false".

## Calc fields: expression class x kind

| expression class | aggregate | detail |
|---|---|---|
| agg-fn | 37 | 0 |
| date-fn(CALC.) | 0 | 7 |
| datepart-fn | 0 | 3 |
| field-ref | 0 | 36 |
| other | 3 | 9 |
| string-fn | 0 | 3 |

## Usage position -> viewsheets covering it (of 23)

| assembly:position | viewsheets | calc dataTypes seen |
|---|---|---|
| CalcTable:aggr | 9 | {'date': 4, 'double': 16, 'string': 2, 'integer': 12} |
| CalcTable:cell-binding | 9 | {'date': 5, 'double': 10, 'string': 6, 'integer': 6, 'timeInstant': 2} |
| CalcTable:condition | 1 | {'date': 1, 'string': 1} |
| CalcTable:dim | 8 | {'date': 4, 'string': 6, 'time': 1, 'timeInstant': 2} |
| Calendar:bound | 6 | {'date': 5, 'timeInstant': 1} |
| Chart:axis-format | 13 | {'date': 3, 'double': 11, 'string': 2, 'integer': 4} |
| Chart:bound-aggr | 19 | {'date': 9, 'double': 32, 'string': 3, 'integer': 13} |
| Chart:bound-dim | 18 | {'date': 12, 'string': 16, 'time': 1, 'timeInstant': 3, 'integer': 2} |
| Chart:other | 4 | {'date': 4, 'string': 3} |
| Chart:visual:color | 7 | {'double': 2, 'date': 3, 'string': 3} |
| Chart:visual:shape | 2 | {'string': 1, 'double': 1} |
| Chart:visual:size | 4 | {'double': 5} |
| Chart:visual:text | 4 | {'date': 2, 'double': 1, 'string': 1} |
| Chart:x | 14 | {'date': 6, 'double': 5, 'string': 6, 'time': 1, 'timeInstant': 2, 'integer': 4} |
| Chart:y | 16 | {'date': 3, 'double': 18, 'string': 4, 'integer': 8} |
| Crosstab:aggr | 12 | {'date': 5, 'double': 21, 'string': 3, 'integer': 14} |
| Crosstab:condition | 3 | {'date': 3, 'string': 1} |
| Crosstab:dim | 12 | {'date': 7, 'string': 11, 'time': 1, 'timeInstant': 3, 'integer': 1} |
| Crosstab:other | 10 | {'double': 1, 'date': 4, 'time': 1, 'string': 7, 'integer': 2, 'timeInstant': 2} |
| Gauge:bindingInfo | 3 | {'integer': 4} |
| SelectionList:bound | 6 | {'integer': 4, 'double': 2, 'date': 1, 'string': 3, 'time': 2} |
| SelectionList:title-text | 1 | {'integer': 1} |
| SelectionTree:bound | 4 | {'date': 1, 'string': 2, 'double': 1} |
| SelectionTree:dataRefs | 2 | {'integer': 1, 'string': 2, 'date': 1} |
| Table:column | 6 | {'timeInstant': 3, 'string': 4, 'boolean': 1, 'time': 1, 'integer': 9} |
| Table:condition | 2 | {'string': 1, 'timeInstant': 1} |
| Text:bindingInfo | 3 | {'double': 1, 'date': 1, 'string': 1, 'integer': 2} |
| TimeSlider:timeInfo | 3 | {'timeInstant': 1, 'boolean': 1, 'time': 1, 'double': 1, 'date': 1, 'integer': 1} |
| TimeSlider:title-text | 2 | {'double': 1, 'date': 1, 'integer': 1} |

Defined but not referenced by any assembly: 0 []

Components NOT exported by ViewsheetTest.executeVS0 (check the module base class): Calendar, CheckBox, CurrentSelection, Gauge, RadioButton, SelectionList, SelectionTree, Text, TimeSlider
