$m('dam/metamanage/standard/assessment/report', function () {


    var reportGrid;
    var pageSize = 20;
    var reportDialog;
    var reportGridQueryFrom;
    var pagerReportView = moduleResource.assessment.getView('pagerReport');

    var report_columns = [[
        {field: 'title', title: '评估标准',
            formatter: function (value, row, index) {
                return StandardUtils.formatter(value);
            },
            width: 100},
        {field: 'score', title: '评分', width: 50},
        {
            field: 'sourceType',
            title: '标准类型',
            width: 50,
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
        {field: 'assessRange', title: '评估范围',
            formatter: function (value, row, index) {
                return StandardUtils.formatter(value);
            },width: 300},
        {
            field: 'reportTime',
            title: '评估时间',
            width: 100,
            formatter: function (value, row, index) {
                return StandardUtils.dateFmtTime(value);
            }
        },
        {
            field: 'action', title: '操作', width: 50,
            buttons: [
                {text: '评估报告', plain: true, onclick: "$m('dam/metamanage/standard/assessment/report').openReportDialog"}
            ]
        }
    ]];

    /**
     * 初始化选择对象
     */
    $(function () {
        reportGrid = $('#metamanage_data_standard_report_grid');
        reportGridQueryFrom = $('#metamanage_data_standard_report_grid_query_form');
        reportGridQueryFrom.find('input[name="keyword"]').on('keyup', function (event) {
            if (event.keyCode == "13") {
                initReportGrid();
            }
        });
    });

    var initReportGrid = this.initReportGrid = function () {
        reportGrid = $('#metamanage_data_standard_report_grid');
        var value = reportGridQueryFrom.form('getRecord').keyword;
        Api.getReportPage(reportGrid, {keyword: value}, {
            rownumbers: false,
            fitColumns: true,
            singleSelect: true,
            pagination: true,
            paginationType:'advanced',
            pageSize: 20,
            fit: true,
            border: false,
            columns: report_columns,
            toolbar: '#metamanage_data_standard_report_grid_gridToolbar',
        });
    };

    this.reportGridQuery = function () {
        initReportGrid();
    };

    this.openReportDialog = function (record, index) {
        var isEnum = StandardUtils.equals(record.sourceType, "ENUM");
        reportDialog = $.topDialog({
            title: '评估报告',
            href: pagerReportView,
            width: 1155,
            height: 800,
            onLoad: function () {
                Api.getReportResult(record.reportId, function (result) {
                    result = result || {};
                    getIsoBanner(record.sourceType, result);
                    getIsoHeader(result);
                    getIsoReports(isEnum, result);
                    var isoTitles = result['isoTitles'];
                    getIsoTitles(isoTitles);
                });
            }
        });
    };

    function getIsoReports(isEnum, result) {
        var isoReports = result['isoReports'];
        isoReports = isoReports || [];
        for (var idx = 0; idx < isoReports.length; idx++) {
            var isoReport = isoReports[idx];
            getIsoTable(isEnum, isoReport, idx + 1);
        }
    }

    function getIsoTable(isEnum, isoReport, sort) {
        var detailHeader = getIsoDetailSort(isoReport.isoId, sort, isoReport.name);
        reportDialog.find('#iso-detail').append(detailHeader);
        var filedDetails = isoReport.filedDetails;
        if (StandardUtils.isNotEmpty(filedDetails)) {
            var thead = getTableThead();
            var table = $('<table class="iso-detail-table">');
            table.append(thead);
            for (var idx = 0; idx < filedDetails.length; idx++) {
                var filedDetail = filedDetails[idx];
                var tr = getFiveTr(idx + 1, filedDetail);
                table.append(tr);
            }
            var tableContent = $('<div class="iso-table-content">');
            tableContent.addClass('iso-table-bottom');
            tableContent.append(table);
            reportDialog.find('#iso-detail').append(tableContent);
        } else {
            detailHeader.addClass('iso-table-bottom');
        }
        if (isEnum) {
            var isoId = isoReport.isoId;
            var isALlDetail = isoReport.success > 0 || isoReport.fail > 0;
            if (isALlDetail) {
                var dicContent = getDicContent(isoReport);
                reportDialog.find('#iso-detail').append(dicContent);
                var fourTable = getNewFourTable(isoId);
                appendNewFourTable(fourTable, isoId);
                if (StandardUtils.equals(isoReport.total, 0)) {
                    var details = isoReport.details;
                    appendFourTableTr(details, isoId, 0);
                } else {
                    var param = {
                        isoId: isoId,
                        pageNum: 1,
                        pageSize: pageSize
                    };
                    Api.getIsoDetailPage(param, function (result) {
                        var rows = result.rows || [];
                        appendFourTableTr(rows, isoId, 0);
                        var total = result.total || 0;
                        if (total > param.pageSize * param.pageNum) {
                            var fourTrMore = getFourTrMore(isoReport.isoId, param.pageSize, param.pageNum + 1);
                            var curTable = reportDialog.find('table[table-id="' + isoId + '"]');
                            curTable.append(fourTrMore);
                        }
                    });
                }
            }
        }
    }

    function appendNewFourTable(fourTable, isoId) {
        var fourContent = $('<div class="iso-table-content" content-table-id="' + isoId + '">');
        fourContent.addClass('iso-table-bottom');
        fourContent.append(fourTable);
        reportDialog.find('#iso-detail').append(fourContent);
    }

    function getNewFourTable(isoId) {
        var fourThead = getFourThead();
        var fourTable = $('<table class="iso-detail-table" table-id="' + isoId + '">');
        fourTable.append(fourThead);
        return fourTable;
    }

    function appendFourTableTr(details, isoId, lastIdx) {
        var fourTable = reportDialog.find('table[table-id="' + isoId + '"]');
        for (var idx = 0; idx < details.length; idx++) {
            var detail = details[idx];
            var fourTr = getFourTr(lastIdx + idx + 1, detail);
            fourTable.append(fourTr);
        }
    }


    function getFourThead() {
        var thead = $('<thead>');
        var tr = $('<tr>');
        tr.append($('<th style="width: 60px;">').text('序号'));
        tr.append($('<th style="width: 260px;">').text('标准'));
        tr.append($('<th style="width: 260px;">').text('当前值'));
        tr.append($('<th style="width: 140px;">').text('结论'));
        thead.append(tr);
        return thead;
    }

    function getFourTr(tdSort, detail) {
        var tr = $('<tr>');
        var td1 = getTd(tdSort, '');
        var td2 = getTd(detail.standard, 'iso-td-span');
        var td3 = getTd(detail.curItem, 'iso-td-span');
        var isSucc = StandardUtils.equals(detail.result, 1);
        var td4 = getTd(isSucc ? '符合标准' : '不符合', isSucc ? 'iso-font-success' : 'iso-font-fail');
        tr.append(td1);
        tr.append(td2);
        tr.append(td3);
        tr.append(td4);
        return tr;
    }

    function getFourTrMore(isoId, pageSize, pageNum) {
        var tr = $('<tr>');
        var clickHtml = ' onclick="$m(\'dam/metamanage/standard/assessment/report\').isoLoadMore(this,\'' + isoId + '\',\'' + pageSize + '\',\'' + pageNum + '\')"';
        var imgHtml = $('<img>').attr("src", "/dam-metamanage/dam/metamanage/standard/assessment/image/detail-more.svg");
        var td = $('<td class="iso-more-detail" colspan="4" ' + clickHtml + ' >').append(imgHtml);
        tr.append(td);
        return tr;
    }

    this.isoLoadMore = function (e, isoId, pageSize, pageNum) {
        $(e).parent().remove();
        pageNum = Number(pageNum);
        pageSize = Number(pageSize);
        var param = {
            isoId: isoId,
            pageNum: pageNum,
            pageSize: pageSize
        };
        var lastIdx = param.pageSize * param.pageNum;
        Api.getIsoDetailPage(param, function (result) {
            var rows = result.rows || [];
            appendFourTableTr(rows, isoId, lastIdx - pageSize);
            var total = result.total || 0;
            if (total > lastIdx) {
                var fourTrMore = getFourTrMore(isoId, pageSize, pageNum + 1);
                var curTable = reportDialog.find('table[table-id="' + isoId + '"]');
                curTable.append(fourTrMore);
            }
        });
    };

    function getDicContent(isoReport) {
        var successVal = isoReport.success;// 成功数
        var failVal = isoReport.fail;// 失败数
        var totalVal = successVal + failVal;// 总数量
        var dicContent = $('<div class="iso-dic-content">');
        var leftHtml = $('<span class="iso-dic-span-left">').text('【' + isoReport.name + '】枚举值评估详情');
        var rightIHtml = $('<span class="iso-dic-span-right iso-font-info">').text('枚举值总数: ' + totalVal + '     符合项: ' + successVal);
        var rightEHtml = $('<span class="iso-dic-span-right">').text('不符合项:  ' + failVal);
        if (failVal > 0) {
            rightEHtml.addClass('iso-font-fail');
        } else {
            rightEHtml.addClass('iso-font-info');
        }
        dicContent.append(leftHtml);
        dicContent.append(rightEHtml);
        dicContent.append(rightIHtml);
        return dicContent;
    }

    function getFiveTr(tdSort, detail) {
        var tr = $('<tr>');
        var td1 = getTd(tdSort, '');
        var td2 = getTd(detail.assessItem, 'iso-td-span');
        var td3 = getTd(detail.standard, 'iso-td-span');
        var td4 = getTd(detail.curItem, 'iso-td-span');
        var isSucc = StandardUtils.equals(detail.result, 1);
        var td5 = getTd(isSucc ? '符合标准' : '不符合', isSucc ? 'iso-font-success' : 'iso-font-fail');
        tr.append(td1);
        tr.append(td2);
        tr.append(td3);
        tr.append(td4);
        tr.append(td5);
        return tr;
    }

    function getTd(value, cls) {
        var span = $('<span title="' + value + '">').text(value);
        if (StandardUtils.isNotEmpty(cls)) {
            span.addClass(cls);
        }
        var td = $('<td>').append(span);
        return td;
    }

    function getTableThead() {
        var thead = $('<thead>');
        var tr = $('<tr>');
        tr.append($('<th style="width: 60px;">').text('序号'));
        tr.append($('<th style="width: 180px;">').text('评估项'));
        tr.append($('<th style="width: 180px;">').text('标准'));
        tr.append($('<th style="width: 180px;">').text('当前值'));
        tr.append($('<th style="width: 100px;">').text('结论'));
        thead.append(tr);
        return thead;
    }

    function getIsoDetailSort(isoId, sort, isoLabel) {
        var item = $('<div style="width: 720px;height: 32px;" id="' + isoId + '">');
        var sortHtml = $('<div class="iso-detail-sort">');
        sortHtml.text(sort);
        var labelHtml = $('<div class="iso-detail-title ">');
        labelHtml.text('评估对象:  ' + isoLabel);
        item.append(sortHtml);
        item.append(labelHtml);
        return item;
    }

    /**
     * 绘制banner
     * @param result
     */
    function getIsoBanner(sourceType, result) {
        var reportName = result.reportName;
        var sourceLabel = StandardUtils.equals(sourceType, "ENUM") ? '枚举项' : '数据元';
        var bannerTitle = sourceLabel + "标准“" + reportName + "”执行评估报告";
        reportDialog.find('#iso-banner-title').text(bannerTitle);
        var bannerTime = StandardUtils.dateFmtTime(result.reportTime);
        reportDialog.find('#iso-banner-time').text('评估时间: ' + bannerTime);
    }

    /**
     * 绘制报表Header和Title
     * @param result
     */
    function getIsoHeader(result) {
        var score = result.score;
        var isSucc = score.indexOf('100') === 0;
        var imgHtml = getImageSvg(isSucc, 'report');
        reportDialog.find('#iso-al-t-icon').append(imgHtml);
        reportDialog.find('#iso-al-t-icon').addClass(isSucc ? 'iso-bg-success' : 'iso-bg-fail');
        reportDialog.find('#iso-al-t-icon').parent().addClass(isSucc ? 'iso-border-info' : 'iso-border-fail');
        reportDialog.find('#iso-al-t-result').text('贯标结果:' + (isSucc ? '通过' : '不通过'));
        reportDialog.find('#iso-al-t-result').addClass(isSucc ? 'iso-font-success' : 'iso-font-fail');
        reportDialog.find('#iso-al-t-label').text('评估标准:  ' + result.reportName);
        reportDialog.find('#iso-al-t-label').attr('title', result.reportName);
        var scoreItem = getAnalItem(score, '评分', isSucc);
        scoreItem.addClass(isSucc ? 'iso-border-info' : 'iso-border-fail');
        reportDialog.find('#iso-header').append(scoreItem);
        var adoptVal = 0;
        var unAdoptVal = 0;
        var isoTitles = result['isoTitles'] || [];
        for (var idx = 0; idx < isoTitles.length; idx++) {
            var isoTitle = isoTitles[idx];
            var isoTleI = getIsoTleI(idx + 1, isoTitle);
            reportDialog.find('#iso-title').append(isoTleI);
            if (StandardUtils.equals(isoTitle.result, 'success')) {
                adoptVal++;
            } else {
                unAdoptVal++;
            }
        }
        var adoptItem = getAnalItem(adoptVal, '通过', adoptVal > 0);
        adoptItem.addClass(adoptVal > 0 ? 'iso-border-info' : 'iso-border-fail');
        reportDialog.find('#iso-header').append(adoptItem);
        var unAdoptItem = getAnalItem(unAdoptVal, '未通过', unAdoptVal < 1);
        unAdoptItem.addClass(unAdoptVal < 1 ? 'iso-border-info' : 'iso-border-fail');
        reportDialog.find('#iso-header').append(unAdoptItem);
    }

    /**
     * 绘制贯标评分与贯标合计结果
     * @param num
     * @param label
     * @param isSucc
     * @returns {x.fn.init|bI.fn.init|jQuery|HTMLElement}
     */
    function getAnalItem(num, label, isSucc) {
        var item = $('<div class="iso-anal-item">');
        var numHtml = $('<div class="iso-score-num">').text(num).addClass(isSucc ? 'iso-font-success' : 'iso-font-fail');
        var labelHtml = $('<div class="iso-score-label">').text(label);
        item.append(numHtml);
        item.append(labelHtml);
        return item;
    }

    /**
     * 绘制评估对象
     * @param isoTitle
     * @returns {x.fn.init|bI.fn.init|jQuery|HTMLElement}
     */
    function getIsoTleI(sort, isoTitle) {
        var tleItem = $('<div class="iso-tle-i">');
        var tleItemLabel = $('<div class="iso-tle-il">');
        var spanLabel = $('<span class="iso-tle-il-label">');
        var spanValue = $('<span class="iso-tle-il-value">');
        spanLabel.text('评估对象' + sort + ' :');
        spanValue.text(isoTitle.name);
        spanValue.attr('title', isoTitle.name);
        tleItemLabel.append(spanLabel);
        tleItemLabel.append(spanValue);
        var tleItemResult = $('<div class="iso-tle-ir">');
        var icon = $('<div class="iso-tle-ir-icon">');
        var isSucc = StandardUtils.equals(isoTitle.result, 'success');
        var imgHtml = getImageSvg(isSucc, 'iso');
        icon.append(imgHtml);
        icon.addClass(isSucc ? 'iso-bg-success' : 'iso-bg-fail');
        var label = $('<div class="iso-tle-ir-label">').text(isSucc ? '通过' : '不通过');
        label.addClass(isSucc ? 'iso-font-success' : 'iso-font-fail');
        tleItemResult.append(icon);
        tleItemResult.append(label);
        tleItem.append(tleItemLabel);
        tleItem.append(tleItemResult);
        return tleItem;
    }

    function getIsoTitles(isoTitles) {
        isoTitles = isoTitles || [];
        for (var isoTitle of isoTitles) {
            var clickHtml = ' onclick="$m(\'dam/metamanage/standard/assessment/report\').gotoView(this,\'' + isoTitle.isoId + '\')"';
            var li = $('<li class="iso-li" ' + clickHtml + '>');
            var span = $('<span class="iso-span iso-title-span">');
            span.text(isoTitle.name);
            span.attr('title', isoTitle.name);
            li.append(span);
            reportDialog.find('#iso-title-content').append(li);
        }
    }

    this.gotoView = function (e, viewId) {
        reportDialog.find('.iso-active').removeClass("iso-active");
        reportDialog.find('.iso-span-active').removeClass("iso-span-active");
        $(e).addClass("iso-active");
        $(e).find('span').addClass("iso-span-active");
        reportDialog.find('#' + viewId)[0].scrollIntoView()
    };

    function getImageSvg(isSucc, prefix) {
        var imageName = isSucc ? '-success' : '-failure';
        var imgHtml = $('<img>').attr("src", "/dam-metamanage/dam/metamanage/standard/assessment/image/" + prefix + imageName + ".svg");
        return imgHtml;
    }
});
