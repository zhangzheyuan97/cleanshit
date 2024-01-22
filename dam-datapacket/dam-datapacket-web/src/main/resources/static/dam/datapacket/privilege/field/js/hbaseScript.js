$m('dam/metamanage/privilege/field/hbase',function () {
    //=======对象引用声明=======start=========
    var fieldModulePath = '/dam-metamanage/api/metamanage/fieldprivilege/';
    var modelHbaseModulePath = "/dam-metamanage/api/metamanage/fieldprivilege/hbase/";
    var hbasefieldGrid;
    var privilegeId;
    /**
     * 初始化选择对象
     */
    this.selectModelTree = function (node) {
        var permission = node.attributes.permission;
        privilegeId = node.id;
        $('#privilegeDatamanageIndex').layout('panel', 'center').panel({
            href: fieldModulePath + 'view/hbase*fieldList',
            onLoad: function () {
                hbasefieldGrid = $("#dam_privilege_field_hbase_grid");
                fieldPage();
                if (!permission) {
                    $(':radio').attr('disabled', 'disabled');
                    $("input[type='checkbox']").prop('disabled', 'disabled');
                }
            }
        });
    }

    /**
     * 列簇名称格式化
     * @param value
     * @param row
     * @param index
     * @returns {*}
     */
    this.formatFamilyName = function (value, row, index) {
       return row.modelVerHbaseFamilyDTO.familyName;
    }

    var fieldPage = function () {
        hbasefieldGrid.datagrid({
            url: modelHbaseModulePath + "findHbaseFieldDetail",
            async: false,
            queryParams: {privilegeId: privilegeId},
            onLoadSuccess : function () {
                $m('dam/metamanage/privilege/field').showAllButton();
            }
        });
    }

    this.saveField = function (modelNode, roleId) {
        var rows = hbasefieldGrid.datagrid("getRows");
        var param;
        var fieldList = [];
        $(rows).each(function (index, element) {
            element.viewable = ($('#hasPrivilege_' + element.modelField + '_' + 'view').is(':checked') ? "1" : "0");
            element.editable = ($('#hasPrivilege_' + element.modelField + '_' + 'edit').is(':checked') ? "1" : "0");
            fieldList.push(element);

        });
        var id = modelNode.id;
        var modelInfo = modelNode.attributes.modelInfo;
        param = {
            id: id,
            modelInfo: modelInfo,
            roleId: roleId,
            fieldPrivilegeDetailList : fieldList
        }

        $.ajax({
            url: fieldModulePath + 'changePrivilege',
            async: false,
            type: 'POST',
            data:JSON.stringify(param),
            dataType: 'JSON',
            contentType: 'application/json',
            async: false,
            success: function (data) {
                if (data.success) {
                    fieldPage();
                    //全选按钮是否被选中
                    $m('dam/metamanage/privilege/field').showAllButton();
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



    this.allPrivilege  = function (privilegeType) {
        $("#button_view").off('.datagrid');
        $("#button_edit").off('.datagrid');
        var headerRow = hbasefieldGrid.prev().find(".datagrid-header-row");
        var listableHeader;
        if(privilegeType == 'view'){
            listableHeader = headerRow.find("td").eq(2);
        }
        if(privilegeType == 'edit'){
            listableHeader = headerRow.find("td").eq(3);
        }
        var target = listableHeader.find("input");
        var checked = target.is(":checked");
        checkHeader(privilegeType, checked);
    }


    /**
     * 数据模板表头单选框选择事件
     * @param tempGride
     * @param name
     * @param value
     */
    var checkHeader = function (privilegeType,checked) {
        var rows = hbasefieldGrid.datagrid("getRows");
        $(rows).each(function (index, element) {
            var id = element.id;
            //查看
            if(privilegeType == 'view') {
                if (checked) {
                    $('#hasPrivilege_' + id + '_' + 'view').prop("checked", true);
                } else {
                    $('#hasPrivilege_' + id + '_' + 'view').prop("checked", false);
                }
            }
            //编辑
            if(privilegeType == 'edit') {
                if (checked) {
                    $('#hasPrivilege_' + id + '_' + 'edit').prop("checked", true);
                } else {
                    $('#hasPrivilege_' + id + '_' + 'edit').prop("checked", false);
                }
            }
        });
    }
});
