$m('dam/metamanage/standard/enumitem/enumDicRe', function () {

    var enumDicReDialog;
    var enumDicReView = moduleResource.enumItem.getView('dic');
    var shuttleBoxLeftId = 'dam_metamanage_dicleftlist';
    var shuttleBoxRightId = 'dam_metamanage_dicrightlist';
    /**
     * 点击编辑按钮
     */
    this.leftGridDic = function (record, index) {
        openDicDialog(record, true);
    };

    this.rightGridDic = function (record, index) {
        openDicDialog(record, false);
    };

    /**
     * 枚举下发
     */
    function openDicDialog(record,isLeftGrid) {
        enumDicReDialog = $.topDialog({
            title: '关联字典',
            href: enumDicReView,
            width: 800,
            height: 600,
            onLoad: function () {
                Api.getDicBindRe(record.enumId, function (result) {
                    var leftGrid = enumDicReDialog.find('#' + shuttleBoxLeftId);
                    var rightGrid = enumDicReDialog.find('#' + shuttleBoxRightId);
                    result = result || {};
                    var unBindRes = result.unBindRes || [];
                    var historyRes = result.historyRes || [];
                    leftGrid.datagrid('loadData', unBindRes);
                    rightGrid.datagrid('loadData', historyRes);
                });
            },
            buttons: [
                {
                    text: '关闭',
                    handler: function () {
                        $.topDialog('close', enumDicReDialog);
                    }
                },
                {
                    text: '保存',
                    id: 'editSaveBtn',
                    handler: function () {
                        dicSave(record.enumId,isLeftGrid);
                    }
                }]
        });
    };

    /**
     * 数据字典状态
     */
    this.dicNameMatter = function (value, row, index) {
        if (!row.dicStatus) {
            value = row.dicName + '<span style="color:red">(已停用)</span>';
        } else {
            value = row.dicName;
        }
        return "<p style='cursor: pointer'><span class='bubbling_hint' title='" + value + "'>" + value + "</span></p>";
    };

    /**
     * 枚举下发保存
     */
    function dicSave(dicEnumId,isLeftGrid) {
        var rows = enumDicReDialog.find('#' + shuttleBoxRightId).datagrid('getRows');
        var ids = [];
        for (var i = 0; i < rows.length; i++) {
            ids.push(rows[i].dicId);
        }
        enumDicReDialog.parent().find('#editSaveBtn').linkbutton('disable');
        Api.saveDicRe(dicEnumId, ids, function (result) {
            if (result) {
                //$m('dam/metamanage/standard/enumitem').showEnumGrid();
                $.topDialog('close', enumDicReDialog);
                top$.messager.promptInfo({
                    msg: '保存成功',
                    icon: 'success',
                });
                if (isLeftGrid) {
                    $m('dam/metamanage/standard/enumitem').leftGridQuery();
                } else {
                    $m('dam/metamanage/standard/enumitem').rightGridQuery();
                }
            }
        });
        enumDicReDialog.parent().find('#editSaveBtn').linkbutton('enable');
    }

    /**
     * 选中右移事件
     */
    this.moveToRight = function () {
        var checkedRows = enumDicReDialog.find('#' + shuttleBoxLeftId).datagrid('getChecked');
        if (StandardUtils.isBlank(checkedRows)) {
            top$.messager.promptInfo({
                msg: '请选择移动的字典!',
                icon: 'warning',
            });
            return;
        }
        $.each(checkedRows, function (i, checkedRow) {
            var moveNumber = enumDicReDialog.find('#' + shuttleBoxLeftId).datagrid('getRowIndex', checkedRow);
            enumDicReDialog.find('#' + shuttleBoxLeftId).datagrid('deleteRow', moveNumber);
            enumDicReDialog.find('#' + shuttleBoxRightId).datagrid('appendRow', checkedRow);
        });
    };

    /**
     * 选中左移事件
     */
    this.moveToLeft = function () {
        var checkedRows = enumDicReDialog.find('#' + shuttleBoxRightId).datagrid('getChecked');
        if (StandardUtils.isBlank(checkedRows)) {
            top$.messager.promptInfo({
                msg: '请选择移动的字典!',
                icon: 'warning',
            });
            return;
        }
        $.each(checkedRows, function (i, checkedRow) {
            var moveNumber = enumDicReDialog.find('#' + shuttleBoxRightId).datagrid('getRowIndex', checkedRow);
            enumDicReDialog.find('#' + shuttleBoxRightId).datagrid('deleteRow', moveNumber);
            enumDicReDialog.find('#' + shuttleBoxLeftId).datagrid('appendRow', checkedRow);
        });
    };

    /**
     * 拼接过滤输入框HTML字符串
     * */
    var appendFilterHtml = this.appendFilterHtml = function (fieldArray) {
        var filterHtml = "";
        var trHtml = '<tr class="datagrid-header-row datagrid-filter-row">';
        var divHtml = '<div class="datagrid-cell" style="width: auto;">';
        var preTdHtml = '<td class="datagrid-filter-cell" field="';
        var quotHtml = '"';
        var endItemHtml = '>';
        filterHtml += trHtml;
        for (var i = 0; i < fieldArray.length; i++) {
            var item = fieldArray[i];
            var type = item.type;
            var field = item.field;
            var width = item.width ? item.width - 20 - 10 + 'px' : 'auto';
            filterHtml += preTdHtml;
            filterHtml += field;
            filterHtml += quotHtml;
            filterHtml += endItemHtml;
            filterHtml += divHtml;
            if ("text" == type) {
                var preInputHtml = '<span style="padding: 0 10px;background-size: contain;display: unset;" class="l-btn-empty"></span><input style="border:#b9d6e4 1px solid;height:22px;width:' + width + '" class="datagrid-selffilter-text" name="';
                var textRolesHtml = ' data-roles="mui-validatebox" class="validatebox-text"';
                filterHtml += preInputHtml;
                filterHtml += field;
                filterHtml += quotHtml;
                filterHtml += textRolesHtml;
                filterHtml += endItemHtml;
            }
        }
        filterHtml += '</tr>';
        return filterHtml;
    };
    /**
     * 初始化待选列表表头过滤
     */
    this.leftDicLoad = function () {
        var enumitemGrid = enumDicReDialog.find('#' + shuttleBoxLeftId);
        var enumitemDivObject = enumDicReDialog.find("#dam_metamanage_enumitem_candidateModelDiv");
        initWeightGridHeader(enumitemGrid, enumitemDivObject);
    };

    /**
     * 初始化已选列表表头过滤
     */
    this.rightDicLoad = function () {
        var selectedGrid = enumDicReDialog.find('#' + shuttleBoxRightId);
        var selectedDivObject = enumDicReDialog.find("#dam_metamanage_enumitem_selectModelDiv");
        initWeightGridHeader(selectedGrid, selectedDivObject);
    };

    var initWeightGridHeader = function (grid, divObject) {
        var setWidth = '100px';
        var height = '22px';
        var fieldArray = [
            {},
            {"field": "dicName", "type": "text"},
        ];
        var view2Obj = divObject.find(".datagrid-view2").find(".datagrid-htable");
        var filterHtml = appendFilterHtml(fieldArray);
        var filterRowObj = view2Obj.find(".datagrid-filter-row");
        if (filterRowObj == "undefined" || filterRowObj == null || filterRowObj.length == 0) {
            view2Obj.append(filterHtml);
            view2Obj.find(".datagrid-filter-row").find(".datagrid-selffilter-text").css({
                "width": setWidth,
                "height": height,
                "border": "#b9d6e4 1px solid"
            });
            view2Obj.find(".datagrid-filter-row").find("span").css("background-size", "contain");
            view2Obj.find(".datagrid-filter-row").find(".datagrid-filter-cell").find("input").on('keyup', function (e) {
                eventFilterFun(e.target.value, grid);
            });

            function eventFilterFun(keyword, filterGrid) {
                var filterGridContext = filterGrid.parent().find('.datagrid-view2 .datagrid-body table');
                var rows = filterGrid.datagrid('getRows') || [];
                for (var idx = 0; idx < rows.length; idx++) {
                    var rowItem = rows[idx];
                    if (rowItem) {
                        var curRow = filterGridContext.find('tr[datagrid-row-index=' + idx + ']');
                        curRow.hide();
                        var dicName = rowItem['dicName'];
                        var validRow = StandardUtils.isNotEmpty(dicName) && dicName.indexOf(keyword) > -1;
                        if (StandardUtils.isBlank(keyword) || validRow) {
                            curRow.show();
                        }
                    }
                }
            }
        }
    };
});