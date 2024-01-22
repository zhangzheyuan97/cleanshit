$m('dam/metamanage/standard/dataelement/typeManage', function () {

    var typeManageView = moduleResource.element.getView('typeManage');
    var elTypeListId = 'dam_matemanage_data_standard_el_type_grid';
    var curETManageUrl = 'dam/metamanage/standard/dataelement/typeManage';
    var elementLManageUrl = 'dam/metamanage/standard/dataelement/list';
    var elTypeManageUrl = 'dam/metamanage/standard/dataelement/type';
    var typeManageDialog;
    var curEditRowData;
    var curEditRowIdx;
    var sendSaveApi = false;

    $(function(){

    });

    this.initELTypeManage = function () {
        typeManageDialog = $.topDialog({
            title: '数据元分类',
            href: typeManageView,
            width: 900,
            height: 600,
            onLoad: function () {
                Api.getTypes(false, function (result) {
                    typeManageDialog.find('#' + elTypeListId).datagrid('loadData', result);
                });
            },
            onBeforeClose: function () {
                if (sendSaveApi) {
                    $m(elTypeManageUrl).reloadTypeTree();
                }
                location.reload();
            }
        });
    };

    this.fmtActionBtn = function (value, row, index) {
        var actionHtml = '';
        var detailFun = " onclick=\"$m('" + curETManageUrl + "').typeEditRow(this);\" row-status='edited'";
        actionHtml += $m(elementLManageUrl).creatBtnLink(detailFun, '编辑', '#2E8AE6');
        var cancelFun = " onclick=\"$m('" + curETManageUrl + "').typeCancelEditRow(this);\" style=\"display:none;\" row-status='editing'";
        actionHtml += $m(elementLManageUrl).creatBtnLink(cancelFun, '取消', '#F09D3F');
        var updateFun = " onclick=\"$m('" + curETManageUrl + "').typeSaveRow(this);\" style=\"display:none;\" row-status='editing'";
        actionHtml += $m(elementLManageUrl).creatBtnLink(updateFun, '保存', '#2E8AE6');
        return actionHtml;
    };

    this.rowBeforeEdit = function (rowIndex, rowData) {
        curEditRowData = rowData;
        curEditRowIdx = rowIndex;
    };

    this.typeEditRow = function (e) {
        typeManageDialog.find('#el_type_add_btn').linkbutton('disable');
        editedLinkOpt(true);
        editingLinkOpt($(e).parent(), false);
        var idx = getGridRowIdx(e);
        var grid = typeManageDialog.find('#' + elTypeListId);
        grid.datagrid('beginEdit', idx);
        //typeManageDialog.find('textarea').attr('style', 'resize : none; width: 374px;');
    };

    this.typeSaveRow = function (e) {
        var idx = getGridRowIdx(e);
        var grid = typeManageDialog.find('#' + elTypeListId);
        var vRow = grid.datagrid('validateRow', idx);
        var isValid = typeManageDialog.find("form").form('validate');
        if (vRow && isValid) {
            var rowEditors = grid.datagrid('getEditors', idx) || [];
            var newRowData = {};
            for (var i = 0; i < rowEditors.length; i++) {
                var tdEditor = rowEditors[i];
                newRowData[tdEditor.field] = tdEditor.target.val();
            }
            if (newRowData.remark && newRowData.remark.length > 200) {
                top$.messager.promptInfo({
                    msg: '数据元分类说明最大允许200字符!',
                    icon: 'warning',
                });
                return;
            }
            var usedNames = getUsedNames(curEditRowData.elementTypeId);
            if (usedNames.indexOf(newRowData.name) > -1) {
                top$.messager.promptInfo({
                    msg: '数据元分类[' + newRowData.name + ']已被使用!',
                    icon: 'warning',
                });
            } else {
                newRowData['elementTypeId'] = curEditRowData.elementTypeId;
                sendSaveApi = true;
                Api.saveTypes([newRowData], function (saveResult) {
                    if (StandardUtils.isNotEmpty(newRowData['elementTypeId'])) {
                        resetBtn($(e).parent());
                        grid.datagrid('endEdit', idx);
                    } else {
                        Api.getTypes(false, function (result) {
                            typeManageDialog.find('#' + elTypeListId).datagrid('loadData', result);
                            resetBtn(typeManageDialog);
                        });
                    }
                });

                function resetBtn(dom) {
                    typeManageDialog.find('#el_type_add_btn').linkbutton('enable');
                    editedLinkOpt(false);
                    editingLinkOpt(dom, true);
                }
            }
        }
    };

    function getUsedNames(ignoreId) {
        var grid = typeManageDialog.find('#' + elTypeListId);
        var rows = grid.datagrid('getRows') || [];
        var usedNames = [];
        for (var i = 0; i < rows.length; i++) {
            var row = rows[i];
            if (!StandardUtils.equals(row.elementTypeId, ignoreId)) {
                usedNames.push(row.name);
            }
        }
        return usedNames;
    }

    //取消编辑
    this.typeCancelEditRow = function (e) {
        typeManageDialog.find('#el_type_add_btn').linkbutton('enable');
        editedLinkOpt(false);
        editingLinkOpt($(e).parent(), true);
        var idx = getGridRowIdx(e);
        var grid = typeManageDialog.find('#' + elTypeListId);
        var tr = $(e).parent().parent().parent();
        var elTId = tr.find('td[field="elementTypeId"]');
        var elementTypeId = elTId.text();
        if (StandardUtils.isNotEmpty(elementTypeId)) {
            grid.datagrid('cancelEdit', idx);
        } else {
            grid.datagrid('deleteRow', idx);
        }
    };

    this.delElementType = function () {
        var grid = typeManageDialog.find('#' + elTypeListId);
        var selectRows = grid.datagrid('getChecked');
        if (selectRows.length == 0) {
            top$.messager.promptInfo({
                msg: '请至少选择一条记录进行删除!',
                icon: 'warning',
            });
            return;
        }
        var needDelIds = [];
        var rmIdxs = [];
        for (var i = 0; i < selectRows.length; i++) {
            var row = selectRows[i];
            if (StandardUtils.isNotEmpty(row.elementTypeId)) {
                needDelIds.push(row.elementTypeId);
            }
            rmIdxs.push(grid.datagrid('getRowIndex', row));
        }
        deleteElTypes(needDelIds, function (result) {
            rmIdxs = sortIdx(rmIdxs);
            for (var i = 0; i < rmIdxs.length; i++) {
                grid.datagrid('deleteRow', rmIdxs[i]);
            }
            typeManageDialog.find('#el_type_add_btn').linkbutton('enable');
            editedLinkOpt(false);
            editingLinkOpt(typeManageDialog, true);
        });
    };

    this.remarkFormatter = function (value, row) {
        value = value ? value : '';
        return "<p style='cursor: pointer'><span class='bubbling_hint' title='" + value + "'>" + value + "</span></p>";
    };
    function sortIdx(arr) {
        arr = arr || [];
        if (arr.length <= 1) return arr;
        var pivotIdx = Math.floor(arr.length / 2);
        var pivot = arr.splice(pivotIdx, 1)[0];
        var left = [];
        var right = [];
        for (var idx = 0; idx < arr.length; idx++) {
            var item = arr[idx];
            if (item > pivot) {
                left.push(item);
            } else {
                right.push(item);
            }
        }
        return sortIdx(left).concat([pivot], sortIdx(right));
    }

    /**
     * 删除数据元分类
     * @param ids
     * @param callback
     */
    function deleteElTypes(ids, callback) {
        if (StandardUtils.listIsNotEmpty(ids)) {
            Api.checkUsed(ids, function (result) {
                sendSaveApi = true;
                if (result) {
                    $.Msg.confirm('提示', '该数据元分类已被引用，是否确认删除？', function (flag) {
                        if (flag) {
                            Api.delTypes(ids, sendDelRest);
                        }
                    });
                } else {
                    Api.delTypes(ids, sendDelRest);
                }

                function sendDelRest(delStatus) {
                    if (delStatus) {
                        top$.messager.promptInfo({
                            msg: '删除成功!',
                            icon: 'success',
                        });
                        callback.call(callback, true);
                    }
                }
            });
        } else {
            callback.call(callback, true);
        }
    }

    this.addElementType = function () {
        var grid = typeManageDialog.find('#' + elTypeListId);
        grid.datagrid({
            rowStyler: function(index,row){
                return 'background: oldlace;';
            }
        });
        grid.datagrid('appendRow', {name: '', remark: ''});
        var idx = grid.datagrid('getRows').length;
        var curIdx = idx - 1;
        grid.datagrid('beginEdit', curIdx);
        var curRow = typeManageDialog.find('tr[datagrid-row-index=' + curIdx + ']');
        typeManageDialog.find('#el_type_add_btn').linkbutton('disable');
        editedLinkOpt(true);
        editingLinkOpt(curRow, false);
        //typeManageDialog.find('textarea').attr('style', 'resize : none; width: 374px;');
    };

    /**
     * 控制 保存和取消按钮是否展示
     * @param trDom
     * @param hidden
     */
    function editingLinkOpt(trDom, hidden) {
        var editingBtns = trDom.find('a[row-status="editing"]') || [];
        for (var idx = 0; idx < editingBtns.length; idx++) {
            $(editingBtns[idx]).css("display", hidden ? 'none' : '');
        }
    }

    /**
     * 控制编辑按钮是否展示
     * @param hidden
     */
    function editedLinkOpt(hidden) {
        var editedBtns = typeManageDialog.find('a[row-status="edited"]') || [];
        for (var idx = 0; idx < editedBtns.length; idx++) {
            $(editedBtns[idx]).css("display", hidden ? 'none' : '');
        }
    }

    /**
     * 获取当前编辑的行下标
     * @param e
     * @returns {null|*|undefined}
     */
    function getGridRowIdx(e) {
        var tr = $(e).parent().parent().parent();
        var index = tr.attr("datagrid-row-index");
        return index;
    }


});
