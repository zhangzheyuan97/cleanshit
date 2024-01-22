$m('dam/metamanage/privilege/field/mongo', function () {
    //=======对象引用声明=======start=========
    var fieldModulePath = '/dam-metamanage/api/metamanage/fieldprivilege/';
    var modelMongoModulePath = "/dam-metamanage/api/metamanage/fieldprivilege/mongo/";

    var mongoGrid;
    // view全选按钮状态
    var allViewChecked;
    // listable全选按钮状态
    var allListableChecked;

    this.selectModelTree = function (node) {
        var permission = node.attributes.permission;
        $('#privilegeDatamanageIndex').layout('panel', 'center').panel({
            href: fieldModulePath + 'view/mongo*fieldList',
            onLoad: function () {
                loadFieldData(node);
                if (!permission) {
                    $(':radio').attr('disabled', 'disabled');
                    $("input[type='checkbox']").prop('disabled', 'disabled');
                }
            }
        });
    }

    /**
     * 加载选中模型的数据
     */
    var loadFieldData = function (node) {
        mongoGrid = $("#dam_privilege_field_mongo_grid");
        var privilegeId = node.id;
        getMongoFields(privilegeId, function (data) {
            var first;
            var fields;
            if (data && data.length > 0) {
                first = data[0];
                fields = first['children'];
            }
            mongoGrid.treegrid({
                data: fields,
                onLoadSuccess: function () {
                    showAllButtonStatus();
                }
            });
        })
    }

    /**
     * 获取Mongo有权限的字段信息
     * @param privilegeId
     * @param successCallBack
     */
    var getMongoFields = function (privilegeId, successCallBack) {
        $.ajax({
            url: modelMongoModulePath + "findMongoFieldDetail",
            type: "POST",
            data: {privilegeId: privilegeId},
            dataType: "JSON",
            success: function (result) {
                if (result && result['success']) {
                    successCallBack(result['data'])
                }

            }
        })
    }

    /**
     * 查看权限
     * @param value
     * @param row
     * @param index
     */
    this.viewPrivilege = function (value, row, index) {
        var html = '';
        var privilegeType = "view";
        var id = row.attributes.modelField;
        var dbType = row.attributes.modelVerFieldDTO.dataType;
        if (row.attributes.viewable == '1') {
            html += '<input type="checkbox" role="id" checked="checked" id="hasPrivilege_' + id + '_' + privilegeType + '" class="chooseBtn" ' +
                'onchange="$m(\'dam/metamanage/privilege/field/mongo\').showChangeButton(this,\'' + id + '\', \'' + dbType + '\', \'' + privilegeType + '\')">';
        } else {
            allViewChecked = false;
            html += '<input type="checkbox" role="id" id="hasPrivilege_' + row.id + '_' + privilegeType + '" ' +
                'onchange="$m(\'dam/metamanage/privilege/field/mongo\').showChangeButton(this,\'' + id + '\', \'' + dbType + '\', \'' + privilegeType + '\')">';
        }

        return html;
    }


    /**
     * 管理权限
     * @param value
     * @param row
     * @param index
     */
    this.listShowPrivilege = function (value, row, index) {
        var privilegeType = "listable";
        var id = row.attributes.modelField;
        var dbType = row.attributes.modelVerFieldDTO.dataType;
        var result = '';
        //可设置列表显示条件：1显示字段；2.非文档；3.非数组文档；
        if (dbType != 'object' && dbType != 'object_array') {
            if (row.attributes.listable == '1') {
                result += '<input type="checkbox" role="hasPrivilege" checked="checked" id="hasPrivilege_' + id + '_' + privilegeType + '"  ' +
                    'onchange="$m(\'dam/metamanage/privilege/field/mongo\').checkIsShow(this,\'' + id + '\', \'' + dbType + '\',  \'' + privilegeType + '\')">';
            } else {
                allListableChecked = false;
                result += '<input type="checkbox" role="hasPrivilege" id="hasPrivilege_' + id + '_' + privilegeType + '"  ' +
                    'onchange="$m(\'dam/metamanage/privilege/field/mongo\').checkIsShow(this,\'' + id + '\', \'' + dbType + '\', \'' + privilegeType + '\')">';
            }
        } else {
            row.attributes.listable = '0';
            result += '<input type="checkbox" disabled = "disabled" id="hasPrivilege_' + id + '_' + privilegeType + ')">';
        }
        return result;
    }

    this.allPrivilege = function (obj, privilegeType) {
        $("#button_view").off('.datagrid');
        $("#button_listable").off('.datagrid');
        if (privilegeType == 'view') {
            checkViewHeader(obj);
        } else {
            var rows = mongoGrid.treegrid('getData');
            var checked = obj.checked;
            allShowHandler(checked, rows);
        }

    }

    var allShowHandler = function (checked, children) {
        for (var i = 0; i < children.length; i++) {
            var root = children[i];
            checkShowStatus(checked, root);
            if (root.children) {
                allShowHandler(checked, root.children);
            }
        }
    }

    var checkShowStatus = function (checked, root) {
        var id = root.attributes.modelField;
        var view = ($('#hasPrivilege_' + id + '_' + 'view').is(':checked') ? true : false);
        if (view && checked) {
            //勾选
            $('#hasPrivilege_' + id + '_' + 'listable').prop('checked', true);
            $('#hasPrivilege_' + id + '_' + 'listable').removeProp('disabled');
            $('#hasPrivilege_' + id + '_' + 'listable').click(function () {
                this.check = !this.check;
            });
            root.attributes.listable = '1';
        } else {
            $('#hasPrivilege_' + id + '_' + 'listable').prop('checked', false);
            $('#hasPrivilege_' + id + '_' + 'listable').prop('disabled', 'disabled');
            root.attributes.listable = '0';
        }
    }

    /**
     * 列表显示全选
     * @param checked
     * @param privilegeType
     */
    var checkViewHeader = function (obj) {
        var checked = obj.checked;
        setHeaderButton(checked);
        var rows = mongoGrid.treegrid('getData');
        recuCheckOrCancel(checked, rows);
    }

    var setHeaderButton = function (check) {
        //查看
        if (check) {
            $('#button_listable').click(function () {
                return true;
            });
        } else {
            $('#button_listable').prop("checked", false);
            $('#button_listable').click(function () {
                this.check = !this.check;
            });
        }
    }

    /**
     * 递归 选中或取消 模板字段 子节点
     * @param children
     * @returns
     */
    function recuCheckOrCancel(checked, children) {
        for (var i = 0; i < children.length; i++) {
            var root = children[i];
            checkOrCancel(checked, root);
            if (root.children) {
                recuCheckOrCancel(checked, root.children);
            }
        }
    }

    /**
     *取消或者选中checkbox
     * @param item
     * @param root
     * @param curDialog
     * @returns
     */

    function checkOrCancel(checked, root) {
        var id = root.attributes.modelField;
        var dbType = root.attributes.modelVerFieldDTO.dataType;
        //查看
        if (checked) {
            $('#hasPrivilege_' + id + '_' + 'view').prop("checked", true);
            if (dbType != 'object' && dbType != 'object_array') {
                $('#hasPrivilege_' + id + '_' + 'listable').removeProp('disabled');
                $('#hasPrivilege_' + id + '_' + 'listable').click(function () {
                    this.check = !this.check;
                });
            } else {
                $('#hasPrivilege_' + id + '_' + 'listable').prop("checked", false);
                $('#hasPrivilege_' + id + '_' + 'listable').prop('disabled', 'disabled');
            }
        } else {
            $('#hasPrivilege_' + id + '_' + 'view').prop('checked', false);
            $('#hasPrivilege_' + id + '_' + 'listable').prop('checked', false);
            $('#hasPrivilege_' + id + '_' + 'listable').prop('disabled', 'disabled');
        }
    }

    /**
     * 按钮触发事件
     * @param obj
     * @param id
     * @param dbType
     * @param privilegeType
     */
    this.showChangeButton = function (obj, id, dbType, privilegeType) {
        var row = mongoGrid.treegrid('find', id);
        var check = $(obj).is(':checked');
        //选中
        if (check) {
            recuCheckOrCancelParent(row, privilegeType, check);
            //是一级字段 不是复杂类型  且不是敏感字段  设置 列表字段可选
            if (dbType != 'object' && dbType != 'object_array') {
                $('#hasPrivilege_' + id + '_' + 'listable').removeProp('disabled');
                $('#hasPrivilege_' + id + '_' + 'listable').click(function () {
                    this.check = !this.check;
                });
            } else if (dbType == 'object' || dbType == 'object_array' || dbType == 'simple_array') {
                // 选择 复杂类型
                var children = mongoGrid.treegrid('getChildren', row.id);
                //递归选中子节点
                recuCheckOrCancel(check, children, privilegeType);
            }
        } else {
            if (dbType != 'object' && dbType != 'object_array') {
                $('#hasPrivilege_' + id + '_' + 'listable').prop('checked', false);
                $('#hasPrivilege_' + id + '_' + 'listable').prop("disabled", "disabled");
            } else if (dbType == 'object' || dbType == 'object_array' || dbType == 'simple_array') {
                // 选择 复杂类型
                var children = mongoGrid.treegrid('getChildren', row.id);
                //递归选中
                recuCheckOrCancel(check, children, privilegeType);
            }
        }
        showAllButtonStatus();
    }

    this.checkIsShow = function (obj, id, dbType, privilegeType) {
        var check = $(obj).is(':checked');
        if (check) {
            $('#hasPrivilege_' + id + '_' + 'listable').prop('checked', true);
        } else {
            $('#hasPrivilege_' + id + '_' + 'listable').prop('checked', false);
        }
    }

    /**
     * 判断全选按钮是否被选中
     */
    var showAllButtonStatus = function () {
        allViewChecked = true;
        allListableChecked = true;
        var allNode = mongoGrid.treegrid('getData');
        checkAllButtonStatus(allNode);
        if (allViewChecked) {
            $('#button_view').prop("checked", true);
        } else {
            $('#button_view').prop("checked", false);
        }
        if (allListableChecked) {
            $('#button_listable').prop("checked", true);
        } else {
            $('#button_listable').prop("checked", false);
        }
    }


    /**
     *  递归判断每个checkbox状态
     * @param node
     * @param allManageChecked
     * @param allViewChecked
     */
    var checkAllButtonStatus = function (node) {
        for (var i = 0; i < node.length; i++) {
            var id = node[i].id;
            var view = ($('#hasPrivilege_' + id + '_' + 'view').is(':checked') ? "1" : "0");
            var listable = ($('#hasPrivilege_' + id + '_' + 'listable').is(':checked') ? "1" : "0");
            if (view == 0) {
                allViewChecked = false;
            }
            if (listable == 0) {
                allListableChecked = false;
            }
            if (node[i].children) {
                checkAllButtonStatus(node[i].children);
            }
        }
    }


    /**
     * 递归选中父节点
     * @param row
     * @param privilegeType
     * @param check
     */
    function recuCheckOrCancelParent(row, privilegeType, check) {
        checkOrCancel(check, row, privilegeType);
        var parent = mongoGrid.treegrid('getParent', row.id);
        if (parent) {
            recuCheckOrCancelParent(parent, privilegeType, check);
        }
    }

    /**
     * 保存操作
     * @param modelNode
     */
    this.saveField = function (modelNode, roleId) {
        var param = {};
        var fieldList = [];
        //mongodb 树节点 通过递归解析出页面全部字段
        var data = mongoGrid.treegrid('getData');
        var temp = [];
        fieldList = recuParseData(data, temp);
        var id = modelNode.id;
        var modelInfo = modelNode.attributes.modelInfo;
        param = {
            id: id,
            modelInfo: modelInfo,
            roleId: roleId,
            fieldPrivilegeDetailList: fieldList
        }
        $.ajax({
            url: fieldModulePath + 'changePrivilege',
            async: false,
            type: 'POST',
            data: JSON.stringify(param),
            dataType: 'JSON',
            contentType: 'application/json',
            async: false,
            success: function (data) {
                if (data.success) {
                    loadFieldData(modelNode);
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

    /**
     * 递归获取需要保存的数据
     * @param data
     * @param ret
     * @returns
     */
    function recuParseData(data, ret) {
        for (var i = 0; i < data.length; i++) {
            var id = data[i].attributes.modelField;
            data[i].attributes.viewable = ($('#hasPrivilege_' + id + '_' + 'view').is(':checked') ? "1" : "0");
            data[i].attributes.listable = ($('#hasPrivilege_' + id + '_' + 'listable').is(':checked') ? "1" : "0");
            ret.push(data[i].attributes);
            if (data[i].children) {
                recuParseData(data[i].children, ret);
            }
        }
        return ret;
    }
});
