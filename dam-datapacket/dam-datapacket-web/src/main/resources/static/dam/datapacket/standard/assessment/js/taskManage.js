$m('dam/metamanage/standard/assessment/task', function () {

    var taskStyleModule = 'dam/metamanage/standard/assessment/taskStyle';
    var curModule = 'dam/metamanage/standard/assessment/task';
    var selectedTaskView = moduleResource.assessment.getView('selectedTask');
    var addTaskDialog;
    var taskGrid;
    var taskGridQueryFrom;
    var unusedGrid;
    var unusedGridQueryFrom;
    var task_columns = [[
        {field: 'ck', checkbox: true},
        {field: 'name', title: '评估标准',
            formatter: function (value, row, index) {
                value = value ? value : '';
                return "<p style='cursor: pointer'><span class='bubbling_hint' title='" + value + "'>" + value + "</span></p>";
            },
            width: 100},
        {
            field: 'sourceType', title: '标准类型', width: 100,
            formatter: function (value, row, index) {
                if (StandardUtils.equals(value, "ELEMENT")) {
                    return '数据元';
                }
                if (StandardUtils.equals(value, "ENUM")) {
                    return '枚举项';
                }
                return '';
            }
        },
        {
            field: 'runStyle', title: '评估方式', width: 100,
            formatter: function (value, row, index) {
                return value ? '自动' : '手动';
            }
        },
        {
            field: 'runFrequency', title: '评估频率', width: 100,
            formatter: function (value, row, index) {
                var label = runFrequency[value];
                return StandardUtils.isBlank(label) ? '-' : label;
            }
        },
        {
            field: 'action', title: '操作', width: 100,
            buttons: [
                {text: '编辑', onclick: "$m('dam/metamanage/standard/assessment/task').updateTask"},
                {text: '立即评估', onclick: "$m('dam/metamanage/standard/assessment/task').startTask"}
            ]
        }
    ]];
    var curStandardList = [];
    var selectedStandardList = [];

    /**
     * 初始化选择对象
     */
    $(function () {
        taskGridQueryFrom = $('#metamanage_data_standard_task_grid_query_form');
        taskGridQueryFrom.find('input[name="keyword"]').on('keyup', function (event) {
            if (event.keyCode == "13") {
                initTaskGrid();
            }
        });
    });
    var runFrequency = {
        YEAR: '年',
        MONTH: '月',
        WEEK: '周',
        DAY: '日'
    };

    var initTaskGrid = this.initTaskGrid = function () {
        taskGrid = $('#metamanage_data_standard_task_grid');
        var value = taskGridQueryFrom.form('getRecord').keyword;
        Api.getTaskPage(taskGrid, {keyword: value}, {
            rownumbers: false,
            fitColumns: true,
            singleSelect: false,
            pagination: true,
            paginationType:'advanced',
            pageSize: 20,
            idField: 'sourceId',
            fit: true,
            border: false,
            columns: task_columns,
            toolbar: '#metamanage_data_standard_task_grid_gridToolbar',
        });
    };

    this.taskGridQuery = function () {
        initTaskGrid();
    };

    this.addTask = function () {
        curStandardList = [];
        selectedStandardList = [];
        openSelectedTaskDialog();
    };

    var openSelectedTaskDialog = this.openSelectedTaskDialog = function () {
        addTaskDialog = $.topDialog({
            title: '新建标准评估任务',
            href: selectedTaskView,
            width: 900,
            height: 600,
            resizable: false,
            onLoad: selectedDialogOnLoad,
            buttons: [
                {
                    text: '取消',
                    handler: function () {
                        $.topDialog('close', addTaskDialog);
                    }
                },
                {
                    text: '下一步',
                    id: 'addSaveBtn',
                    handler: function () {
                        selectedStandardList = unusedGrid.datagrid('getSelections');
                        if (StandardUtils.listIsEmpty(selectedStandardList)) {
                            top$.messager.promptInfo({
                                msg: '请至少选择一条标准进行操作!',
                                icon: 'warning',
                            });
                        } else {
                            $.topDialog('close', addTaskDialog);
                            var title = "新建标准评估任务";
                            $m(taskStyleModule).openTaskStyleDialog(undefined, curModule, title);
                        }
                    }
                }
            ]
        });
    };

    function selectedDialogOnLoad() {
        unusedGrid = addTaskDialog.find('#metamanage_data_standard_unused_grid');
        unusedGridQueryFrom = addTaskDialog.find('#metamanage_data_standard_unused_grid_form');
        unusedGridQueryFrom.find('input[name="keyword"]').on('keyup', function (event) {
            if (event.keyCode == "13") {
                unusedGridQuery();
            }
        });
        if (StandardUtils.listIsNotEmpty(curStandardList)) {
            unusedGrid.datagrid("loadData", {
                total: 0,
                rows: curStandardList
            });
            for (var idx = 0; idx < selectedStandardList.length; idx++) {
                unusedGrid.datagrid("selectRow", unusedGrid.datagrid("getRowIndex", selectedStandardList[idx]));
            }
        } else {
            top$.messager.progress({
                text: '正在加载，请稍候....',
                interval: 500,
                width: 500
            });
            Api.getStandardList(function (result) {
                top$.messager.progress('close');
                curStandardList = result || [];
                unusedGrid.datagrid("loadData", {
                    total: 0,
                    rows: curStandardList
                });
            });
        }
    }

    this.delTask = function () {
        taskGrid = $('#metamanage_data_standard_task_grid');
        var selectRows = taskGrid.datagrid('getSelections');
        selectRows = selectRows || [];
        if (StandardUtils.listIsEmpty(selectRows)) {
            top$.messager.promptInfo({
                msg: '请至少选择一条任务进行删除!',
                icon: 'warning',
            });
            return;
        }
        var ids = [];
        for (var i = 0; i < selectRows.length; i++) {
            ids.push(selectRows[i].taskId);
        }
        $.Msg.confirm('提示', '任务进行删除，请确认！', function (flag) {
            if (flag) {
                top$.messager.progress({
                    text: '正在删除，请稍候....',
                    interval: 500,
                    width: 500
                });
                Api.delTaskByIds(ids, function (result) {
                    top$.messager.promptInfo({
                        msg: '成功!',
                        icon: 'success',
                    });
                    top$.messager.progress('close');
                    initTaskGrid();
                });
            }
        });
    };

    var unusedGridQuery = this.unusedGridQuery = function () {
        var value = unusedGridQueryFrom.form('getRecord').keyword;
        var rows = unusedGrid.datagrid('getRows');
        if (StandardUtils.listIsNotEmpty(rows)) {
            $(rows).each(function (index, item) {
                var sourceName = item['sourceName'];
                var keyNotEmpty = StandardUtils.isNotEmpty(value);
                var curRow = addTaskDialog.find('.datagrid-view .datagrid-view2 .datagrid-body table')
                    .find('tr[datagrid-row-index=' + index + ']');
                if (keyNotEmpty) {
                    var nameNotEmpty = StandardUtils.isNotEmpty(sourceName);
                    if (nameNotEmpty && sourceName.indexOf(value) > -1) {
                        curRow.show();
                        unusedGrid.datagrid('checkRow', index);
                    } else {
                        curRow.hide();
                        unusedGrid.datagrid('uncheckRow', index);
                    }
                } else {
                    curRow.show();
                    unusedGrid.datagrid('uncheckRow', index);
                }
            })
        }
    };
    //冒泡提示
    this.taskFormatter = function (value, row, index) {
        value = value ? value : '';
        return "<p style='cursor: pointer'><span class='bubbling_hint' title='" + value + "'>" + value + "</span></p>";
    };
    this.startTask = function (record, index) {
        Api.startTaskById(record.taskId, function (result) {
            top$.messager.promptInfo({
                msg: "开始评估.....",
                icon: 'success',
            });
        })
    };

    this.updateTask = function (record, idx) {
        var title = "编辑标准评估任务";
        $m(taskStyleModule).openTaskStyleDialog(record, curModule, title);
    };

    this.submitTaskStyle = function (taskCon, callback) {
        top$.messager.progress({
            text: '正在加载，请稍候....',
            interval: 500,
            width: 500
        });
        if (StandardUtils.isNotEmpty(taskCon.taskId)) {
            Api.updateTaskConf(taskCon, function (result) {
                callback.call(callback, true);
                top$.messager.progress('close');
                initTaskGrid();
            });
        } else {
            var tasks = [];
            for (var idx = 0; idx < selectedStandardList.length; idx++) {
                var item = selectedStandardList[idx];
                tasks.push({
                    name: item.sourceName,
                    runStyle: taskCon.runStyle,
                    runFrequency: taskCon.runFrequency,
                    runTime: taskCon.runTime,
                    sourceType: item.sourceType,
                    sourceId: item.sourceId
                });
            }
            Api.addTasks(tasks, function (result) {
                callback.call(callback, true);
                top$.messager.progress('close');
                initTaskGrid();
            });
        }
    };
});
