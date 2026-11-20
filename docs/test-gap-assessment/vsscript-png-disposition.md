# vsscript 旧 PNG 用例的处置记录

范围决定（vsscript-upgrade-plan §2、§3）：D4（旧用例改成状态断言）和 D9（迁移到 render/）放弃，旧 PNG 用例**全部保留原样，没有删除、没有改动**（唯一的改动是比对清单的修正，见末尾）。
下表逐个列出：用例、所在 spec、用的 viewsheet、比对的图片数、处置、该 viewsheet 是否另有 state 用例（手工用例自动化、导出/写回总检等）。

处置说明：**保留**＝继续作为渲染回归（像素），脚本效果的状态断言见“另有 state 用例”一列；这些用例在本机 114/115 失败（渲染环境），以 CI 基线为准，CI 还没有跑到（见计划 §7）。

| # | spec | 用例 | viewsheet | 图片数 | 处置 | 另有 state 用例 |
|---|---|---|---|---|---|---|
| 1 | Bugs_Spec | TestCase-EGraphBind1 | ScriptAuto/Chart/EgraphBinding | 2 | 保留 | 否 |
| 2 | Bugs_Spec | TestCase-PointChart | ScriptAuto/Chart/PointChart | 1 | 保留 | 否 |
| 3 | Bugs_Spec | TestCase-SetShapeText | ScriptAuto/Chart/EgraphBinding | 2 | 保留 | 否 |
| 4 | Bugs_Spec | TestCase_FreehandDataByScript | ScriptAuto/SpecialScript/FreehandFormula | 1 | 保留 | 是（FreehandFormula） |
| 5 | Bugs_Spec | TestCase_ArrayIncludes | ScriptAuto/SpecialScript/ArraysIncludes | 1 | 保留 | 否 |
| 6 | ChartBindingInfo_Spec | TestCase-BandingField | ScriptAuto/Chart/ChartBandingInfo | 2 | 保留 | 是（ChartBandingInfo） |
| 7 | ChartBindingInfo_Spec | TestCase-MapChart | ScriptAuto/Chart/MapBinding | 2 | 保留 | 否 |
| 8 | ChartBindingInfo_Spec | TestCase-StockingAndCandel | ScriptAuto/Chart/StockingBanding | 2 | 保留 | 否 |
| 9 | ChartBindingInfo_Spec | TestCase-ChartFrame | ScriptAuto/Chart/ChartFrame | 4 | 保留 | 否 |
| 10 | ChartBindingInfo_Spec | TestCase-GroupInfo | ScriptAuto/Chart/ChartBandingInfo | 1 | 保留 | 是（ChartBandingInfo） |
| 11 | ChartBindingInfo_Spec | TestCase-AggregateInfo | ScriptAuto/Chart/AggregateInfo | 2 | 保留 | 否 |
| 12 | ChartBindingInfo_Spec | TestCase-Discreate | ScriptAuto/Chart/SetDiscrete | 1 | 保留 | 否 |
| 13 | ChartBindingInfo_Spec | TestCase-getFunctions | ScriptAuto/Chart/getFunctions | 1 | 保留 | 否 |
| 14 | ChartBindingInfo_Spec | TestCase-EGraph1 | ScriptAuto/Chart/EGraph1 | 2 | 保留 | 否 |
| 15 | ChartBindingInfo_Spec | TestCase-EGraph2 | ScriptAuto/Chart/EGraph1 | 3 | 保留 | 否 |
| 16 | ChartBindingInfo_Spec | TestCase-EGraph3 | ScriptAuto/Chart/EGraph2 | 2 | 保留 | 否 |
| 17 | ChartBindingInfo_Spec | TestCase-EGraph4 | ScriptAuto/Chart/EGraph1 | 2 | 保留 | 否 |
| 18 | ChartBindingInfo_Spec | TestCase_ShapseApply | ScriptAuto/Chart/ShapseApply | 1 | 保留 | 否 |
| 19 | ChartProperties_Spec | TestCase-Chart_Property | ScriptAuto/Chart/title | 1 | 保留 | 否 |
| 20 | ChartProperties_Spec | TestCase-Chart_AxisProperty | ScriptAuto/Chart/AxisProperty | 3 | 保留 | 否 |
| 21 | ChartProperties_Spec | TestCase-Chart_AxisProperty2 | ScriptAuto/Chart/AllTitleProperties | 2 | 保留 | 否 |
| 22 | ChartProperties_Spec | TestCase-TimeAxisProperty | ScriptAuto/Chart/TimeAxis | 2 | 保留 | 否 |
| 23 | ChartProperties_Spec | TestCase-SharedRange | ScriptAuto/Chart/ShareRange | 1 | 保留 | 否 |
| 24 | ChartProperties_Spec | TestCase-Chart_VauleProperty | ScriptAuto/Chart/ValueProerty | 2 | 保留 | 否 |
| 25 | ChartProperties_Spec | TestCase-Chart_TitleProperty | ScriptAuto/Chart/AllTitleProperties | 2 | 保留 | 否 |
| 26 | ChartProperties_Spec | TestCase-Chart_LengendProperty | ScriptAuto/Chart/LengendProperty | 3 | 保留 | 否 |
| 27 | ChartProperties_Spec | TestCase-Chart_LengendsProperty | ScriptAuto/Chart/LengendsProperty | 3 | 保留 | 否 |
| 28 | ChartProperties_Spec | TestCase-Chart_Targetline1 | ScriptAuto/Chart/AddTarget | 4 | 保留 | 否 |
| 29 | ChartProperties_Spec | TestCase-Chart_Targetline2 | ScriptAuto/Chart/AddTarget | 3 | 保留 | 否 |
| 30 | ChartProperties_Spec | TestCase-Chart_LengendAlias | ScriptAuto/Chart/LengendAlias | 1 | 保留 | 否 |
| 31 | ChartProperties_Spec | TestCase-Chart_PlotProperty | ScriptAuto/Chart/PlotProperty | 6 | 保留 | 否 |
| 32 | ChartProperties_Spec | TestCase-Chart_PlotProperty2 | ScriptAuto/Chart/PlotProperty2 | 4 | 保留 | 否 |
| 33 | ChartProperties_Spec | TestCase-Chart_WebMap | ScriptAuto/Chart/webMap | 3 | 保留 | 否 |
| 34 | ChartProperties_Spec | TestCase-Chart_FillBlank | ScriptAuto/Chart/LineChart | 1 | 保留 | 否 |
| 35 | Egraph_Spec | TestCase-GraphElement1 | ScriptAuto/Chart/EGraph1 | 3 | 保留 | 否 |
| 36 | Egraph_Spec | TestCase-GraphElement2 | ScriptAuto/Chart/EGraph1 | 2 | 保留 | 否 |
| 37 | Egraph_Spec | TestCase-IntervalElement | ScriptAuto/Chart/EGraph1 | 2 | 保留 | 否 |
| 38 | Egraph_Spec | TestCase-LineElement | ScriptAuto/Chart/EGraph1 | 2 | 保留 | 否 |
| 39 | Egraph_Spec | TestCase-SchemaElement | ScriptAuto/Chart/EGraph1 | 2 | 保留 | 否 |
| 40 | Egraph_Spec | TestCase-LogScale | ScriptAuto/Chart/EGraph1 | 1 | 保留 | 否 |
| 41 | Egraph_Spec | TestCase-CategoricalScale | ScriptAuto/Chart/EGraph1 | 1 | 保留 | 否 |
| 42 | Egraph_Spec | TestCase-LinearScale | ScriptAuto/Chart/EGraph1 | 2 | 保留 | 否 |
| 43 | Egraph_Spec | TestCase-coordinateScale | ScriptAuto/Chart/EGraph2 | 3 | 保留 | 否 |
| 44 | Egraph_Spec | TestCase-PolarCood | ScriptAuto/Chart/EGraph1 | 1 | 保留 | 否 |
| 45 | Egraph_Spec | TestCase-RectCoord | ScriptAuto/Chart/EGraph1 | 1 | 保留 | 否 |
| 46 | Egraph_Spec | TestCase-TriCood | ScriptAuto/Chart/EGraph1 | 1 | 保留 | 否 |
| 47 | Egraph_Spec | TestCase-Rect25Coord | ScriptAuto/Chart/EGraph1 | 1 | 保留 | 否 |
| 48 | Egraph_Spec | TestCase-ParallelCoord | ScriptAuto/Chart/EGraph1 | 1 | 保留 | 否 |
| 49 | Egraph_Spec | TestCase-FacetCoord | ScriptAuto/Chart/EGraph1 | 1 | 保留 | 否 |
| 50 | Egraph_Spec | TestCase-Scale | ScriptAuto/Chart/EGraph1 | 6 | 保留 | 否 |
| 51 | Egraph_Spec | TestCase-PowerScale | ScriptAuto/Chart/EGraph1 | 1 | 保留 | 否 |
| 52 | Egraph_Spec | TestCase-TimeScale | ScriptAuto/Chart/EGraph1 | 1 | 保留 | 否 |
| 53 | Egraph_Spec | TestCase-StackRange | ScriptAuto/Chart/EGraph1 | 2 | 保留 | 否 |
| 54 | Egraph_Spec | TestCase_PolarCoord | ScriptAuto/Chart/PolarGraph | 1 | 保留 | 否 |
| 55 | Egraph_Spec | TestCase-MultiText | ScriptAuto/Chart/MultiTextFrame | 1 | 保留 | 是（MultiTextFrame） |
| 56 | Egraph_Spec | TestCase-LineForm | ScriptAuto/Chart/EgraphBinding | 2 | 保留 | 否 |
| 57 | Egraph_Spec | TestCase-LabelForm | ScriptAuto/Chart/EgraphBinding | 2 | 保留 | 否 |
| 58 | Egraph_Spec | TestCase-ShapeForm | ScriptAuto/Chart/EgraphBinding | 2 | 保留 | 否 |
| 59 | Egraph_Spec | TestCase-setProperties | ScriptAuto/Chart/EgraphBinding | 1 | 保留 | 否 |
| 60 | Egraph_Spec | TestCase-AxisSpec | ScriptAuto/Chart/EGraph1 | 3 | 保留 | 否 |
| 61 | Egraph_Spec | TestCase-LegendSpec | ScriptAuto/Chart/EGraph1 | 4 | 保留 | 否 |
| 62 | Egraph_Spec | TestCase-PlotSpec | ScriptAuto/Chart/EGraph1 | 3 | 保留 | 否 |
| 63 | Form_Spec | TestCase_RCB_binding1 | ScriptAuto/Form/Form_RCB | 3 | 保留 | 否 |
| 64 | Form_Spec | TestCase-RCB_binding2 | ScriptAuto/Form/Form_RCB | 3 | 保留 | 否 |
| 65 | Form_Spec | TestCase-Slide | ScriptAuto/Form/Form_RCB | 3 | 保留 | 否 |
| 66 | Form_Spec | TestCase-Spinner | ScriptAuto/Form/Form_RCB | 1 | 保留 | 否 |
| 67 | Form_Spec | TestCase-Input | ScriptAuto/Form/Form_RCB | 3 | 保留 | 否 |
| 68 | Others_Spec | TestCase-Shape | ScriptAuto/Others/Shape | 5 | 保留 | 否 |
| 69 | Others_Spec | TestCase-Tab | ScriptAuto/Others/TabGroup | 2 | 保留 | 否 |
| 70 | Others_Spec | TestCase_GroupContainer | ScriptAuto/Others/TabGroup | 3 | 保留 | 否 |
| 71 | Others_Spec | TestCase_AccessWS | ScriptAuto/SpecialScript/accessWS | 1 | 保留 | 否 |
| 72 | Others_Spec | TestCase_DataTable | ScriptAuto/SpecialScript/TableDataScript | 1 | 保留 | 否 |
| 73 | Others_Spec | TestCase_TablePara1 | ScriptAuto/SpecialScript/TablePara1 | 1 | 保留 | 否 |
| 74 | OutPut_Spec | TestCase_Image1 | ScriptAuto/OutPut/OutPut1 | 3 | 保留 | 是（OutPut1） |
| 75 | OutPut_Spec | TestCase_Gauge1 | ScriptAuto/OutPut/OutPut1 | 2 | 保留 | 是（OutPut1） |
| 76 | OutPut_Spec | TestCase_Text1 | ScriptAuto/OutPut/OutPut1 | 5 | 保留 | 是（OutPut1） |
| 77 | ScriptEngine_Spec | TestCase-Script_Order1 | ScriptAuto/ScriptEngine/Script_Order1 | 1 | 保留 | 是（Script_Order1） |
| 78 | ScriptEngine_Spec | TestCase-Script_Order2 | ScriptAuto/ScriptEngine/Script_Order2 | 1 | 保留 | 否 |
| 79 | ScriptEngine_Spec | TestCase-Script_Order3 | ScriptAuto/ScriptEngine/Script_Order3 | 1 | 保留 | 否 |
| 80 | ScriptEngine_Spec | TestCase-Script_Order_OneWay3 | ScriptAuto/ScriptEngine/Script_Order_OneWay3 | 1 | 保留 | 否 |
| 81 | ScriptEngine_Spec | TestCase-Script_Order_OneWay4 | ScriptAuto/ScriptEngine/Script_Order_OneWay4 | 1 | 保留 | 是（Script_Order_OneWay4） |
| 82 | ScriptEngine_Spec | TestCase-Script_Order_passOn1 | ScriptAuto/ScriptEngine/Script_Order_passOn1 | 1 | 保留 | 否 |
| 83 | Selection_Spec | TestCase-Selection_Com1 | ScriptAuto/Selection/Selection1 | 4 | 保留 | 否 |
| 84 | Selection_Spec | TestCase-SelTree_Multi | ScriptAuto/Selection/SelectionTree1 | 3 | 保留 | 否 |
| 85 | Selection_Spec | TestCase-SelTree_single1 | ScriptAuto/Selection/SelectionTree1 | 3 | 保留 | 否 |
| 86 | Selection_Spec | TestCase-SelTree_single2 | ScriptAuto/Selection/SelectionTree2 | 1 | 保留 | 否 |
| 87 | Selection_Spec | TestCase-PCTree | ScriptAuto/Selection/PCTree | 3 | 保留 | 否 |
| 88 | Selection_Spec | TestCase-Calendar_Select | ScriptAuto/Selection/Relation | 1 | 保留 | 否 |
| 89 | Selection_Spec | TestCase-Selection-self | ScriptAuto/Selection/Selection2 | 2 | 保留 | 否 |
| 90 | Selection_Spec | TestCase-Range_Bind1 | ScriptAuto/Selection/Selection1 | 4 | 保留 | 否 |
| 91 | Selection_Spec | TestCase-Range_Bind2 | ScriptAuto/Selection/Selection1 | 5 | 保留 | 否 |
| 92 | Selection_Spec | TestCase-Range-Self | ScriptAuto/Selection/Selection2 | 4 | 保留 | 否 |
| 93 | Selection_Spec | TestCase-Range-Composite | ScriptAuto/Selection/Selection2 | 2 | 保留 | 否 |
| 94 | Selection_Spec | TestCase-Calendar-binding | ScriptAuto/Selection/Selection1 | 1 | 保留 | 否 |
| 95 | Selection_Spec | TestCase-Container-script | ScriptAuto/Selection/Selection2 | 2 | 保留 | 否 |
| 96 | Selection_Spec | TestCase_Calendar_ParseDate | ScriptAuto/Selection/Calendar_ParseDate | 1 | 保留 | 否 |
| 97 | Selection_Spec | TestCase_Calendar_MinMax | ScriptAuto/Selection/Calendar1 | 1 | 保留 | 否 |
| 98 | Selection_Spec | TestCase_Selection_SelectedFirstItem | ScriptAuto/Selection/Selection3 | 1 | 保留 | 否 |
| 99 | Selection_Spec | TestCase_Selection_SelectionContainer | ScriptAuto/Selection/SelectionContainer | 1 | 保留 | 是（SelectionContainer） |
| 100 | Table_Spec | TestCase-Table_Common1 | ScriptAuto/Table/Table_Table1 | 2 | 保留 | 否 |
| 101 | Table_Spec | TestCase-Crosstab_Common1 | ScriptAuto/Table/Table_Crosstab1 | 2 | 保留 | 是（Table_Crosstab1） |
| 102 | Table_Spec | TestCase-Freehand_Common1 | ScriptAuto/Table/Table_Freehand1 | 2 | 保留 | 否 |
| 103 | Table_Spec | TestCase-Table_comFunction | ScriptAuto/Table/Table_Table1 | 1 | 保留 | 否 |
| 104 | Table_Spec | TestCase-Crosstab_comFunction | ScriptAuto/Table/Table_Crosstab1 | 1 | 保留 | 是（Table_Crosstab1） |
| 105 | Table_Spec | TestCase-Table_binding | ScriptAuto/Table/Table_Table1 | 1 | 保留 | 否 |
| 106 | Table_Spec | TestCase-Crosstab-binding1 | ScriptAuto/Table/Table_Crosstab1 | 5 | 保留 | 是（Table_Crosstab1） |
| 107 | Table_Spec | TestCase-Freehand_comFunction | ScriptAuto/Table/Table_Freehand1 | 1 | 保留 | 否 |
| 108 | Table_Spec | TestCase-Freehand_binding1 | ScriptAuto/Table/Table_Freehand1 | 3 | 保留 | 否 |
| 109 | Table_Spec | TestCase-Table_Apply1 | ScriptAuto/Table/Table_Apply1 | 1 | 保留 | 否 |
| 110 | Table_Spec | TestCase-HeaderParameter | ScriptAuto/Table/Table_HeaderParameter | 3 | 保留 | 否 |
| 111 | Table_Spec | TestCase-Freehand_setRowHeight | ScriptAuto/Table/FreehandSetRowHeight | 1 | 保留 | 否 |
| 112 | Table_Spec | TestCase-Freehand_setColumnWidthAll | ScriptAuto/Table/SetColWidthAll | 1 | 保留 | 否 |
| 113 | Viewsheet_Spec | TestCase-VS_Base | ScriptAuto/VS_Self/VS_Baisc | 2 | 保留 | 否 |
| 114 | Viewsheet_Spec | TestCase-VS_Self | ScriptAuto/VS_Self/VS_Baisc | 1 | 保留 | 否 |
| 115 | Viewsheet_Spec | TestCase-VS_embedded | ScriptAuto/VS_Self/VS_embedded | 2 | 保留 | 否 |

注：“另有 state 用例”一列只计针对该 viewsheet 的专门用例。全部 130 个 viewsheet（包括上表里每一个）还各被两个总检覆盖：`ScriptStateRefreshTripwire_Spec`（刷新不吞脚本错误）和 `ScriptStateExportTripwire_Spec`（Excel / PDF 导出不抛错）。

## 比对清单的修正（G1，2026-11-20，随本记录一起改）

- `ChartProperties_Spec`（LengendsProperty 用例）的比对清单把 `LengendsProperty1` 写了两遍，`LengendsProperty2` 导出了、基线也在，却从不被比；已改成比 `LengendsProperty2`。**本机无法核对基线**（本机渲染与 CI 基线不一致），第一次 CI 跑到时如果这张图红，是基线的问题，不是脚本回归。
- `Viewsheet_Spec` 三个用例用无参 `compareImage()`（比“实际导出了什么”，某张导出丢失不会被发现）；已改成点名：VS_Base（format、property）、VS_Self（self）、VS_embedded（onInit、onRefresh）。

## 没有任何用例引用的基线（8 张，未删除）

用例改名或删除后留下的文件，现有 spec 里搜不到它们的名字。**保留未删**，是否删除由有 CI 基线的人决定（删除可由 git 恢复）：

- `cases/Chart/ChartBindingInfo_Spec/TestCase-BandingField_Chartmap.png`
- `cases/Chart/ChartProperties_Spec/TestCase-Chart_barCornerRadius_barCornerRadius1.png`、`…2.png`、`…3.png`
- `cases/Chart/ChartProperties_Spec/TestCase-FillBlank_fillTimeGap.png`
- `cases/Form_Spec/TestCase_RCB_binding_script1.png`
- `cases/Others_Spec/TestCase-Tab_bottomTabs.png`
- `cases/Viewsheet_Spec/TestCase-VS_embedded_VS.png`（VS_embedded 用例改成点名后，这张是它原来无参比较时的整图；现在不再比）

评估文档（vsscript.md）写的是 11 张；其中 `coordinateScale_coordianteTranspose`、`EGraph4_lenlayout&titlespec`、`LengendsProperty2` 已被清单引用或已修，当前是 8 张。
