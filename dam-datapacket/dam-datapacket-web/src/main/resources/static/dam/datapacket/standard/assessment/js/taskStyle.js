$m('dam/metamanage/standard/assessment/taskStyle', function () {


    var taskStyleDialog;
    var taskStyleView = moduleResource.assessment.getView('taskStyle');
    var taskManageModule = 'dam/metamanage/standard/assessment/task';
    var curMonth = 1;
    var curDay = 1;
    var allowedDayMsg = '因每月天数在28-31天不等，建议您直接选择最后一天';
    var allowedMonthMsg = '因平年、闰年2月的天数不等，建议您直接选择最后一天';

    function getDefaultInfo() {
        var runType ='0';
        // if (taskStyleDialog) {
        //     runType = taskStyleDialog.find('#task-style-form').form('getRecord').runType;
        // }
        return {
            auto: runType, //执行方式
            frequency: 'MONTH',//执行频率
            WEEK: '1',//按周执行
            YEAR: {//按年执行
                month: '01',
                day: '01'
            },
            MONTH: '01',//按月执行
            time: '00:00:00'
        };
    }

    var info = getDefaultInfo();
    /**
     * 输入输出
     * @param record 输入
     * @param callback 输出
     */
    //打开任务调度方式窗口
    this.openTaskStyleDialog = function (record, moduleId, title) {
        var buttons = getDialogBtns(record, moduleId);
        taskStyleDialog = $.topDialog({
            title: title,
            href: taskStyleView,
            width: 900,
            height: 680,
            resizable: false,
            onLoad: function () {
                panelHideClickEvent();
                info = covertInfoByRecord(record);
                //执行方式
                taskStyleDialog.find('#task-style-form').form('load', {runType: info.auto});
                contentChange(info.auto);
                if (StandardUtils.equals("1", info.auto)) {
                    //执行频率
                    selectedFrequency(info.frequency);
                    //执行日期
                    var runDateVal = setAutoInfoRunDate(info.frequency);
                    taskStyleDialog.find('#runDate').combobox('setValue', runDateVal);
                    //执行时间
                    taskStyleDialog.find('#runTime').timespinner('setValue', info.time);
                }
            },
            buttons: buttons
        });
    };


    function getDialogBtns(record, moduleId) {
        if (record) {
            return [
                {
                    text: '取消',
                    handler: function () {
                        $.topDialog('close', taskStyleDialog);
                    }
                },
                {
                    text: '确定',
                    id: 'addSaveBtn',
                    handler: function () {
                        var isValid = taskStyleDialog.find('#task-style-form').form('validate');
                        if (isValid) {
                            info.auto = taskStyleDialog.find('#task-style-form').form('getRecord').runType;
                            var runStyle = StandardUtils.equals(info.auto, '1');
                            $m(moduleId).submitTaskStyle({
                                taskId: record.taskId,
                                runStyle: runStyle,
                                runFrequency: runStyle ? info.frequency : '',
                                runTime: runStyle ? getRunTimeFromInfo() : ''
                            }, function (result) {
                                $.topDialog('close', taskStyleDialog);
                            });
                        }
                    }
                }
            ];
        }
        return [
            {
                text: '取消',
                handler: function () {
                    $.topDialog('close', taskStyleDialog);
                }
            },
            {
                text: '上一步',
                id: 'addSaveBtn',
                handler: function () {
                    $.topDialog('close', taskStyleDialog);
                    $m(taskManageModule).openSelectedTaskDialog();
                }
            },
            {
                text: '确定',
                id: 'addSaveBtn',
                handler: function () {
                    var isValid = taskStyleDialog.find('#task-style-form').form('validate');
                    if (isValid) {
                        info.auto = taskStyleDialog.find('#task-style-form').form('getRecord').runType;
                        var runStyle = StandardUtils.equals(info.auto, '1');
                        $m(moduleId).submitTaskStyle({
                            runStyle: runStyle,
                            runFrequency: runStyle ? info.frequency : '',
                            runTime: runStyle ? getRunTimeFromInfo() : ''
                        }, function (result) {
                            $.topDialog('close', taskStyleDialog);
                        });
                    }
                }
            }
        ];
    }

    /**
     * 构建执行频率数据
     * year: 0000:01:-1 00:00:00
     * month: 0000:00:-1 00:00:00
     * week: 0000:00:07 00:00:00
     * day: 0000:00:00 00:00:00
     * @param record
     */
    function getRunTimeFromInfo() {
        function appendPre(v) {
            v = Number(v);
            return (v === -1 || v > 9) ? '' + v : '0' + v;
        }

        var runTime = '0000';
        if (StandardUtils.equals(info.frequency, 'YEAR')) {
            runTime += (':' + appendPre(info.YEAR.month));
            runTime += (':' + appendPre(info.YEAR.day));
        }
        if (StandardUtils.equals(info.frequency, 'MONTH')) {
            runTime += (':00:' + appendPre(info.MONTH));
        }
        if (StandardUtils.equals(info.frequency, 'WEEK')) {
            runTime += (':00:0' + info.WEEK);
        }
        if (StandardUtils.equals(info.frequency, 'DAY')) {
            runTime += ':00:00';
        }
        runTime += (' ' + taskStyleDialog.find('#runTime').timespinner('getValue'));
        return runTime;
    }

    this.selectedWeek = function (record) {
        info.WEEK = record.value;
    };

    /**
     * 构建执行对象
     * @param record
     */
    function covertInfoByRecord(record) {
        if (record) {
            info.frequency = record.runFrequency;
            info.auto = String(record.runStyle ? '1' : '0');
            var runTime = record.runTime || '';
            var month = runTime.substr(5, 2);
            var day = runTime.substr(8, 2);
            var time = runTime.substr(11);
            info.time = time;
            if (StandardUtils.equals(info.frequency, 'YEAR')) {
                info.YEAR.month = month;
                info.YEAR.day = day;
            }
            if (StandardUtils.equals(info.frequency, 'MONTH')) {
                info.MONTH = day;
            }
            if (StandardUtils.equals(info.frequency, 'WEEK')) {
                info.WEEK = String(Number(day));
            }
            return info;
        }
        return getDefaultInfo();
    }

    /**
     * 弹出层控制面板点击监听
     */
    function panelHideClickEvent() {
        taskStyleDialog.find(".panel").on('click', function (ev) {
            var datePanel = taskStyleDialog.find('#dam_matemanage_assessment_fsed_panel');
            var classList = ev.target.classList;
            var curTagName = ev.target.tagName;
            if (StandardUtils.equals(curTagName, 'SPAN')) {
                if (classList.contains('combo-arrow')) {
                    return;
                }
            }
            var noHide = isFsedPanel(ev);
            if (!noHide) {
                datePanel.hide();
                //执行日期
                var runDateVal = setAutoInfoRunDate(info.frequency);
                taskStyleDialog.find('#runDate').combobox('setValue', runDateVal);
            }
        });
    }

    function setAutoInfoRunDate(v) {
        var runDateVal = '';
        if (StandardUtils.equals('YEAR', v)) {
            var monthObj = info['YEAR'];
            var monthValue = monthObj.month;
            var dayValue = monthObj.day;
            if (StandardUtils.equals(dayValue, '-1')) {
                dayValue = '最后一天';
            }
            runDateVal = Number(monthValue) + '月' + dayValue;
        }
        if (StandardUtils.equals('MONTH', v)) {
            runDateVal = info['MONTH'];
            if (StandardUtils.equals(runDateVal, '-1')) {
                runDateVal = '最后一天';
            }
        }
        if (StandardUtils.equals('WEEK', v)) {
            runDateVal = info.WEEK;
        }
        return runDateVal;
    }

    this.changAssessType = function (type) {
        contentChange(type);
        if (StandardUtils.equals("1", info.auto)) {
            reloadStyleForm();
        }
    };

    function contentChange(type) {
        info.auto = type;
        if (StandardUtils.equals('0', type)) {
            taskStyleDialog.find('#tk-run-frequency-content').hide();
            taskStyleDialog.find('#tk-run-runDate-content').hide();
            taskStyleDialog.find('#tk-run-runTime-content').hide();
        } else {
            taskStyleDialog.find('#tk-run-frequency-content').show();
            taskStyleDialog.find('#tk-run-runDate-content').show();
            taskStyleDialog.find('#tk-run-runTime-content').show();
        }
    }

    function reloadStyleForm() {
        info = getDefaultInfo();
        //执行频率
        selectedFrequency(info.frequency);
        //执行日期
        var runDateVal = setAutoInfoRunDate(info.frequency);
        taskStyleDialog.find('#runDate').combobox('setValue', runDateVal);
        //执行时间
        taskStyleDialog.find('#runTime').timespinner('setValue', info.time);
    }

    function isFsedPanel(ev) {
        var tag = ev.target;
        var tagTd = tag.id;
        var tagName = tag.tagName;
        if (StandardUtils.equals(tagTd, 'dam_matemanage_assessment_fsed_panel')) {
            return true;
        }
        while (!StandardUtils.equals(tagName, 'BODY')) {
            tag = tag.parentElement;
            tagTd = tag.id;
            tagName = tag.tagName;
            if (StandardUtils.equals(tagTd, 'dam_matemanage_assessment_fsed_panel')) {
                return true;
            }
        }
        return false;
    }

    var selectedFrequency = this.selectedFrequency = function (frequency) {
        taskStyleDialog.find('.tk-selected-frequency').removeClass('tk-selected-frequency');
        taskStyleDialog.find('#tk-run-frequency-' + frequency).addClass('tk-selected-frequency');
        info.frequency = frequency;
        if (StandardUtils.equals(info.frequency, 'DAY')) {
            taskStyleDialog.find('#tk-run-runDate-content').hide();
        } else {
            taskStyleDialog.find('#tk-run-runDate-content').show();
        }
        //执行日期
        var runDateVal = setAutoInfoRunDate(frequency);
        taskStyleDialog.find('#runDate').combobox('setValue', runDateVal);
    };
    this.confShowPanel = function () {
        taskStyleDialog.find('#runDate').combobox({required: false});
        if (StandardUtils.equals(info.frequency, 'WEEK')) {
            taskStyleDialog.find('#runDate').combobox({required: true});
        } else {
            taskStyleDialog.find('#runDate').combobox('hidePanel');
            taskStyleDialog.find('#dam_matemanage_assessment_fsed_panel').show();
            if (StandardUtils.equals('YEAR', info.frequency)) {
                var monthValue = info.YEAR.month;
                var dayValue = info.YEAR.day;
                var mv = Number(monthValue);
                dyeMonthDay(mv);
                var dv = Number(dayValue);
                dyeDay(dv);
                curMonth = mv;
                curDay = dv;
            }
            if (StandardUtils.equals('MONTH', info.frequency)) {
                dyeMonthDay(0);
                var dv = Number(info.MONTH);
                curDay = dv;
                dyeDay(dv);
            }
        }
        //执行日期
        var runDateVal = setAutoInfoRunDate(info.frequency);
        (function () {
            // 面板隐藏事件
            // taskStyleDialog.find('#runDate').combobox('setValue', runDateVal);
            taskStyleDialog.unbind('.combo').bind('mousedown.combo mousewheel.combo', function (e) {
                var p = $(e.target).closest('#dam_matemanage_assessment_fsed_panel');
                if (p.length) {
                    return;
                }
                taskStyleDialog.find('#dam_matemanage_assessment_fsed_panel').hide();
            });
            // 面板位置适应微调
            var combo = taskStyleDialog.find('#runDate').data('combo').combo;
            var panel = taskStyleDialog.find('#dam_matemanage_assessment_fsed_panel');
            var window = taskStyleDialog.dialog('window');
            if (panel.is(':visible')) {
                panel.css({
                    top: getTop()
                });
                setTimeout(arguments.callee, 200);
            }
            function getTop() {
                var top = combo.position().top + combo._outerHeight();
                if (top + panel._outerHeight() > window._outerHeight() ) {
                    //超出底部边界
                    top = window._outerHeight() - panel._outerHeight();
                    panel.css({
                        marginTop:"-6px"
                    });
                }
                return top;
            }
        })();
    };

    this.prevMonth = function () {
        changeMonth(-1);
    };
    this.nextMonth = function () {
        changeMonth(1);
    };

    function changeMonth(v) {
        if (StandardUtils.equals(info.frequency, 'DAY') || StandardUtils.equals(info.frequency, 'WEEK')) {
            taskStyleDialog.find('#dam_matemanage_assessment_fsed_panel').hide();
            return;
        }
        curMonth += v;
        if (curMonth < 1) {
            curMonth = 12;
        }
        if (curMonth > 12) {
            curMonth = 1;
        }
        dyeMonthDay(curMonth);
    }

    function dyeMonthDay(curMonth) {
        taskStyleDialog.find('#fsed-p-month-value').text(curMonth + '月');
        var maxDay = 30;
        var maxAllowed = 30;
        if (StandardUtils.equals(info.frequency, 'YEAR')) {
            taskStyleDialog.find('#fsed-p-month-select').show();
            if (StandardUtils.equals(curMonth, 2)) {
                maxDay = 29;
                maxAllowed = 28;
            } else {
                var max31Day = [1, 3, 5, 7, 8, 10, 12];
                if (max31Day.indexOf(curMonth) > -1) {
                    maxDay = 31;
                    maxAllowed = 31;
                }
            }
        }
        if (StandardUtils.equals(info.frequency, 'MONTH')) {
            taskStyleDialog.find('#fsed-p-month-select').hide();
            maxDay = 31;
            maxAllowed = 28;
        }
        taskStyleDialog.find('#fsed-p-day-content').html('');
        var html = '';
        for (var idx = 1; idx < maxDay + 1; idx++) {
            if (idx <= maxAllowed) {
                html += '<div class="fsed-day-btn" id ="day-btn-' + idx + '" onclick="$m(\'dam/metamanage/standard/assessment/taskStyle\').selectedDay(this,\'' + idx + '\');">' + idx + '</div>';
            } else {
                var msg = StandardUtils.equals(info.frequency, 'YEAR') ? allowedMonthMsg : allowedDayMsg;
                html += '<div class="fsed-day-btn fsed-day-not-allowed" title="' + msg + '">' + idx + '</div>';
            }
        }
        html += '<div class="fsed-day-btn" style="width: 150px;float: right;"  id ="day-btn--1"' +
            ' onclick="$m(\'dam/metamanage/standard/assessment/taskStyle\').selectedDay(this,\'-1\');">最后一天</div>';
        taskStyleDialog.find('#fsed-p-day-content').html(html);
    }

    this.selectedDay = function (e, day) {
        curDay = day;
        dyeDay(day);
        var runDateVal = '';
        var dayLabel = StandardUtils.equals(curDay, '-1') ? '最后一天' : (curDay > 9 ? curDay : '0' + curDay);
        if (StandardUtils.equals('YEAR', info.frequency)) {
            runDateVal = curMonth + '月' + dayLabel;
        }
        if (StandardUtils.equals('MONTH', info.frequency)) {
            runDateVal = dayLabel;
        }
        taskStyleDialog.find('#runDate').combobox('setValue', runDateVal);
    };

    function dyeDay(day) {
        taskStyleDialog.find('.fsed-day-btn-dye').removeClass('fsed-day-btn-dye');
        taskStyleDialog.find('#day-btn-' + day).addClass('fsed-day-btn-dye');
    }

    this.cancelBtn = function () {
        taskStyleDialog.find('#dam_matemanage_assessment_fsed_panel').hide();
        //执行日期
        var runDateVal = setAutoInfoRunDate(info.frequency);
        taskStyleDialog.find('#runDate').combobox('setValue', runDateVal);
    };

    this.confirmBtn = function () {
        if (StandardUtils.equals('YEAR', info.frequency)) {
            info['YEAR'] = {
                month: String(curMonth),
                day: String(curDay)
            }
        }
        if (StandardUtils.equals('MONTH', info.frequency)) {
            info['MONTH'] = String(curDay);
        }
        taskStyleDialog.find('#dam_matemanage_assessment_fsed_panel').hide();
    };
});
