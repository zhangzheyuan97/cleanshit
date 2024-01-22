$m('dam/metamanage/privilege/datatype', function () {
    //=======对象引用声明=======start=========
    var tree;
    var grid;
    var queryForm;
    var treeGrid;

    //=======对象引用声明=======end=========

    //=======相关访问路径声明=======start=========
    var modulePath = '/dam-metamanage/api/metamanage/datatypeprivilege/';
    //获取日志路径
    var operateLogUrl = '?module=' + encodeURIComponent(TempoUtils.getPathLists().module.name) + '&menu=' + encodeURIComponent(TempoUtils.getPathLists().menu.name);
    // 公共角色树路径
    this.roleTreePath = '/dam-metamanage/api/metamanage/privilege/roleTree/roleTreeAll';
    this.treeGridList = modulePath + 'treeGridList' + operateLogUrl;
    var cascadeAssignPath = modulePath + 'cascadeAssign';
    //=======相关访问路径声明=======end=========

    /**
     * 初始化选择对象
     */
    $(function () {
        tree = $('#dam_metamanage_role_tree');
        treeGrid = $('#dam_metamanage_datatype_privilege_grid');
        queryForm = $('#dam_metamanage_datatype_privilege_queryform');
    });


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
            treeGrid.treegrid('load', {roleId: nextId});
        }
    }

    /**
     * 查询按钮事件
     */
    this.query = function () {
        var value = queryForm.form('getRecord').name;
        if (value == null || value == '') {
            return;
        }
        var children = treeGrid.treegrid('getChildren', treeGrid.treegrid('getRoot').target);
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
            var selectedNode = treeGrid.treegrid("getSelected");
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
            var nextNode = treeGrid.treegrid("find", nextId);
            treeGrid.treegrid("expandTo", nextNode.id);
            treeGrid.treegrid("select", nextNode.id);
            // treeGrid.scrollTop($(nextNode.target).offset().top - treeGrid.offset().top);
            $("#typeTotal").text('共' + pareTotal + '条数据');
        } else {
            treeGrid.treegrid('clearSelections');
        }
    };

    /**
     * 点击树节点事件
     * @param node
     */
    var selectTree = this.selectTree = function (node) {
        var roleId = node.id;
        $.ajax({
            url: $m('dam/metamanage/privilege/datatype').treeGridList,
            data: {roleId: roleId},
            type: 'post',
            success: function (data) {
                treeGrid.treegrid('loadData', data.data.rows);
                if (data.data.rows.length > 0) {
                    $("#typeTotal").text('共' + data.data.total + '条数据');
                }
            }
        });
    }

    /**
     * 分类权限
     * @param value
     * @param row
     * @param index
     */
    this.datatpyePrivilege = function (value, row, index) {
        var html = '';
        if (row.attributes != null) {
            var permission = row.attributes.permission;
            var rolePermission = row.attributes.rolePermission;
            /*if (permission) {
                if (rolePermission) {
                    html += '<input type="checkbox" role="hasPrivilege" checked="checked" id="hasPrivilege_' + row.id + '" class="chooseBtn" ' +
                        'onchange="$m(\'dam/metamanage/privilege/datatype\').changePrivilege(\'' +  row.id + '\')">';
                } else {
                    // html += '<input type="checkbox" role="hasPrivilege" id="hasPrivilege_' + row.id + '" class="chooseBtn">';
                    html += '<input type="checkbox" role="hasPrivilege" id="hasPrivilege_' + row.id + '" class="chooseBtn" ' +
                        'onchange="$m(\'dam/metamanage/privilege/datatype\').changePrivilege(\'' + row.id + '\')">';
                }
            }*/
            if (permission) {
                if (rolePermission) {
                    html += '<input type="checkbox" role="hasPrivilege" checked="checked" id="hasPrivilege_' + row.id + '" class="chooseBtn" ' +
                        'onchange="$m(\'dam/metamanage/privilege/datatype\').changePrivilege(\''+row.id+'\')">';
                } else {
                    html += '<input type="checkbox" role="hasPrivilege" id="hasPrivilege_' + row.id + '" class="chooseBtn" ' +
                        'onchange="$m(\'dam/metamanage/privilege/datatype\').changePrivilege(\''+row.id+'\')">';
                }
            }
        }
        return html;
    }

    /**
     * 级联授权
     * @param id
     * @param index
     */
    /*this.changePrivilege = function (id) {
        var type = $('#hasPrivilege_' + id).is(':checked');
        if (type) {
            // 向下级联选中所有子分类
            var children = treeGrid.treegrid('getChildren', id);
            if (children != null) {
                for (var i = 0; i < children.length; i++) {
                    $('input[id="hasPrivilege_' + children[i].id + '"]').eq(0).prop('checked', true);
                }
            }
        } else {
            // 向上级联取消选中所有祖先节点
            var parent = treeGrid.treegrid('getParent', id);
            if (parent != null) {
                $('input[id="hasPrivilege_' + parent.id + '"]').eq(0).prop('checked', false);
            }
        }
    }*/

    /**
     *  级联授权
     * @param id
     * @param index
     */
    this.changePrivilege = function (id) {
        var roleId = tree.tree('getSelected').id;
        var node = tree.tree('getSelected');
        var dataTypeId = id;
        var type = $('#hasPrivilege_' + id).is(':checked');
        $.ajax({
            url: cascadeAssignPath + operateLogUrl,
            data: {'dataTypeId': dataTypeId, 'roleId': roleId, 'type': type},
            type: 'post',
            success: function (data) {
                if (data.success) {
                    selectTree(node);
                    /*treeGrid.treegrid('reload',roleId);*/
                } else {
                    $.Msg.alert('提示', '操作失败');
                }
            }
        });
    }
});
