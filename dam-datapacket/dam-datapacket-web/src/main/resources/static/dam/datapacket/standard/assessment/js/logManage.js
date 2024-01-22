$m('dam/metamanage/standard/assessment/log', function () {

    var logGrid;
    var logGridQueryFrom;
    var log_columns = [[
        {field: 'taskName', title: '评估标准',
            formatter: function (value, row, index) {
                return StandardUtils.formatter(value);
            },
            width: 100},
        {
            field: 'taskStartTime',
            title: '开始执行时间',
            width: 100,
            formatter: function (value, row, index) {
                return StandardUtils.dateFmtTime(value);
            }
        },
        {
            field: 'taskEndTime',
            title: '耗时',
            width: 50,
            formatter: function (value, row, index) {
                var taskStartTime = row.taskStartTime;
                var taskEndTime = row.taskEndTime;
                return differTimes(taskStartTime, taskEndTime);
            }
        },
        {
            field: 'status',
            title: '评估状态',
            width: 100,
            formatter: function (value, row, index) {

                function statusLabel(color, label) {
                    return "<span style='color:" + color + ";'>" + label + "</span> ";
                }

                if (StandardUtils.equals(value, '0') || StandardUtils.equals(value, '2')) {
                    return statusLabel('#2E8AE6', '正常');
                }
                if (StandardUtils.equals(value, '1')) {
                    return statusLabel('#88C24E', '评估中');
                }
                if (StandardUtils.equals(value, '-1')) {
                    return statusLabel('#F09D3F', '异常');
                }
                return '';
            }
        },
        {field: 'assessRange', title: '评估范围',
            formatter: function (value, row, index) {
                return StandardUtils.formatter(value);
            },
            width: 300},
        {field: 'errorMsg', title: '异常信息',
            formatter: function (value, row, index) {
                return StandardUtils.formatter(value);
            },
            width: 300}
    ]];

    /**
     * 初始化选择对象
     */
    $(function () {
        logGrid = $('#metamanage_data_standard_log_grid');
        logGridQueryFrom = $('#metamanage_data_standard_log_grid_query_form');
        logGridQueryFrom.find('input[name="keyword"]').on('keyup', function (event) {
            if (event.keyCode == "13") {
                initLogGrid();
            }
        });
    });

    var initLogGrid = this.initLogGrid = function () {
        logGrid = $('#metamanage_data_standard_log_grid');
        var value = logGridQueryFrom.form('getRecord').keyword;
        var status = logGridQueryFrom.form('getRecord').status;
        Api.getLogPage(logGrid, {keyword: value, status: status}, {
            rownumbers: false,
            fitColumns: true,
            singleSelect: true,
            pagination: true,
            paginationType:'advanced',
            pageSize: 20,
            fit: true,
            border: false,
            columns: log_columns,
            toolbar: '#metamanage_data_standard_log_grid_gridToolbar',
        });
    };

    this.logGridQuery = function () {
        initLogGrid();
    };

    function differTimes(taskStartTime, taskEndTime) {
        if (StandardUtils.isNotEmpty(taskStartTime) && StandardUtils.isNotEmpty(taskEndTime)) {
            var startDate = new Date(taskStartTime);
            var startYear = startDate.getFullYear();
            var startMonth = startDate.getMonth();
            var startDay = startDate.getDate();
            var startHours = startDate.getHours();
            var startMinutes = startDate.getMinutes();
            var startSeconds = startDate.getSeconds();
            var startMSeconds = startDate.getMilliseconds();

            var endDate = new Date(taskEndTime);
            var endYear = endDate.getFullYear();
            var endMonth = endDate.getMonth();
            var endDay = endDate.getDate();
            var endHours = endDate.getHours();
            var endMinutes = endDate.getMinutes();
            var endSeconds = endDate.getSeconds();
            var endMSeconds = endDate.getMilliseconds();

            function val(s, e, c) {
                var dif = e - s;
                return dif > 0 ? dif + c : '';
            }

            var rt = val(startYear, endYear, 'y') +
                val(startMonth, endMonth, 'm') +
                val(startDay, endDay, 'd') +
                val(startHours, endHours, 'h') +
                val(startMinutes, endMinutes, 'min') +
                val(startSeconds, endSeconds, 's') +
                val(startMSeconds, endMSeconds, 'ms');
            return StandardUtils.isBlank(rt) ? '1ms' : rt;
        }
        return '-';
    }
});
