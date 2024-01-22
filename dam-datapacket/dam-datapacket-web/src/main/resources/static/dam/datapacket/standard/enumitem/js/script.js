$m('dam/metamanage/standard/enumitem', function () {
    /**
     * 初始化选择对象
     */
    var addDialog;
    var editDialog;
    var addEnumDialog;
    var editEnumDialog;
    var importDataDialog;
    var itemExtractDialog;
    var grid;
    var enumgrid;
    //左侧枚举列表查询form域
    var leftQueryForm;
    //右侧枚举列表查询form域
    var rightQueryForm;
    //枚举值列表查询form域
    var itemQueryForm;
    var tree;
    var listEnumId;
    var structure;
    var addName;
    var parentId;
    var nextNode;
    var currPage = 1;
    var pageSize = 20;
    var pageCount = 0;
    var pageKeyword = '';
    var bnn = false;
    var bnn2 = false;
    var addgetTypepath = moduleResource.enumItem.getView('add');
    var editgetTypepath = moduleResource.enumItem.getView('edit');
    var addEnumTypepath = moduleResource.enumItem.getView('addEnum');
    var editEnumTypepath = moduleResource.enumItem.getView('editEnum');
    var importData = moduleResource.enumItem.getView('importData');
    var itemExtract = moduleResource.enumItem.getView('itemExtract');
    this.pagePath = moduleResource.enumItem.rootPath + moduleResource.enumItem.apiPath.typePage;
    this.page = moduleResource.enumItem.rootPath + moduleResource.enumItem.apiPath.typePage;
    $(function () {
        getData(currPage, pageSize);
        grid = $("#dam_metamanage_standard_grid");
        tree = $('#dam_metamanage_standard_enum_item_tree');
        enumgrid = $("#all_in_one_grid");
        leftQueryForm = $('#dam_metamanage_se_type_left_query_form');
        leftQueryForm.find('input[name="keyword"]').on('keyup', function (e) {
            leftGridKeydown(e)
        });
        rightQueryForm = $('#dam_metamanage_se_type_right_query_form');
        rightQueryForm.find('input[name="keyword"]').on('keyup', function (e) {
            rightGridKeydown(e)
        });
        itemQueryForm = $('#dam_metamanage_dataEnum_query_form');
        itemQueryForm.find('input[name="keyword"]').on('keyup', function (e) {
            itemGridKeydown(e);
        });
        $("#addBtn").hide();
        $("#aiot_titleHide").hide();
        $('.layout-button-left:first').on('click', function (e) {
            $("#showBtn").hide();
            $("#addBtn").show();
            $("#aiot_title").hide();
            $('#aiot_center_north').css({
                'height': '60px',
                // 'line-height': '60px'
            });
            $('.aiot_center').find('.panel.layout-panel.layout-panel-center').css({
                'top': '60px',
                'height':'auto',
            });
            var height_center = $('#aiot_center_center')[0].offsetHeight;
            $('#aiot_center_center').css({
                'height': height_center+55+'px',
            })
            $('#aiot_center_west').css('display', 'none');
            $('#aiot_center_west').parent().css('display', 'none');
            var value = leftQueryForm.form('getRecord').keyword;
            rightQueryForm.find('input[name="keyword"]').val(value);
            resetRightDiv(28, 0);
            showEnumGrid(value);
            if (!bnn) {
                bnn = true;
                $('.layout-button-right:eq('+($('.layout-button-right').size()-1)+')').on('click',function(e){
                    $("#aiot_title").show();
                    var pagination = $("#all_in_one_grid").datagrid("getPager").data("pagination");
                    currPage = pagination.options.pageNumber;
                    pageSize = pagination.options.pageSize;
                    var value = rightQueryForm.form('getRecord').keyword;
                    leftQueryForm.find('input[name="keyword"]').val(value);
                    leftGridSelectNode = undefined;
                    getData(currPage, pageSize, value);
                    setTimeout(function () {
                        if (listEnumId) {
                            reloadItemTreeAndPage();
                        } else {
                            reloadItemInitView();
                        }
                        $("#showBtn").show();
                        $("#addBtn").hide()
                    }, 700)
                });
            }
        });
    });

    /**
     * 左右收缩事件
     */
    var enum_columns = [[
        {field: 'code', title: '枚举项编码',  formatter: function (value, row, index) {
                return StandardUtils.formatter(value);
            },width: 100},
        {field: 'name', title: '枚举项名称',
            formatter: function (value, row, index) {
                return StandardUtils.formatter(value);
            },
            width: 100},
        {
            field: 'structure', title: '数据结构',
            formatter: function (value, row, index) {
                if (row.structure) {
                    return '树结构';
                } else {
                    return '列表结构';
                }
            },
            width: 100
        },
        {field: 'reDicNames', title: '关联字典',
            formatter: function (value, row, index) {
                return StandardUtils.formatter(value);
            },
            width: 100},
        {field: 'remark', title: '枚举项说明',
            formatter: function (value, row, index) {
                return StandardUtils.formatter(value);
            },
            width: 100},
        {
            field: 'action', title: '操作', width: 100,
            buttons: [
                {text: '编辑', onclick: "$m('dam/metamanage/standard/enumitem').rightGridEdit"},
                {text: '关联字典', onclick: "$m('dam/metamanage/standard/enumitem/enumDicRe').rightGridDic"},
                {text: '删除', onclick: "$m('dam/metamanage/standard/enumitem').rightGridRemove"}
            ]
        }
    ]];

    var formatteval = function(value, row, index){
        value = value ? value : '';
        return "<p style='cursor: pointer'><span class='bubbling_hint' title='" + value + "'>" + value + "</span></p>";
    };
   var showEnumGrid = this.showEnumGrid=function(keyword) {
        rightGridSelectNode = undefined;
        var tempTable = $('#all_in_one_grid');
        Api.getTypePage(tempTable, {keyword: keyword, detail: true}, {
            rownumbers: false,
            fitColumns: true,
            singleSelect: true,
            pagination: true,
            paginationType:'advanced',
            pageSize: pageSize,
            fit: true,
            border: false,
            columns: enum_columns,
            onLoadSuccess: function (data) {
                if (currPage > 1) {
                    gotoGridPageNumber(currPage);
                    currPage = 1;
                }
                var selectRow = undefined;
                data = data || {};
                var leftNode = leftGridSelectNode || {};
                var dataRows = data['rows'] || [];
                for (var item of dataRows) {
                    if (StandardUtils.equals(item.enumId, leftNode.enumId)) {
                        selectRow = item;
                        break
                    }
                }
                if (selectRow) {
                    leftGridSelectNode = undefined;
                    var idx = tempTable.datagrid('getRowIndex', selectRow);
                    if (!StandardUtils.equals(idx, -1)) {
                        tempTable.datagrid('selectRow', idx);
                    }
                }
            },
            onSelect: function (rowIndex, rowData) {
                rightGridSelectNode = rowData;
            }
        });
    }

    //同步右侧，加载到第几页
    function gotoGridPageNumber(pageNum) {
        setTimeout(function () {
            var pageEvent = $.Event('keydown', {keyCode: 13});
            $('.pagination-num').val(pageNum);
            $('.pagination-num').trigger(pageEvent);
        }, 500);
    }


    var list_columns = [[
        {field: 'ck', checkbox: true},
        {field: 'code', title: '枚举值编码',
            formatter: function (value, row, index) {
                return StandardUtils.formatter(value);
            },
            width: 100},
        {field: 'name', title: '枚举值名称',
            formatter: function (value, row, index) {
                return StandardUtils.formatter(value);
            },
            width: 100},
        {field: 'explain', title: '枚举值说明',
            formatter: function (value, row, index) {
                return StandardUtils.formatter(value);
            },
            width: 100},
        {
            field: 'action', title: '操作', width: 100,
            buttons: [
                {text: '编辑', onclick: "$m('dam/metamanage/standard/enumitem').editEnum",}
            ]
        }
    ]];

    var tree_columns = [[
        {field: 'ck', checkbox: true},
        {field: 'code', title: '枚举值编码',
            formatter: function (value, row, index) {
                return StandardUtils.formatter(value);
            },
            width: 100},
        {field: 'name', title: '枚举值名称',
            formatter: function (value, row, index) {
                return StandardUtils.formatter(value);
            },
            width: 100},
        {field: 'explain', title: '枚举值说明',
            formatter: function (value, row, index) {
                return StandardUtils.formatter(value);
            },
            width: 100},
        {field: 'parentName', title: '所属上级', width: 100},
        {
            field: 'action', title: '操作', width: 100,
            buttons: [
                {text: '编辑', onclick: "$m('dam/metamanage/standard/enumitem').editEnum"}
            ]
        }
    ]];
    /**
     * 冒泡提示
     */
    this.formatWeightRuleName = function (value, row, index) {
        value = value ? value : '';
        return "<p style='cursor: pointer'><span class='bubbling_hint' title='" + value + "'>" + value + "</span></p>";
    };
    function resetRightDiv(left, treeWidth) {
        $('#aiot_center_container .layout-panel-center').css({
            width: $(window).width() - left - treeWidth,
            left: treeWidth - 13
        });
        $('#aiot_center_container .panel-body').css({
            width: $(window).width() - left - treeWidth,
        });
        $('#aiot_center_container .panel-body').css('padding-left', '8px');
        $('#aiot_center_container .layout').css({
            width: $(window).width() - left,
        });
        $('#aiot_center_container .panel .datagrid').css({
            width: $(window).width() - left - treeWidth,
        });
        $('#aiot_center_north').css({
            width: $(window).width() - left,
        });
    }

    /**
     * 枚举项名称或编码是否被使用
     * e.g: enumTypeUsed('3f2882fb75ac6cd00175ac8da7500003', 'codeooooo', 'code', function (result) {});
     * @param id 编辑状态，当前检查数据的ID
     * @param value 需要检查的值
     * @param label 检查项 name/code
     * @param callback true 已使用/ false 未使用
     */
    function enumTypeUsed(id, value, label, callback) {
        var params = {id: id, value: value};
        var variables = {type: 'type', label: label};
        Api.checkValueUsed(params, variables, callback);
    }

    /**
     * 页面初始化加载
     */
    function getData(currPage, pageSize, keyword) {
        leftGridSelectNode = undefined;
        top$.messager.progress({
            text: '正在加载，请稍候....',
            interval: 500,
            width: 500
        });
        if (StandardUtils.equals(currPage, 1)) {
            pageKeyword = keyword;
        }
        var $grid = $("#dam_metamanage_standard_grid");
        Api.postTypePage({pageNum: currPage, pageSize: pageSize, keyword: keyword}, function (result) {
            top$.messager.progress('close');
            pageCount = result.total;
            var thisRows = result.rows || [];
            result.rows = thisRows;
            $grid.datagrid('loadData', result);
            if (StandardUtils.equals(currPage, 1)) {
                $('#prevPageBtn').addClass('l-btn-disabled l-btn-plain-disabled');
            } else {
                $('#prevPageBtn').removeClass('l-btn-disabled l-btn-plain-disabled');
            }
            if (pageSize * currPage >= pageCount || thisRows.length < pageSize) {
                $('#nextPageBtn').addClass('l-btn-disabled l-btn-plain-disabled');
            } else {
                $('#nextPageBtn').removeClass('l-btn-disabled l-btn-plain-disabled');
            }
        });
    }

    var leftGridSelectNode;
    var rightGridSelectNode;

    this.onLoadSuccessLeftEnum = function (data) {
        var selectRow = undefined;
        data = data || {};
        var rightNode = rightGridSelectNode || {};
        var dataRows = data['rows'] || [];
        for (var item of dataRows) {
            if (StandardUtils.equals(item.enumId, rightNode.enumId)) {
                selectRow = item;
                break;
            }
        }
        if (selectRow) {
            rightGridSelectNode = undefined;
            var idx = grid.datagrid('getRowIndex', selectRow);
            if (!StandardUtils.equals(idx, -1)) {
                grid.datagrid('selectRow', idx);
            }
        } else {
            reloadItemInitView();
        }
    };

    this.onSelectLeftEnum = function (rowIndex, rowData) {
        leftGridSelectNode = rowData;
        addName = rowData.name;
        var remark = rowData.remark || '';
        //$("#enumName").text( "<span class='bubbling_hintEnum' title='" + addName + "'>枚举项:" + addName + "</span>");
       // $("#enumRemark").html( "<span class='bubbling_hintEnum' title='" + remark + "'>枚举项说明:" + remark + "</span>");
        $("#enumRemark").text( "枚举项说明:" + remark + "");
        $("#enumRemark").attr( "title" ,remark);
        $("#enumRemark").addClass( "bubbling_hintEnum");
        $("#enumName").text( "枚举项:" + addName + "");
        $("#enumName").attr( "title" ,addName);
        $("#enumName").addClass( "bubbling_hintEnum");
        listEnumId = rowData.enumId;
        parentId = '';
        structure = rowData.structure;
        reloadItemTreeAndPage();
    };
    /**
     *上一页
     */
    this.prevPage = function () {
        currPage--;
        if (currPage > 0) {
            getData(currPage, pageSize);
        }
    };
    /**
     *下一页
     */
    this.nextPage = function () {
        if (pageSize * currPage < pageCount) {
            currPage++;
            getData(currPage, pageSize);
        }
    };

    /**
     * 左侧树查询按钮事件
     */
    var leftGridQuery = this.leftGridQuery = function () {
        var value = leftQueryForm.form('getRecord').keyword;
        currPage = 1;
        getData(currPage, pageSize, value);
    };

    function leftGridKeydown(event) {
        if (event.keyCode == "13") {
            leftGridQuery();
        }
    }

    /**
     * 右侧列表查询按钮事件
     */
    var rightGridQuery = this.rightGridQuery = function () {
        var value = rightQueryForm.form('getRecord').keyword;
        showEnumGrid(value);
    };

    function rightGridKeydown(event) {
        if (event.keyCode == "13") {
            rightGridQuery();
        }
    }

    var itemGridQuery = this.itemGridQuery = function () {
        if (StandardUtils.isBlank(listEnumId)) {
            return;
        }
        getItemPage(listEnumId, parentId)
    };

    function itemGridKeydown(event) {
        if (event.keyCode == "13") {
            itemGridQuery();
        }
    }

    /**
     * 枚举值名称或编码是否被使用
     * e.g: enumItemUsed('', 'name', '3f2882fb75ac6cd00175ac81d07d0000', 'name', function (result) {});
     * @param enumId 枚举项ID 必填
     * @param id 编辑状态，当前检查数据的ID
     * @param value 需要检查的值
     * @param label 检查项 name/code
     * @param callback true 已使用/ false 未使用
     */
    function enumItemUsed(id, value, enumId, label, callback) {
        var params = {id: id, value: value, enumId: enumId};
        var variables = {type: 'item', label: label};
        Api.checkValueUsed(params, variables, callback);
    }

    /**
     * 点击新增按钮
     */
    this.add = function (val) {
        addDialog = $.topDialog({
            title: '新增',
            href: addgetTypepath,
            width: 800,
            height: 400,
            onLoad: function () {
            },
            buttons: [
                {
                    text: '关闭',
                    handler: function () {
                        $.topDialog('close', addDialog);
                    }
                },
                {
                    text: '保存',
                    id: 'addSaveBtn',
                    // handler: addSave
                    handler: function () {
                        if (addDialog) {
                            var isValid = addDialog.find("#modelAddForm").form('validate');
                            if (!isValid) {
                                return;
                            }
                            var obj = {};
                            obj.code = addDialog.find('#code').val();
                            obj.name = addDialog.find('#name').val();
                            obj.structure = addDialog.find('input[name="structure"]:checked').val();
                            obj.remark = addDialog.find('#explain').val();
                            addDialog.parent().find('#addSaveBtn').linkbutton('disable');
                            Api.addType(obj, function (result) {
                                if (result) {
                                    $.topDialog('close', addDialog);
                                    top$.messager.promptInfo({
                                        msg: result.message || '保存成功',
                                        icon: 'success',
                                    });
                                    if (val == 1) {
                                        leftGridQuery();
                                    } else {
                                        rightGridQuery();
                                    }
                                }
                            });
                            addDialog.parent().find('#addSaveBtn').linkbutton('enable');
                        }
                    }
                }]
        });
    };
    /**
     * 点击编辑按钮
     */
    this.leftGridEdit = function (record, index) {
        openEditEnumDialog(record, true);
    };

    this.rightGridEdit = function (record, index) {
        openEditEnumDialog(record, false);
    };

    function openEditEnumDialog(record, isLeftGrid) {
        editDialog = $.topDialog({
            title: '编辑',
            href: editgetTypepath,
            width: 800,
            height: 400,
            onLoad: function () {
                var id = record.enumId;
                Api.getTypeOne(id, function (result) {
                    if (result) {
                        editDialog.find("form").form("load", result);
                        if (result.used) {
                            editDialog.find("input[name='structure']").attr("disabled", true);
                        } else {
                            editDialog.find("input[name='structure']").attr("disabled", false);
                        }
                    }
                });
            },
            buttons: [
                {
                    text: '关闭',
                    handler: function () {
                        $.topDialog('close', editDialog);
                    }
                },
                {
                    text: '保存',
                    id: 'editSaveBtn',
                    handler: function () {
                        editEnumSave(editDialog, isLeftGrid);
                    }
                }
            ]
        });
    }

    function editEnumSave(dialog, isLeftGrid) {
        var isValid = dialog.find("#modelAddForm").form('validate');
        if (!isValid) {
            return;
        }
        var obj = {};
        obj.code = dialog.find('#code').val();
        obj.name = dialog.find('#name').val();
        obj.structure = dialog.find('input[name="structure"]:checked').val();
        obj.remark = dialog.find('#remark').val();
        obj.enumId = dialog.find('#enumId').val();
        editDialog.parent().find('#editSaveBtn').linkbutton('disable');
        Api.updateType(obj, function (result) {
            if (result) {
                $.topDialog('close', dialog);
                top$.messager.promptInfo({
                    msg: result.message || '保存成功',
                    icon: 'success'
                });
                if (isLeftGrid) {
                    leftGridQuery();
                } else {
                    rightGridQuery();
                }
            }
        });
        editDialog.parent().find('#editSaveBtn').linkbutton('enable');
    }

    //左侧枚举项移除
    this.leftGridRemove = function (record, index) {
        removeEnumTypeGrid(record, true);
    };

    //右侧枚举项移除
    this.rightGridRemove = function (record, index) {
        removeEnumTypeGrid(record, false);
    };

    function removeEnumTypeGrid(record, isLeft) {
        Api.checkTypeIsUsed(record.enumId, null, function (result) {
            if (result) {
                $.Msg.confirm('提示', '删除枚举项将同步删除枚举值和下发数据字典信息!', function (flag) {
                    if (flag) {
                        removeList(record.enumId, isLeft);
                    }
                });
            } else {
                $.Msg.confirm('提示', '确认删除数据吗？', function (flag) {
                    if (flag) {
                        removeList(record.enumId, isLeft);
                    }
                });
            }
        });
    }

    /**
     * 删除
     */
    function removeList(id, isLeft) {
        Api.delTypeById(id, function (result) {
            if (result) {
                if (isLeft) {
                    leftGridQuery();
                } else {
                    rightGridQuery();
                }
            } else {
                top$.messager.promptInfo({
                    msg: '删除失败。',
                    icon: "error",
                });
            }
        });
    }

    /**
     * 枚举值列表
     */
    function getItemPage(id, pId) {
        var keyword = itemQueryForm.form('getRecord').keyword;
        var params = {
            enumId: id,
            pageSize: 20,
            parentId: pId,
            keyword: keyword
        };
        var tempTable = $('#all_in_one_grid');
        Api.getItemPage(tempTable, params, {
            rownumbers: false,
            fitColumns: true,
            singleSelect: false,
            pagination: true,
            paginationType:'advanced',
            fit: true,
            border: false,
            columns: structure ? tree_columns : list_columns,
            onLoadSuccess: function () {
            }
        });
    }

    /**
     * 枚举列表的树名称模糊匹配
     * @param value
     */
    this.searchModel = function (value) {
        if (value == null || value == '') {
            return;
        }
        var children = tree.tree('getChildren', tree.tree('getRoot').target);
        var pareTotal = 0;
        var pareMap = {};
        var countMap = {};
        // 获取查询结果
        if (children != null && children.length > 0) {
            for (var i = 0; i < children.length; i++) {
                var child = children[i];
                var enumId = child.id;
                var enumName = child.text;
                // 匹配模型信息
                if (enumName.indexOf(value) != -1) {
                    pareTotal = pareTotal + 1;
                    pareMap[enumId] = pareTotal;
                    countMap[pareTotal] = enumId;
                }
            }
        }
        // 循环选中查询结果
        if (pareTotal > 0) {
            var selectedNode = tree.tree("getSelected");
            var nextId = "";
            if (selectedNode == null) {
                nextId = countMap[1];
            } else {
                var selectedIndex = pareMap[selectedNode.id];
                if (selectedIndex == 'undefined' || selectedIndex == null || selectedIndex == '') {
                    nextId = countMap[1];
                } else {
                    if (selectedIndex < pareTotal) {
                        nextId = countMap[selectedIndex + 1];
                    } else {
                        nextId = countMap[1];
                    }
                }
            }
            // 获取节点，展开树结构，选中查询结果节点
            nextNode = tree.tree("find", nextId);
            tree.tree("expandTo", nextNode.target);
            tree.tree("select", nextNode.target);
            tree.scrollTop($(nextNode.target).offset().top - tree.offset().top);
        }
    };

    /**
     * 枚举值说明的格式化
     */
    this.remarkMatter = function (value, row, index) {
        if (row.explain != undefined) {
            value = row.explain;
        } else {
            value = '';
        }
        return "<p style='cursor: pointer'><span class='bubbling_hint' title='" + value + "'>" + value + "</span></p>";
    };

    function checkSelectedLeftGridRows() {
        if (leftGridSelectNode) {
            return true;
        }
        top$.messager.promptInfo({
            msg: '请先选择枚举项',
            icon: 'warning',
        });
        return false;
    }

    /**
     * 枚举值新增
     */
    this.addenumitem = function () {
        if (!checkSelectedLeftGridRows()) {
            return;
        }
        addEnumDialog = $.topDialog({
            title: '新增',
            href: addEnumTypepath,
            width: 800,
            height: 400,
            onLoad: function () {
                if (structure) {
                    addEnumDialog.find("#parent_label").show()
                    addEnumDialog.find("#parentName").val(addName)
                } else {
                    addEnumDialog.find("#parent_label").hide()
                }
            },
            buttons: [
                {
                    text: '关闭',
                    handler: function () {
                        $.topDialog('close', addEnumDialog);
                    }
                },
                {
                    text: '保存',
                    id: 'addSave',
                    handler: addSaveEnum
                }]
        });
    };

    /**
     * 枚举值保存按钮
     */
    function addSaveEnum() {
        if (addEnumDialog) {
            var isValid = addEnumDialog.find("#enumitem_modelAddForm").form('validate');
            if (!isValid) {
                return;
            }
            var obj = {};
            obj.code = addEnumDialog.find('#code').val();
            obj.name = addEnumDialog.find('#name').val();
            obj.parentId = parentId;
            obj.explain = addEnumDialog.find('#explain').val();
            obj.enumId = listEnumId;
            addEnumDialog.parent().find('#addSave').linkbutton('disable');
            Api.addItem(obj, function (result) {
                if (result) {
                    $.topDialog('close', addEnumDialog);
                    top$.messager.promptInfo({
                        msg: result.message || "保存成功",
                        icon: 'success',
                    });
                    reloadItemTreeAndPage();
                }
            });
            addEnumDialog.parent().find('#addSave').linkbutton('enable');
        }
    }

    /**
     * 枚举值编辑
     */
    this.editEnum = function (record, index) {
        editEnumDialog = $.topDialog({
            title: '编辑',
            href: editEnumTypepath,
            width: 800,
            height: 400,
            onLoad: function () {
                if (structure) {
                    editEnumDialog.find("#parent_label").show();
                    editEnumDialog.find("#parentName").val(addName)
                } else {
                    editEnumDialog.find("#parent_label").hide()
                }
                Api.getItemById(record.itemId, function (result) {
                    if (result) {
                        editEnumDialog.find("form").form("load", result);
                    }
                });
            },
            buttons: [
                {
                    text: '关闭',
                    handler: function () {
                        $.topDialog('close', editEnumDialog);
                    }
                },
                {
                    text: '保存',
                    id: 'editSave',
                    handler: editSaveEnum
                }]
        })
    };

    /**
     * 枚举值编辑保存按钮
     */
    function editSaveEnum() {
        if (editEnumDialog) {
            var isValid = editEnumDialog.find("#enumitem_modelAddForm").form('validate');
            if (!isValid) {
                return;
            }
            var obj = {};
            obj.code = editEnumDialog.find('#code').val();
            obj.name = editEnumDialog.find('#name').val();
            obj.explain = editEnumDialog.find('#explain').val();
            obj.itemId = editEnumDialog.find('#itemId').val();
            editEnumDialog.parent().find('#editSave').linkbutton('disable');
            Api.updateItem(obj, function (result) {
                if (result) {
                    $.topDialog('close', editEnumDialog);
                    top$.messager.promptInfo({
                        msg: result.message || '保存成功',
                        icon: 'success',
                    });
                    reloadItemTreeAndPage();
                }
            });
            editEnumDialog.parent().find('#editSave').linkbutton('enable');
        }
    }

    this.removeEnumitem = function () {
        var selections = enumgrid.datagrid('getSelections');
        if (selections.length < 1) {
            top$.messager.promptInfo({
                msg: '请至少选择一条记录进行操作!',
                icon: 'warning',
            });
            return;
        }
        var ids = [];
        for (var i = 0; i < selections.length; i++) {
            ids.push(selections[i].itemId);
        }
        Api.preDelItem(ids, function (result) {
            if (result) {
                $.Msg.confirm('提示', '该枚举值下已有子级，若删除，将同时删除它们的子级', function (flag) {
                    if (flag) {
                        removeItemByIds(ids)
                    }
                });
            } else {
                $.Msg.confirm('提示', '确认删除数据吗？', function (flag) {
                    if (flag) {
                        removeItemByIds(ids)
                    }
                });
            }
        });
    };

    /**
     * 枚举值删除按钮
     */
    function removeItemByIds(ids) {
        Api.delItem(ids, function (result) {
            if (result) {
                top$.messager.promptInfo({
                    msg: '删除成功。',
                    icon: "success",
                });
                reloadItemTreeAndPage();
            } else {
                top$.messager.promptInfo({
                    msg: '删除失败。',
                    icon: "error",
                });
            }
        });
    }

    /**
     * 枚举值导入
     */
    this.importData = function () {
        if (!checkSelectedLeftGridRows()) {
            return;
        }
        Api.checkTypeIsUsed(listEnumId, false, function (result) {
            if (result) {
                $.Msg.confirm('提示', '该枚举项下已存在枚举值，如果继续将全覆盖当前已有值。', function (flag) {
                    if (flag) {
                        importDialog()
                    }
                });
            } else {
                importDialog()
            }
        });
    };

    /**
     * 打开导入的弹窗
     */
    function importDialog() {
        importDataDialog = $.topDialog({
            title: '数据导入',
            href: importData,
            width: 600,
            height: 400,
            onLoad: function () {
            },
            buttons: [
                {
                    text: '关闭',
                    handler: function () {
                        $.topDialog('close', importDataDialog);
                    }
                },
                {
                    text: '保存',
                    id: 'editSave',
                    handler: uploadData
                }]
        })
    }

    /**
     * 数据导入的保存
     */
    function uploadData() {
        var form = importDataDialog.find("#dam_standard_upload_id");
        var formFile = form.find('#file');
        var filePath = formFile.val();
        if (StandardUtils.isNotEmpty(filePath)) {
            importDataDialog.parent().find('#editSave').linkbutton('disable');
            var url = Ajax.getResources('enumItem', 'upload', {enumId: listEnumId})
           //构建formData,通过ajax上传文件
            var formData = new FormData();
            $.each(formFile[0].files, function (i, item) {
                formData.append("file", item, item.name);
            });
            $.ajax({
                url: url,
                type: 'post',
                data: formData,
                processData: false,
                contentType: false,
                cache: false,
                success: function (data) {
                    if(!data.data && !data.success){
                        $.Msg.alert('提示', data.message);
                        importDataDialog.parent().find('#editSave').linkbutton('enable');
                        importDataDialog.parent().find('#file').val("");
                        return;
                    }
                    $.Msg.alert('提示', data.data.message);
                    if (data.data.success) {
                        $.topDialog('close', importDataDialog);
                    }
                    reloadItemTreeAndPage();
                },
                error: function (rs) {
                    if (rs.status == 0 && 'rejected' == rs.state()) {
                        $.Msg.alert('提示', '文件可能被删除、移动、修改，请重新选择上传！');
                        formFile.val('');
                    }
                },
                complete: function (data) {
                    top$.messager.progress('close');
                    importDataDialog.parent().find('#editSave').linkbutton('enable');
                }
            });

            // Api.upload(form, listEnumId, function (result) {
            //     $.Msg.alert('提示', result.message);
            //     if (result.success) {
            //         $.topDialog('close', importDataDialog);
            //     }
            //     reloadItemTreeAndPage();
            // });
            // importDataDialog.parent().find('#editSave').linkbutton('enable');
        }else{
            top$.messager.promptInfo({
                msg: '请选择导入文件',
                icon: 'warning',
            });
        }
    }

    /**
     * 模板下载
     */
    this.exportTemplateData = function () {
        var form = $('<form>').attr('method', 'post').hide();
        $('body').append(form);
        Api.downloadExcelTemp(form, listEnumId, function (result) {
            form.delete();
        });
    };

    /**
     * 导出数据
     */
    this.downLoadData = function () {
        if (!checkSelectedLeftGridRows()) {
            return;
        }
        var form = $('<form>').attr('method', 'post').hide();
        $('body').append(form);
        var enumId = listEnumId;
        Api.download(form, enumId, function (result) {
            form.delete();
        });
    };
    /**
     * 枚举值抽取
     */
    this.extractEnumitem = function () {
        if (!checkSelectedLeftGridRows()) {
            return;
        }
        Api.checkTypeIsUsed(listEnumId, false, function (result) {
            if (result) {
                $.Msg.confirm('提示', '该枚举项下已存在枚举值，如果继续将全覆盖当前已有值。', function (flag) {
                    if (flag) {
                        getExtractDicSelect()
                    }
                });
            } else {
                getExtractDicSelect()
            }
        });
    };

    /**
     * 获取下拉字典的值
     */
    function getExtractDicSelect() {
        itemExtractDialog = $.topDialog({
            title: '枚举值提取',
            href: itemExtract,
            width: 600,
            height: 300,
            onLoad: function () {
                Api.getDicBasicList(structure, function (result) {
                    itemExtractDialog.find('#name').combobox('loadData', result);
                });
            },
            buttons: [
                {
                    text: '关闭',
                    handler: function () {
                        $.topDialog('close', itemExtractDialog);
                    }
                },
                {
                    text: '提取',
                    id: 'editSave',
                    handler: extractData
                }]
        })
    }

    function extractData() {
        var itemId = itemExtractDialog.find('#name').combobox('getValue');
        if (StandardUtils.isBlank(itemId)) {
            return;
        }
        openProgresss();
        Api.itemExtractFormDic(itemId, listEnumId, function (result) {
            $.topDialog('close', itemExtractDialog);
            closeProgresss()
            top$.messager.promptInfo({
                msg: result.message,
                icon: 'success',
            });
            reloadItemTreeAndPage();
        });
    }

    /**
     * 关闭进度条
     */
    var closeProgresss = function () {
        try {
            top$.messager.progress('close');
        } catch (e) {
            $.messager.progress('close');
        }
    };
    /**
     * 打开进度条
     */
    var openProgresss = function () {
        try {
            top$.messager.progress({
                text: '正在处理，请稍候....',
                interval: 500,
                width: 500
            });
        } catch (e) {
            $.messager.progress({
                text: '正在处理，请稍候....',
                interval: 500,
                width: 500
            });
        }
    };

    function reloadItemTreeAndPage() {
        Api.getItemTree(listEnumId, function (result) {
            var enumConf = result.enumConf || {};
            var curStructure = enumConf.structure;
            showEnumItemGrid(curStructure);
            if (enumConf.structure) {
                var treeData = result.treeData || {};
                tree.tree('loadData', [treeData]);
                if (parentId) {
                    // 查找一个节点然后返回它
                    var node = tree.tree('find', parentId);
                    if (node) {
                        tree.tree('select', node.target);
                        getItemPage(listEnumId, parentId);
                    } else {
                        getItemPage(listEnumId, parentId);
                    }
                }
                $('.layout-button-left:eq(1)').on('click',function () {
                    if (!bnn2) {
                        bnn2 = true;
                        $('.layout-button-right:first').on('click', function (e) {
                            setTimeout(function () {
                                if($('#aiot_center_west').width()<100){
                                    $('#aiot_center_west').panel('resize', {
                                        width: 238,
                                        left: 0
                                    });
                                    $('#aiot_center_center').panel('resize', {
                                        width: $('#aiot_center_north').width() - 238,
                                        left: 238
                                    });
                                }
                            },500);

                        });
                    }
                })

            } else {
                getItemPage(listEnumId, parentId);
            }
        });
    }

    function reloadItemInitView() {
        addName = '';
        $("#enumName").html("枚举项:");
        $("#enumRemark").html("枚举项说明:");
        listEnumId = '';
        parentId = '';
        structure = '';
        showEnumItemGrid(false);
        /**
         * 枚举值列表
         */
        var tempTable = $('#all_in_one_grid');
        //用假数据做页面重置
        Api.getItemPage(tempTable, {enumId: '3f28760362af500d6036fa00'}, {
            rownumbers: false,
            fitColumns: true,
            singleSelect: false,
            pagination: true,
            paginationType:'advanced',
            fit: true,
            border: false,
            pageSize: 20,
            columns: list_columns,
            onLoadSuccess: function () {
            }
        });
    }

    /**
     * 处理当前展示的枚举值区域的结构
     * @param treeFlag structure
     */
    function showEnumItemGrid(treeFlag) {
        if (treeFlag) {
            resetRightDiv(500, 240);
            $('#aiot_center_west').css('display', '');
            $('#aiot_center_west').parent().css('display', '');
            $('#aiot_center_west').panel('resize',{width: 240});

        } else {
            resetRightDiv(500, 0);
            $('#aiot_center_west').css('display', 'none');
            $('#aiot_center_west').parent().css('display', 'none');
            $('#aiot_center_west').panel('resize',{width: '0.5%'});
        }
    }

    this.enumItemTreeLoadSuccess = function (node, data) {
        if (parentId) {
            // 查找一个节点然后返回它
            var node = tree.tree('find', parentId);
            if (node) {
                tree.tree('select', node.target);
            } else {
                treeSelectRootNode();
            }
        } else {
            treeSelectRootNode();
        }

        function treeSelectRootNode() {
            var root = tree.tree('getRoot');
            tree.tree('select', root.target);
        }
    };

    /**
     * 点击树节点事件
     * @param node
     */
    this.enumItemTreeSelected = function (node) {
        addName = node.text;
        parentId = node.id;
        getItemPage(listEnumId, parentId);
    };
});
