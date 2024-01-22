$m('dam/metamanage/privilege/field', function () {
    //=======对象引用声明=======start=========
    var roleTree;
    var modelTree;
    var fieldGrid;
    var addModelDialog;
    // view全选按钮状态
    var allViewChecked;
    // edit全选按钮状态
    var allEditChecked;
    // listable全选按钮状态
    var allListableChecked;

    var dbType;
    var createMode;
    //=======相关访问路径声明=======start=========
    var modulePath = '/dam-metamanage/api/metamanage/fieldprivilege/';
    this.roleTreePath = '/dam-metamanage/api/metamanage/privilege/roleTree/roleTreeAll';
    //获取日志路径
    var operateLogUrl = '?module=' + encodeURIComponent(TempoUtils.getPathLists().module.name) + '&menu=' + encodeURIComponent(TempoUtils.getPathLists().menu.name);
    //=======相关访问路径声明=======end=========
    /**
     * 静态资源访问路径dam/metamanage/privilege/field
     * @type {string}
     */
    var staticPath = '/dam-metamanage/dam/metamanage/privilege/field/';
    /**
     * 初始化选择对象
     */
    $(function () {
        roleTree = $('#dam_privilege_field_role_tree');
        modelTree = $('#dam_privilege_field_model_tree');
    });


    var fieldPage = function (privilegeId, permission) {
        fieldGrid.datagrid({
            url: modulePath + "findFieldDetail",
            async: false,
            queryParams: {privilegeId: privilegeId},
            onLoadSuccess: function () {
                //创建方式为抽取，并且数据库类型为hive时，编辑隐藏
                if (createMode == '1' || dbType == 'hive') {
                    fieldGrid.datagrid('hideColumn', 'fieldEdit');
                }
                showAllButton();
                // 无权限的所有按钮不可编辑
                if (!permission) {
                    $(':radio').attr('disabled', 'disabled');
                    $("input[type='checkbox']").prop('disabled', 'disabled');
                }
            }
        });
    }

    var loadFieldPrivilege = function (privilegeId) {
        var dataList;
        $.ajax({
            url: modulePath + 'findFieldPrivilege',
            data: JSON.stringify({id: privilegeId}),
            async: false,
            type: 'POST',
            dataType: 'JSON',
            contentType: 'application/json',
            success: function (data) {
                if (data.success) {
                    dataList = data.data[0];
                } else {
                    $.Msg.alert('提示', '操作失败');
                }
            }
        });
        return dataList;
    }


    /**
     * 添加模型
     */
    this.addModel = function () {
        var roleId = roleTree.tree("getSelected").id;
        if (roleId == -1) {
            $.Msg.alert('提示', '不能选中根节点!');
            return false;
        }
        addModelDialog = $.topDialog({
            title: "添加模型",
            cache: false,
            resizable: true,
            modal: true,
            width: 500,
            height: 600,
            href: modulePath + "view/addModel",
            onLoad: function () {
                var $tree = addModelDialog.find('#dam_privilege_model_tree');
                // 获取角色id，模型树默认选中角色已分配字段权限的模型
                var param = {roleId: roleId};
                loadModelTree($tree, modulePath + 'getModelTree', param, true);
            },
            buttons: [
                {
                    text: '关闭',
                    handler: function () {
                        $.topDialog('close', addModelDialog);
                    }
                },
                {
                    text: '保存',
                    id: 'addSaveBtn',
                    handler: addModelSave
                }]
        });

    }

    /**
     * 保存模型
     */
    var addModelSave = function () {
        var roleNode = roleTree.tree("getSelected");
        var selected = tree.instance.tree('getChecked');
        var modelIds = [];
        $.each(selected, function (i, item) {
            if (item.attributes) {
                var model = item.attributes.model;
                if (model) {
                    modelIds.push(model.id);
                }
            }
        })
        if (modelIds.length === 0) {
            top$.messager.promptInfo({msg: '请至少选择一个模型', icon: 'warning'});
            return false;
        }
        //保存按钮置灰，防止重复提交
        addModelDialog.parent().find('#addSaveBtn').linkbutton('disable');
        $.ajax({
            url: modulePath + 'saveModel',
            type: 'POST',
            data: {roleId: roleNode.id, modelIds: modelIds.toString()},
            success: function (data) {
                if (data.success) {
                    $.topDialog('close', addModelDialog);
                    selectRoleTree(roleTree.tree("getSelected"));
                }
                addModelDialog.parent().find('#addSaveBtn').linkbutton('enable');
            }
        });
    }

    /**
     * 加载模型树
     * @param modelIds
     */
    var loadModelTree = function ($tree, url, param, checkBoxFlag) {
        $.ajax({
            url: url,
            type: 'POST',
            data: param,
            success: function (data) {
                if (data.success) {
                    tree.init($tree, data.data, checkBoxFlag);
                    //var root = modelTree.tree('getRoot');
                    var children = modelTree.tree('getChildren');
                    if (children.length > 0) {
                        $('#clearBtn').linkbutton('enable');
                    }
                }
            }
        });
    }


    var tree = {
        instance: undefined,
        init: function (treeJq, data, checkBoxFlag) {
            this.instance = treeJq;
            this.instance.tree({
                checkbox: checkBoxFlag,
                loadFilter:function(data,parent){
                    addModelIcon(data)
                    return data;
                },
                data: data
            });
            //鼠标移入事件：去除根节点
            if (!checkBoxFlag) {
                $('#dam_privilege_field_model_tree div[node-id != "-1"]').mouseenter(function () {
                    var modelId = $(this).attr("node-id");
                    var modelName = $(this).find(".tree-title").text();
                    var node = $('#dam_privilege_field_model_tree').tree('find', modelId);
                    var permission = node.attributes.permission;
                    $(this).append("<img class='removeModel'" +
                        " onclick=\"$m(\'dam/metamanage/privilege/field\').removeModel(\'" + modelId + "\', \'" + modelName + "\', \'" + permission + "\')\"" +
                        "  src='" + staticPath + "image/logout.png' " +
                        "style='left:275px;margin-top: 8px;position: sticky;'>")
                })

                //鼠标离开事件：去除根节点
                $('#dam_privilege_field_model_tree div[node-id != "-1"]').mouseleave(function () {
                    $(".removeModel").remove()
                })
            }
        },
        onDblClick: function (node) {
            var state = node.state;
            if (state == "closed") {
                $(this).tree("expand", node.target);
            } else {
                $(this).tree("collapse", node.target);
            }
        }
    };

    /**
     * 通过递归的方式给模型进行图标的增加
     */
    function addModelIcon(data){
        for(var i=0;i<data.length;i++){
            var node = data[i];
            if(node.attributes != undefined && node.attributes.modelInfo != undefined){
                node.iconCls = "icon-model";
            }
            if(node.children != undefined && node.children.length > 0){
                addModelIcon(node.children);
            }
        }
    }

    this.removeModel = function (modelId, modelName, permission) {
        var roleNode = roleTree.tree("getSelected");
        var roleName = roleNode.attributes.name;
        $.Msg.confirm('提示', '是否确认取消角色为【' + roleName + '】对【' + modelName + '】模型的数据管理/查看权限?', function (r) {
            if (r) {
                if (permission) {
                    $.ajax({
                        url: modulePath + 'clearAllPrivilege' + operateLogUrl,
                        data: {id: modelId},
                        type: 'post',
                        success: function (data) {
                            if (data.success) {
                                selectRoleTree(roleNode);
                                $("#field_view_area").hide();
                            } else {
                                $.Msg.alert('提示', '操作失败');
                            }
                        }
                    });
                } else {
                    $.Msg.alert('提示', '权限不足，无法删除');
                }
            }
        });
    }

    /**
     * 选择某个角色触发事件
     */
    var selectRoleTree = this.selectRoleTree = function (node) {
        //按钮置灰
        $('#clearBtn').linkbutton('disable');
        $('#saveBtn').linkbutton('disable');
        if (node.id != -1) {
            $("#model_view_area").show();
            var param = {roleId: node.id};
            loadModelTree(modelTree, modulePath + 'findModelTreeByRole', param, false);
        } else {
            $("#model_view_area").hide();
        }
        // 切换角色，清空字段权限区域
        $("#field_view_area").hide();
    }

    /**
     * 角色树模糊匹配
     */
    this.searchRoleTree = function (value) {
        searchTree(value, 'role');
    };

    /**
     * 新增模型树模糊匹配
     */
    this.searchModel = function (value) {
        searchTree(value, 'modelList');
    };

    /**
     * 新增模型树模糊匹配
     */
    this.searchAddModelTree = function (value) {
        searchTree(value, 'addModel');
    };

    /**
     * 查询树上匹配的节点
     * */
    var searchTree = function (value, type) {
        var tree;
        if (type == "role") {
            tree = roleTree;
        } else if (type == "addModel") {
            tree = addModelDialog.find('#dam_privilege_model_tree');
        } else {
            tree = modelTree;
        }
        if (value == null || value == '') {
            return;
        }
        var children = tree.tree('getChildren');
        var pareTotal = 0;
        var pareMap = {};
        var countMap = {};
        // 获取查询结果
        if (children != null && children.length > 0) {
            for (var i = 0; i < children.length; i++) {
                var child = children[i];
                var dataTypeId = child.id;
                var dataTypeName = child.text;
                // 匹配模型信息
                if (dataTypeName.indexOf(value) != -1) {
                    pareTotal = pareTotal + 1;
                    pareMap[dataTypeId] = pareTotal;
                    countMap[pareTotal] = dataTypeId;
                }
            }
        }
        // 循环选中查询结果
        /*if (pareTotal > 0) {
            var selectedNode = tree.tree("getSelected");
            var nextId = "";
            if (selectedNode == null) {
                nextId = countMap[1];
            } else {
                var selectedIndex = pareMap[selectedNode.id];
                if (selectedIndex == 'undefined' || selectedIndex == null || selectedIndex == '') {
                    nextId = countMap[1];
                } else {
                    if (selectedIndex <= pareTotal) {
                        nextId = countMap[selectedIndex];
                    } else {
                        nextId = countMap[1];
                    }
                }
            }
            // 获取节点，展开树结构，选中查询结果节点
            var nextNode = tree.tree("find", nextId);
            tree.tree("expandTo", nextNode.target);
            tree.tree("select", nextNode.target);
            tree.parent().scrollTop($(nextNode.target).offset().top - tree.parent().offset().top + tree.parent().scrollTop());
        }*/
    };


    /**
     * 清空权限
     */
    this.clearPrivilege = function () {
        var roleNode = roleTree.tree("getSelected");
        $.Msg.confirm('提示', '确认要删除该该角色下所有模型的数据管理权限吗?', function (r) {
            if (r) {
                $.ajax({
                    url: modulePath + 'clearAllPrivilege' + operateLogUrl,
                    data: {roleId: roleNode.id},
                    type: 'post',
                    success: function (data) {
                        if (data.success) {
                            selectRoleTree(roleNode);
                            $("#field_view_area").hide();
                        } else {
                            $.Msg.alert('提示', '操作失败');
                        }
                    }
                });
            }
        });
    }

    var selectModelTree = this.selectModelTree = function (node) {
        if (node.id != -1) {
            $("#field_view_area").show();
            $('#saveBtn').linkbutton('enable');
            dbType = node.attributes.modelVersionDTO.database.type;
            createMode = node.attributes.modelVersionDTO.createMode;
            var permission = node.attributes.permission;
            if (dbType == 'mongodb') {
                $m('dam/metamanage/privilege/field/mongo').selectModelTree(node);
            } else if (dbType == 'hbase') {
                $m('dam/metamanage/privilege/field/hbase').selectModelTree(node);
            } else {
                $('#privilegeDatamanageIndex').layout('panel', 'center').panel({
                    href: modulePath + 'view/fieldList',
                    onLoad: function () {
                        fieldGrid = $('#dam_privilege_field_fieldlist_grid');
                        fieldPage(node.id, permission);
                    }
                });
            }
        } else {
            $("#field_view_area").hide();
            $('#saveBtn').linkbutton('disable');
        }
    };
    /**
     * 属性列表格式化
     */
    this.formatFieldName = function (value, row, index) {
        return row.modelVerFieldDTO.busiName;
    }

    /**
     *  级联授权
     * @param id
     * @param index
     */
    this.changePrivilege = function (id, privilege) {
        var view;
        var edit;
        var listable;
        var viewChecked = $('#hasPrivilege_' + id + '_' + 'view').is(':checked');
        var editChecked = $('#hasPrivilege_' + id + '_' + 'edit').is(':checked');
        var listableChecked = $('#hasPrivilege_' + id + '_' + 'listable').is(':checked');
        view = viewChecked ? "1" : "0";
        edit = editChecked ? "1" : "0";
        listable = listableChecked ? "1" : "0";
        var param = {
            ids: id,
            viewable: view,
            editable: edit,
            listable: listable,
        }
        $.ajax({
            url: modulePath + 'changePrivilege' + operateLogUrl,
            data: param,
            async: false,
            type: 'POST',
            success: function (data) {
                if (data.success) {
                    fieldPage(modelTree.tree("getSelected").id);
                    showAllButton(fieldGrid);
                } else {
                    $.Msg.alert('提示', '操作失败');
                }
            }
        });
    }

    /**
     * 查看权限
     * @param value
     * @param row
     * @param index
     */
    this.viewPrivilege = function (value, row, index) {
        var html = '';
        var privilegeType = 'view';
        var id = row.modelField;
        var sys = row.sys;
        var busiType;
        if (sys != 1 && dbType != "hbase") {
            busiType = row.modelVerFieldDTO.fieldDataType.busiType;
        }
        if (row.viewable == '1') {
            html += '<input type="checkbox" role="id"  checked="checked" onclick="$m(\'dam/metamanage/privilege/field\').showChangeButton(this,\'' + id + '\', \'' + sys + '\', \'' + privilegeType + '\', \'' + busiType + '\')" id="hasPrivilege_' + id + '_' + privilegeType + '">';
        } else {
            allViewChecked = false;
            html += '<input type="checkbox" role="id" onclick="$m(\'dam/metamanage/privilege/field\').showChangeButton(this,\'' + id + '\', \'' + sys + '\', \'' + privilegeType + '\', \'' + busiType + '\')"  id="hasPrivilege_' + id + '_' + privilegeType + '">';
        }

        return html;
    }


    /**
     * 管理权限
     * @param value
     * @param row
     * @param index
     */
    this.editPrivilege = function (value, row, index) {
        var html = '';
        var privilegeType = "edit";
        var id = row.modelField;
        var sys = row.sys;
        var busiType;
        if (sys != 1 && dbType != "hbase") {
            busiType = row.modelVerFieldDTO.fieldDataType.busiType;
        }
        if (row.editable == '1') {
            html += '<input type="checkbox" role="hasPrivilege" checked="checked" onclick="$m(\'dam/metamanage/privilege/field\').showChangeButton(this,\'' + id + '\', \'' + sys + '\', \'' + privilegeType + '\', \'' + busiType + '\')"  id="hasPrivilege_' + id + '_' + privilegeType + '" >';
        } else {
            allEditChecked = false;
            if (sys == 0) {
                html += '<input type="checkbox" role="hasPrivilege" onclick="$m(\'dam/metamanage/privilege/field\').showChangeButton(this,\'' + id + '\', \'' + sys + '\', \'' + privilegeType + '\', \'' + busiType + '\')"  id="hasPrivilege_' + id + '_' + privilegeType + '" >';
            } else {
                html += '<input type="checkbox" role="hasPrivilege" onclick="return false"  id="hasPrivilege_' + id + '_' + privilegeType + '" >';
            }

        }

        return html;
    }

    this.showChangeButton = function (obj, id, sys, privilegeType, busiType) {
        var viewCheck = $('#hasPrivilege_' + id + '_' + 'view').is(':checked') ? true : false;
        fieldDataButton(viewCheck, id, sys, privilegeType, busiType);
        showAllButton();
    }

    /**
     * 管理权限
     * @param value
     * @param row
     * @param index
     */
    this.listShowPrivilege = function (value, row, index) {
        var html = '';
        var privilegeType = "listable";
        var id = row.modelField;
        var sys = row.sys;
        var busiType;
        if (sys != 1 && dbType != "hbase") {
            busiType = row.modelVerFieldDTO.fieldDataType.busiType;
        }
        if (row.listable == '1') {
            html += '<input type="checkbox" role="hasPrivilege" checked="checked" onclick="$m(\'dam/metamanage/privilege/field\').showChangeButton(this,\'' + id + '\', \'' + sys + '\', \'' + privilegeType + '\', \'' + busiType + '\')"  id="hasPrivilege_' + id + '_' + privilegeType + '" >';
        } else {
            allListableChecked = false;
            html += '<input type="checkbox" role="hasPrivilege" onclick="$m(\'dam/metamanage/privilege/field\').showChangeButton(this,\'' + id + '\', \'' + sys + '\', \'' + privilegeType + '\', \'' + busiType + '\')"   id="hasPrivilege_' + id + '_' + privilegeType + '">';
        }
        return html;
    }

    /**
     * 保存事件
     */
    this.saveField = function () {
        var modelNode = modelTree.tree("getSelected");
        var param;
        var fieldList = [];
        var roleId = roleTree.tree("getSelected").id;
        var id = modelNode.id;
        var modelInfo = modelNode.attributes.modelInfo;
        var permission = modelNode.attributes.permission;
        //置灰500毫秒
        $('#saveBtn').linkbutton('disable');
        setTimeout(()=> {
            $('#saveBtn').linkbutton('enable');
        }, 500)
        if (dbType == 'hbase') {
            $m('dam/metamanage/privilege/field/hbase').saveField(modelNode, roleId);
        } else if (dbType == 'mongodb') {
            $m('dam/metamanage/privilege/field/mongo').saveField(modelNode, roleId);
        } else {
            var rows = fieldGrid.datagrid("getRows");
            $(rows).each(function (index, element) {
                element.viewable = ($('#hasPrivilege_' + element.modelField + '_' + 'view').is(':checked') ? "1" : "0");
                // hive模型或抽取的模型无编辑权限
                if (dbType == 'hive' || createMode == '1') {
                    element.editable = null;
                } else {
                    element.editable = ($('#hasPrivilege_' + element.modelField + '_' + 'edit').is(':checked') ? "1" : "0");
                }
                element.listable = ($('#hasPrivilege_' + element.modelField + '_' + 'listable').is(':checked') ? "1" : "0");
                fieldList.push(element);
            });
            if (dbType == 'hive') {
                var manageData = $('input[name="manageData"]:checked').val();
                param = {
                    id: id,
                    modelInfo: modelInfo,
                    roleId: roleId,
                    manageData: manageData,
                    fieldPrivilegeDetailList: fieldList
                }
            } else {
                param = {
                    id: id,
                    modelInfo: modelInfo,
                    roleId: roleId,
                    fieldPrivilegeDetailList: fieldList
                }
            }

            $.ajax({
                url: modulePath + 'changePrivilege' + operateLogUrl,
                async: false,
                type: 'POST',
                data: JSON.stringify(param),
                dataType: 'JSON',
                contentType: 'application/json',
                async: false,
                success: function (data) {
                    if (data.success) {
                        fieldPage(modelTree.tree("getSelected").id, permission);
                        top$.messager.promptInfo({
                            msg:'保存成功',
                            icon:'success',
                        });
                    } else {
                        top$.messager.promptInfo({
                            msg:'保存失败',
                            icon:'error',
                        });
                    }
                }
            });
        }
    }


    /**
     * 权限按钮状态展示
     */
    var showAllButton = this.showAllButton = function () {
        //判断全选按钮是否被选中
        if (dbType == 'hbase') {
            fieldGrid = $("#dam_privilege_field_hbase_grid");
        }
        checkAllButtonStatus(fieldGrid);
        if (allViewChecked) {
            $('#button_view').prop("checked", true);
        } else {
            $('#button_view').prop("checked", false);
        }
        if (allEditChecked) {
            $('#button_edit').prop("checked", true);
        } else {
            $('#button_edit').prop("checked", false);
        }
        if (allListableChecked) {
            $('#button_listable').prop("checked", true);
        } else {
            $('#button_listable').prop("checked", false);
        }
        if (dbType == 'hive') {
            $("#manager_data").show();
            var privilegeId = modelTree.tree("getSelected").id;
            var privilegeData = loadFieldPrivilege(privilegeId);
            if(privilegeData.modelVersionDTO.createMode === 1){
                $('input[name="manageData"]').eq(1).prop('checked', true);
                $('input[name="manageData"]').prop('disabled', true);
            } else {
                var manageData = $('input[name="manageData"]:checked').val();
                if (privilegeData.manageData == 1 || manageData == 1) {
                    $('input[name="manageData"]').eq(0).prop('checked', true);
                } else {
                    $('input[name="manageData"]').eq(1).prop('checked', true);
                }
            }
        } else {
            $("#manager_data").hide();
        }
    }

    var checkAllButtonStatus = function (fieldGrid) {
        var fieldList = fieldGrid.datagrid('getRows');
        allViewChecked = true;
        allEditChecked = true;
        allListableChecked = true;
        var editCount = 0;
        var viewCount = 0;
        var listAbleCount = 0;
        var editTotal = 0;
        $.each(fieldList, function (i, item) {
            var id = item.modelField;
            var sys = item.sys;
            var busiType;
            if (sys != 1 && dbType != "hbase") {
                busiType = item.modelVerFieldDTO.fieldDataType.busiType;
            }
            var view = ($('#hasPrivilege_' + id + '_' + 'view').is(':checked') ? "1" : "0");
            var edit = ($('#hasPrivilege_' + id + '_' + 'edit').is(':checked') ? "1" : "0");
            var listable = ($('#hasPrivilege_' + id + '_' + 'listable').is(':checked') ? "1" : "0");
            //非系统字段统计,判断编辑总数
            if (sys == 0) {
                editTotal++;
            }
            if (view == '1') {
                viewCount++;
            }
            //view处于选中状态 并且非系统字段
            if (view == '1' && edit == '1' && sys == 0) {
                editCount++;
            }
            if (view == '1' && (listable == '1' || busiType == 'CLOB')) {
                listAbleCount++;
            }
            var viewCheck = $('#hasPrivilege_' + id + '_' + 'view').is(':checked') ? true : false;
            fieldDataButton(viewCheck, id, sys, 'view', busiType);
        });
        if (viewCount != fieldList.length) {
            allViewChecked = false;
        }
        if (editCount != editTotal) {
            allEditChecked = false;
        }
        if (listAbleCount != fieldList.length) {
            allListableChecked = false;
        }
    }

    this.allPrivilege = function (privilegeType) {
        if (dbType == 'hbase') {
            fieldGrid = $("#dam_privilege_field_hbase_grid");
        }
        $("#button_view").off('.datagrid');
        $("#button_edit").off('.datagrid');
        $("#button_listable").off('.datagrid');
        var headerRow = fieldGrid.prev().find(".datagrid-header-row");
        var listableHeader;
        listableHeader = headerRow.find("td").eq(2);
        var viewCheck = listableHeader.find("input").is(":checked");
        if (privilegeType == 'view') {
            listableHeader = headerRow.find("td").eq(2);
        }
        if (privilegeType == 'edit') {
            listableHeader = headerRow.find("td").eq(3);
        }
        if (privilegeType == 'listable') {
            listableHeader = headerRow.find("td").eq(4);
        }
        var target = listableHeader.find("input");
        var checked = target.is(":checked");
        checkHeader(privilegeType, viewCheck, checked);
    }

    /**
     * 数据模板表头单选框选择事件
     * @param tempGride
     * @param name
     * @param value
     */
    var checkHeader = function (privilegeType, viewCheck, checked) {
        var rows = fieldGrid.datagrid("getRows");
        //判断编辑和列表全选状态
        if (privilegeType == 'view') {
            setHeaderButton(viewCheck, privilegeType);
        }
        $(rows).each(function (index, element) {
            var id = element.modelField;
            var sys = element.sys;
            var busiType;
            if (sys != 1 && dbType != "hbase") {
                busiType = element.modelVerFieldDTO.fieldDataType.busiType;
            }
            // 判断字段是否有查看权限
            var viewable = $('#hasPrivilege_' + id + '_' + 'view').is(':checked') ? true : false;
            //查看
            if (privilegeType == 'view') {
                fieldDataButton(viewable && checked, id, sys, privilegeType, busiType);
            }
            if (privilegeType == 'view') {
                //判断编辑和列表显示的状态
                if (checked) {
                    $('#hasPrivilege_' + id + '_' + 'view').prop("checked", true);
                } else {
                    $('#hasPrivilege_' + id + '_' + 'view').prop("checked", false);
                }
            }
            if (viewable) {
                //编辑:系统字段不可设置
                if (privilegeType == 'edit') {
                    if (sys == 1) {
                        $('#hasPrivilege_' + id + '_' + 'edit').click(function () {
                            return false;
                        });
                    } else {
                        if (checked) {
                            $('#hasPrivilege_' + id + '_' + 'edit').prop("checked", true);
                        } else {
                            $('#hasPrivilege_' + id + '_' + 'edit').prop("checked", false);
                        }
                    }

                } else if (privilegeType == 'listable') {
                    // CLOB字段不能列表显示
                    if (busiType == 'CLOB') {
                        $('#hasPrivilege_' + id + '_' + 'listable').prop("checked", false);
                    } else {
                        if (checked) {
                            $('#hasPrivilege_' + id + '_' + 'listable').prop("checked", true);
                        } else {
                            $('#hasPrivilege_' + id + '_' + 'listable').prop("checked", false);
                        }
                    }
                }
            } else {
                // 无查看权限，则列表及编辑权限不可选
                $('#hasPrivilege_' + id + '_' + 'edit').prop("checked", false);
                $('#hasPrivilege_' + id + '_' + 'listable').prop("checked", false);
            }
        });
    }

    /**
     * 设置表头状态
     * @param check
     * @param privilegeType
     */
    var setHeaderButton = function (check, privilegeType) {
        //查看
        if (check) {
            $('#button_edit').click(function () {
                return true;
            });
            $('#button_listable').click(function () {
                return true;
            });
        } else {
            $('#button_edit').prop("checked", false);
            $('#button_listable').prop("checked", false);
            $('#button_edit').click(function () {
                this.check = !this.check;
            });
            $('#button_listable').click(function () {
                this.check = !this.check;
            });
        }
    }

    var fieldDataButton = function (check, id, sys, privilegeType, busiType) {
        if (check) {
            //编辑处于可用状态
            if (sys == 0) {
                $('#hasPrivilege_' + id + '_' + 'edit').click(function () {
                    return true;
                });
            } else {
                $('#hasPrivilege_' + id + '_' + 'edit').click(function () {
                    this.check = !this.check;
                });
            }
            //列表显示处于可用状态
            // CLOB字段不能列表显示
            if (dbType != "hbase" && busiType == 'CLOB') {
                $('#hasPrivilege_' + id + '_' + 'listable').click(function () {
                    return false;
                });
            } else {
                $('#hasPrivilege_' + id + '_' + 'listable').click(function () {
                    return true;
                });
            }
        } else {
            $('#hasPrivilege_' + id + '_' + 'edit').prop("checked", false);
            $('#hasPrivilege_' + id + '_' + 'listable').prop("checked", false);
            $('#hasPrivilege_' + id + '_' + 'edit').click(function () {
                this.check = !this.check;
            });
            //列表显示处于可用状态
            $('#hasPrivilege_' + id + '_' + 'listable').click(function () {
                this.check = !this.check;
            });
        }
    }
});
