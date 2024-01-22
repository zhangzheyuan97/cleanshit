$m('dam/metamanage/privilege/datasource/database', function () {
    //=======对象引用声明=======start=========
    var tree;
    var grid;
    var queryForm;

    //=======对象引用声明=======end=========

    //=======相关访问路径声明=======start=========
    var modulePath = '/dam-metamanage/api/metamanage/datasource/database/privilege/';
    //获取日志路径
    var operateLogUrl = '?module=' + encodeURIComponent(TempoUtils.getPathLists().module.name) + '&menu=' + encodeURIComponent(TempoUtils.getPathLists().menu.name);
    // 公共角色树路径
    this.roleTreePath = '/dam-metamanage/api/metamanage/privilege/roleTree/roleTreeAll';
    this.pagePath = modulePath + 'list' + operateLogUrl;
    var authorizeOpenPath = modulePath + 'openAuhorize';
    var authorizeClosePath = modulePath + 'closeAuhorize';
    var roleId = '';
    //=======相关访问路径声明=======end=========
    /**
     * 初始化选择对象
     */
    $(function () {
        tree = $('#dam_metamanage_role_tree');
        grid = $('#dam_metamanage_database_privilege_grid');
        queryForm = $('#dam_metamanage_database_privilege_queryform');
    });

    /**
     * 授权按钮初始化
     * @param value
     * @param row
     * @param index
     * @returns {string}
     */
    this.authorizeFormatter = function (value, row, index) {
        var html = '';
        if (row.hasPrivilege == '1') {
            html += '<input type="checkbox" role="hasPrivilege" checked="checked" id="hasPrivilege_' +index + '"  ' +
                'onchange="$m(\'dam/metamanage/privilege/datasource/database\').changePrivilege(\''+ row.id +'\', \''+index+'\')">';
        } else {
            html += '<input type="checkbox" role="hasPrivilege" id="hasPrivilege_' +index + '"  ' +
                'onchange="$m(\'dam/metamanage/privilege/datasource/database\').changePrivilege(\''+row.id +'\', \''+index+'\')">';
        }
        return html + '<label for="hasPrivilege_' + index +'" ></label>';
    };

    /**
     * 保存数据库权限
     */
    this.save = function () {
        var databaseIds = [];
        var roleId = tree.tree('getSelected').id;
        var rows = grid.datagrid('getRows');
        // 遍历列表获取选择的数据库id
        for (var i = 0; i < rows.length; i++) {
            var isChecked = $('#hasPrivilege_' + i).is(':checked');
            if (isChecked) {
                databaseIds.push(rows[i].id);
            }
        }
        $.ajax({
            url: modulePath + 'save' +operateLogUrl,
            data: {'roleId': roleId, 'databaseIds': $.toJSON(databaseIds)},
            type: 'post',
            success: function (data) {
                if (data.success) {
                    grid.datagrid('reload', {roleId: roleId});
                    top$.messager.promptInfo({
                        msg:'保存成功',
                        icon:'success',
                    });
                } else {
                    top$.messager.promptInfo({
                        msg:'操作失败',
                        icon:'error',
                    });
                }
            }
        });
    }
    /**
     * 权限改变事件
     * @param record
     * @param index
     */
    this.changePrivilege = function (row, index) {
        // // var row = grid.datagrid('getRows').get(index);
        // var roleId = tree.tree('getSelected').id;
        // var value = $('#hasPrivilege_' + index).is(':checked');
    };


    /**
     * 增加权限
     */
    var authorizeOpen = function (dataBaseId, roleId, enable) {
        var url;
        if (enable == true) {
            url = authorizeOpenPath;
        } else {
            url = authorizeClosePath;
        }
        $.ajax({
            url: url + operateLogUrl,
            data: {'databaseId': dataBaseId, 'roleId': roleId},
            type: 'post',
            success: function (data) {
                if (data.success) {
                    grid.datagrid({
                        url: pagePath,
                        queryParams: {roleId: roleId}
                    });
                } else {
                    $.Msg.alert('提示', '操作失败');
                }
            }
        });
    };

    /**
     * 角色树名称模糊匹配
     * @param value
     */
    this.searchRole = function (value) {
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
                var roleId = child.id;
                var roleName = child.text;
                // 匹配模型信息
                if (roleName.indexOf(value) != -1) {
                    pareTotal = pareTotal + 1;
                    pareMap[roleId] = pareTotal;
                    countMap[pareTotal] = roleId;
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
            tree.scrollTop($(nextNode.target).offset().top - tree.offset().top);
            grid.datagrid('load', {deptId: nextId});
        }
    }

    /**
     * 查询按钮事件
     */
    this.query = function () {
        $('#roleId').val(roleId);
        var param = queryForm.form('getRecord');
        if (queryForm.form('validate')) {
            $.ajax({
                url: $m('dam/metamanage/privilege/datasource/database').pagePath,
                type: 'post',
                data: param,
                success: function (data) {
                    grid.datagrid('loadData', data.data);
                    if (data.data.rows.length > 0) {
                        $("#typeTotal").text('共' + data.data.total + '条数据');
                    }
                }
            });
            grid.datagrid('load', queryForm.form('getRecord'));
        }
    };

    /**
     * 点击树节点事件
     * @param node
     */
    this.selectTree = function (node) {
        if (node.id == '-1') {
            $('#save_Btn').hide();
        } else {
            $('#save_Btn').show();
            roleId = node.id;
            $.ajax({
                url: $m('dam/metamanage/privilege/datasource/database').pagePath,
                data: {roleId: roleId},
                type: 'post',
                success: function (data) {
                    grid.datagrid('loadData', data.data);
                    if (data.data.rows.length > 0) {
                        $("#typeTotal").text('共' + data.data.total + '条数据');
                    }
                }
            });
        }
    }
});
