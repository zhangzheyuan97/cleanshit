$m('dam/metamanage/privilege/button',function () {
    //=======对象引用声明=======start=========
    var roleTree;
    var datatypeTree;
    var prvilegeTable;
    var allPrivilege = [];
    var allPrivilegeEnum = [];
    //=======对象引用声明=======end=========

    //=======相关访问路径声明=======start=========
    var modulePath = '/dam-metamanage/api/metamanage/buttonprivilege/';
    //获取日志路径
    var operateLogUrl = '?module=' + encodeURIComponent(TempoUtils.getPathLists().module.name) + '&menu=' + encodeURIComponent(TempoUtils.getPathLists().menu.name);
    // 公共角色树路径
    this.roleTreePath = '/dam-metamanage/api/metamanage/privilege/roleTree/roleTreeAll';
    var datatypeTreePath = modulePath + 'datatypeModelTree' + operateLogUrl;
    var privilegePath = modulePath + 'changePrivilege';
    var getButtonPrivilegePath = modulePath + 'buttonPrivilege';
    var batchPrivilegePath = modulePath + 'batchButtonPrivilege';
    var getButtonNumByModelIdPath = modulePath + 'getButtonNumByModelId';
    //=======相关访问路径声明=======end=========

    /**
     * 初始化选择对象
     */
    $(function () {
        roleTree = $('#dam_metamanage_role_tree');
        datatypeTree = $('#dam_metamanage_datatype_tree');
        prvilegeTable = $('#dam_metamanage_button_privilege_table');
    });


    /**
     * 角色树名称模糊匹配
     * @param value
     */
    this.searchRole = function (value) {
        if(value==null || value==""){
            return;
        }
        var selectedNode = roleTree.tree("getSelected");
        if(selectedNode!=null){
              $m('dam/metamanage/privilege/button').selectRoleTree(selectedNode);
        }
    }


    /**
     * 查询按钮事件
     */
    this.searchDataType = function (value) {
        if(value==null || value==""){
            return;
        }
        var selectedNode = datatypeTree.tree("getSelected");
        if(selectedNode!=null){
            $m('dam/metamanage/privilege/button').selectDatatypeTree(selectedNode);
        }
    };
    /**
     * 角色树点击事件
     * @param node
     */
    this.selectRoleTree = function(node) {
        if (node == null || node.id == '-1') {
            $("#dataTypeTreeDiv").css("display",'none');
            $("#buttonListDiv").css("display",'none');
            $("#dataTypeBaseDiv").css("display",'block');
        } else {
            $("#dataTypeBaseDiv").css("display",'none');
            $("#dataTypeTreeDiv").css("display",'block');
            $("#buttonListDiv").css("display",'none');
            datatypeTree.tree({url: datatypeTreePath + operateLogUrl + "&roleId=" + node.id,loadFilter:$m('dam/metamanage/privilege/button').loadFilterTree});
        }
    }

    /**
     * 模型分类树点击事件
     * @param node
     */
    this.selectDatatypeTree = function (node) {
        var modelId;
        if(node.attributes != undefined){
            modelId = node.attributes.modelInfo;
        }
        var roleId = roleTree.tree('getSelected').id;
        if (modelId != undefined || modelId != null) {
            $("#baseDiv").css("display",'none');
            $("#buttonListDiv").css("display",'block');
            $m('dam/metamanage/privilege/button').getButtonNumByModelId(modelId,roleId);
            $m('dam/metamanage/privilege/button').showButtonPrivilege();
        } else {
            $("#buttonListDiv").css("display",'none');
            $("#baseDiv").css("display",'block');
        }
    }

    /**
     * 按钮权限展示
     */
    this.showButtonPrivilege =function () {
        var modelId = datatypeTree.tree('getSelected').id;
        var roleId = roleTree.tree('getSelected').id;
        $.ajax({
            url: getButtonPrivilegePath + operateLogUrl,
            data: {'modelId': modelId, 'roleId': roleId},
            type: 'post',
            async: false,
            success: function (data) {
                if (data.success) {
                    var privilegeList = data.data;
                    var length = privilegeList.length;
                    if (length == allPrivilege.length) {
                        $('#button_all').prop("checked", true);
                    } else {
                        $('#button_all').prop("checked", false);
                    }
                    if (length > 0) {
                        // 选择按钮
                        for(var i = 0; i < length; i++){
                            var privilege = privilegeList[i].privilege;
                            allPrivilege.splice(privilege);
                            $("#button_" + privilege.toLowerCase()).val('1');
                            $("#input_" + privilege.toLowerCase()).prop("checked", true);
                            $("#button_" + privilege.toLowerCase()).removeClass("unchecked_button_style");
                            $("#button_" + privilege.toLowerCase()).addClass("checked_button_style");
                       }
                    }
                    if (allPrivilege.length > 0) {
                        for(var j = 0; j < allPrivilege.length; j++) {
                            var privilege = allPrivilege[j].toLowerCase()
                            $("#button_" + privilege).val('0');
                            $("#input_" + privilege).prop("checked", false);
                            $("#button_" +privilege).addClass("unchecked_button_style");
                        }
                    }
                } else {
                    $.Msg.alert('提示', '操作失败');
                }
            }
        });
    }

    /**
     * 按钮权限改变
     * @param index
     */
    this.changePrivilege = function (index) {
        var modelId = datatypeTree.tree('getSelected').id;
        var roleId = roleTree.tree('getSelected').id;
        var type;
        var buttonValue = $("#button_" + index.toLowerCase()).val();
        if (buttonValue == '1') {
            type = false;
            $("#button_" + index.toLowerCase()).val('0');
            $("#input_" + index.toLowerCase()).prop("checked", false);
        } else {
            type = true;
            $("#button_" + index.toLowerCase()).val('1');
            $("#input_" + index.toLowerCase()).prop("checked", true);
        }
        $.ajax({
            url: privilegePath + operateLogUrl,
            data: {'modelId': modelId, 'roleId': roleId, 'privilege':index ,'type':type},
            async: false,
            type: 'post',
            success: function (data) {
                if (data.success) {
                    if (buttonValue == '1') {
                        $("#button_" + index.toLowerCase()).val('0');
                        $("#input_" + index.toLowerCase()).prop("checked", false);
                        $("#button_" + index.toLowerCase()).removeClass("checked_button_style");
                        $("#button_" + index.toLowerCase()).addClass("unchecked_button_style");
                    } else {
                        $("#button_" + index.toLowerCase()).val('1');
                        $("#input_" + index.toLowerCase()).prop("checked", true);
                        $("#button_" + index.toLowerCase()).removeClass("unchecked_button_style");
                        $("#button_" + index.toLowerCase()).addClass("checked_button_style");
                    }
                    $m('dam/metamanage/privilege/button').allButtonStatus();
                } else {
                    $.Msg.alert('提示', '操作失败');
                }
            }
        });
    }

    /**
     * 判断当前全选按钮状态
     */
    this.allButtonStatus = function () {
        var num = 0;
        for (var i = 0; i< allPrivilegeEnum.length ; i++) {
            if ($("#button_" + allPrivilegeEnum[i].toLowerCase()).val() == '1') {
                num ++;
            }
        }
        if (num == allPrivilegeEnum.length) {
            $("#button_all").prop("checked", true);
        } else {
            $("#button_all").prop("checked", false);
        }
    }
    /**
     * 全选按钮改变
     */
    this.changeAllPrivilege = function () {
        var modelId = datatypeTree.tree('getSelected').id;
        var roleId = roleTree.tree('getSelected').id;
        var type = $('#button_all').is(':checked');
        $.ajax({
            url: batchPrivilegePath + operateLogUrl,
            data: {'modelId': modelId, 'roleId': roleId ,'type':type},
            type: 'post',
            async: false,
            success: function (data) {
                if (data.success) {
                    $m('dam/metamanage/privilege/button').getButtonNumByModelId(modelId);
                    $m('dam/metamanage/privilege/button').showButtonPrivilege();
                } else {
                    $.Msg.alert('提示', '操作失败');
                }
            }
        });
    }

    /**
     * 根据模型ID获取按钮
     * @param String
     */
    this.getButtonNumByModelId = function (modelId,roleId) {
        $.ajax({
            url: getButtonNumByModelIdPath,
            data: {'modelId': modelId,roleId:roleId},
            type: 'post',
            async: false,
            success: function (data) {
                if (data.success) {
                    allPrivilege = [];
                    allPrivilegeEnum = [];
                    var html = "";
                    var trNum = 0;
                    var button = data.data;
                    var privilegeName;
                    var privilegeNum;
                    var buttonId;
                    var inputId;
                    if (button.length > 0) {
                        for (var i = 0; i < button.length; i++) {
                            privilegeName = button[i].name;
                            privilegeNum = button[i].value;
                            allPrivilege.push(privilegeNum);
                            allPrivilegeEnum.push(privilegeNum);
                            buttonId = 'button_' + privilegeNum.toLowerCase();
                            inputId = 'input_' + privilegeNum.toLowerCase();
                            if (i  == trNum * 3) {
                                html += '<tr>';
                            }
                            html += '<td><input id="' + inputId + '" type="checkbox" onchange="$m(\'dam/metamanage/privilege/button\').changePrivilege(\''+ privilegeNum +'\')"/>' +
                                '<button id="' + buttonId + '"  class="unchecked_button_style" value="">' + privilegeName + '</button></td>';
                            if (i + 1 == (trNum + 1) * 3  ) {
                                html += '</tr>';
                                trNum ++;
                            }
                        }
                        prvilegeTable.html(html);
                    }
                } else {
                    $.Msg.alert('提示', '操作失败');
                }
            }
        });
    }

    var loadFilterTree = this.loadFilterTree = function(data, parent){
        addModelIcon(data);
        return data;
    }

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
});
