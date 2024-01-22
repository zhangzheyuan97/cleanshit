$m('dam/metamanage/privilege/view',function () {
    //=======对象引用声明=======start=========
    var userQueryForm;
    var userGrid;
    var userTree;
    var dataQueryForm;
    var dataGrid;
    var dataTree;
    var nodeId;
    var viewDialog;
    var viewTab;

    //=======对象引用声明=======end=========

    //=======相关访问路径声明=======start=========

    // 公共角色树路径
    var modulePath = '/dam-metamanage/api/metamanage/viewprivilege/';
    //获取日志路径
    var operateLogUrl = '?module=' + encodeURIComponent(TempoUtils.getPathLists().module.name) + '&menu=' + encodeURIComponent(TempoUtils.getPathLists().menu.name);
    // 左侧模型树请求地址
    var dataLimitViewtreePath = '/dam-metamanage/api/metamanage/buttonprivilege/datatypeModelTree';
    var userLimitViewtreePath = '/dam-metamanage/api/metamanage/privilege/roleTree/roleTreeAll';
    var operateLogUrl = '?module=' + encodeURIComponent(TempoUtils.getPathLists().module.name) + '&menu=' + encodeURIComponent(TempoUtils.getPathLists().menu.name);
    // 根据模型获取用户权限列表请求路径
    this.dataLimitViewPagePath = modulePath + 'dataLimitViewPage' + operateLogUrl;
    // 根据角色或者用户信息获取数据模型权限列表的请求路径
    this.userLimitViewPagePath = modulePath + 'userLimitViewPage' + operateLogUrl;
    // 查看字段权限列表请求路径
    this.viewFieldsPath = modulePath + 'viewFieldsList' + operateLogUrl;
    //=======相关访问路径声明=======end=========

    /**
     * 初始化选择对象
     */
    $(function () {
        dataGrid = $('#dam_metamanage_datalimit_grid');
        dataQueryForm = $('#dam_metamanage_datalimit_queryform');
        dataTree = $("#dam_metamanage_datalimit_tree");

        userGrid = $('#dam_metamanage_userlimit_grid');
        userQueryForm = $('#dam_metamanage_userlimit_queryform');
        userTree = $("#dam_metamanage_userlimit_tree");
        nodeId = "";
        viewTab = $('#dam_metamanage_viewTab');
        dataTree.tree({url: dataLimitViewtreePath,loadFilter:$m('dam/metamanage/privilege/view').loadFilterTree});
        userTree.tree({url: userLimitViewtreePath});
        // $('#dam_metamanage_datalimit_tree').height($('#dam_metamanage_viewTab').height()-98);
        dataTree.css('cssText','overflow: auto; margin-top: 8px;height:775px !important');
        // 选项卡触发事件
        // viewTab.tabs({
        //     border:false,
        //     onSelect:function(title){
        //         if (title == "数据权限查询") {
        //             // 初始化分类模型树
        //             dataTree.tree({url: dataLimitViewtreePath});
        //         } else {
        //             userTree.tree({url: userLimitViewtreePath});
        //         }
        //     }
        // });
    });




    /**
     * 左侧模型树选择事件
     * */
    this.selectDataLimitViewTree = function(node) {
        var modelId;
        if(node.attributes != undefined){
            modelId = node.attributes.modelInfo;
        }
        if (modelId != null || modelId != undefined) {
            nodeId = node.id;
        }
        $m('dam/metamanage/privilege/view').DataReset();
        $m('dam/metamanage/privilege/view').DataQuery();
    }
    /**
     * 左侧角色树选择事件
     * */
    this.selectUserLimitViewTree = function(node) {
        var roleId;
        if(node.attributes != undefined){
            roleId = node.attributes.roleId;
        }
        if (roleId != null || roleId != undefined) {
            nodeId = node.id;
        }
        $m('dam/metamanage/privilege/view').UserReset();
        $m('dam/metamanage/privilege/view').UserQuery();
    }


    /**
     * 查询区重置按钮事件
     */
    this.UserReset = function() {
        userQueryForm.form('reset');
    }
    /**
     * 查询按钮事件
     */
    this.UserQuery = function() {
        if (userQueryForm.form('validate')) {
            var queryParams = userQueryForm.form('getRecord');
            queryParams["treeId"] = userTree.tree('getSelected').id;
            userGrid.datagrid('load', queryParams);
        }
    }

    /**
     * 查询区重置按钮事件
     */
    this.DataReset = function() {
        dataQueryForm.form('reset');
    }

    /**
     * 查询按钮事件
     */
    this.DataQuery = function() {
        if (dataQueryForm.form('validate')) {
            var queryParams = dataQueryForm.form('getRecord');
            queryParams["treeId"] = dataTree.tree('getSelected').id;;
            dataGrid.datagrid('load', queryParams);
        }
    }

    /**
     * 查看字段权限查询按钮事件
     */
    this.fieldsDataQuery = function() {
        var modelId = viewDialog.find('#modelId').val();
        var datagrid = viewDialog.find("#dam_metamanage_limitview_field_grid_" + modelId);
        var queryParams = viewDialog.find("#dam_metamanage_limitview_field_grid_form_" + modelId).form('getRecord');
        var dataQueryForm = viewDialog.find('#dam_metamanage_fieldsdatalimit_queryform');
        if (dataQueryForm.form('validate')) {
            var name = dataQueryForm.find(':input[name="name"]').val();
            queryParams['name'] = name;
            datagrid.datagrid('load', queryParams);
        }
    }
    /**
     * 根据窗口校验的方法
     */
    var validate = function(dialog) {
        return true;
    }

    /**
     * 限制周期显示内容格式化
     * */
    this.limitPeriodFmt = function(value, row, index) {
        if ("DAY" == value) {
            value = "天";
        } else if ("WEEK" == value) {
            value = "周";
        } else if ("MONTH" == value) {
            value = "月";
        } else if ("YEAR" == value) {
            value = "年";
        }
        return value;
    }

    /**
     * 字段权限显示内容格式化
     * */
    this.authFormatter = function(value, row, index) {
        // 'P' == row.pob分区字段强制列表显示和查询条件
        if (value == '1' || 'P' == row.pob) {
            return '<input type="checkbox" checked="checked" disabled="true" />';
        } else {
            return '<input type="checkbox" disabled="true">';
        }
    }

    /**
     * 初始化表头过滤
     */
    this.initColumnFilter = function(data) {
        var tabs = viewDialog.find("#modelSubTabs").tabs('tabs');
        for (var i = 0; i < tabs.length; i++) {
            var tab = tabs[i];
            var tabObj = $(tab);
            var modelId = tabObj.find('#modelId').val();
            var $table = viewDialog.find("#dam_metamanage_limitview_field_grid_" + modelId);
            var $view1 = $table.siblings("div.datagrid-view1");
            var $view2 = $table.siblings("div.datagrid-view2").find(".datagrid-htable");
            var filterRowObj = $view2.find(".datagrid-filter-row");
            if ($view2.find(".datagrid-filter-row").length == 0) {
                $view1.find(".datagrid-htable").eq(0).find('.datagrid-header-row').find('td').attr('rowspan', 2);
                var fieldArray = [{
                    "field" : "busiName",
                    "type" : "text"
                }, {
                    "field" : "fieldName",
                    "type" : "text"
                }];

                var filterHtml = appendFilterHtml(fieldArray);
                $view2.eq(0).append(filterHtml);
                var setWidth = 210;
                $view2.find(".datagrid-filter-row").find(".datagrid-selffilter-text").css({"width" : setWidth, "border" : "#b9d6e4 1px solid"});
                $view2.find(".datagrid-filter-row").find(".datagrid-selffilter-combo").css({
                    "width" : setWidth,
                    "height" : "22px",
                    "border" : "#b9d6e4 1px solid"
                });
                $view2.find(".datagrid-filter-row").find("span").css("background-size", "contain");
                $view2.find(".datagrid-filter-row").find(".datagrid-filter-cell").find("input").on('keyup', function() {
                    evenFun(null);
                });
            }
        }
    }

    var evenFun = function(tabObj) {
        if (tabObj == null) {
            tabObj = $(viewDialog.find("#modelSubTabs").tabs('getSelected'));
        }
        var modelId = tabObj.find('#modelId').val();
        var $table = viewDialog.find("#dam_metamanage_limitview_field_grid_" + modelId);
        var $view1 = $table.siblings("div.datagrid-view1");
        var $view2 = $table.siblings("div.datagrid-view2").find(".datagrid-htable");
        var tds = $view2.find(".datagrid-filter-row").find("td");
        var queryParams = tabObj.find("#dam_metamanage_limitview_field_grid_form_" + modelId).form('getRecord');
        $.each(tds, function(i, td) {
            var fieldName = $(td).attr("field");
            queryParams[fieldName] = $(td).find("[name='" + fieldName + "']").val();
        });
        $table.datagrid("load", queryParams);
    }

    this.showButton = function (value, row, index) {
        var html;
        var dbType = row.dbType;
        if ('hbase' == dbType) {
            // html = '<a id="btn" href="javascript:void(0)" disabled="disabled" style="color: #969696" ' +
            //     '>查看字段权限</a>'
        } else {
            html = '<a id="btn"  class="actionColumns l-btn l-btn-medium l-btn-normal" group="" href="javascript:void(0)" data-options="theme:\'brand\'"\n' +
                'onclick="$m(\'dam/metamanage/privilege/view\').viewFields(\''+ row.modelId +'\', \'' + row.roleId +'\',' +
                ' \'' + row.limitTypeKey +'\',  \'' + row.userTypeKey +'\')">' +
                '        <span class="l-btn-left">\n' +
                '            <span class="l-btn-text">查看字段权限</span>\n' +
                '        </span>\n' +
                '    </a>';
        }
        return html;
    }


    /**
     * 查看字段权限按钮事件
     * */
    this.viewFields = function(modelId,roleId,limitType,userType) {
        var hrefPost = "&roleId=" + roleId  + "&limitType=" + limitType +"&userType=" + userType;
        viewDialog = $.topDialog({
            title:'查看字段权限',
            width:1000,
            height:600,
            modal:true,
            href: modulePath + 'view/viewIndex?viewIndex=true' + hrefPost + '&modelId=' + modelId,
            onLoad:function(){
                // 获取页面需要信息
                $.ajax({
                    url : modulePath + 'preView',
                    data : {
                        'viewIndex' : 'true',
                        'modelId' : modelId
                    },
                    method : 'post',
                    dataType : 'json',
                    success : function(data) {
                        var modelList = data.data;
                        for (var i = 0; i < modelList.length; i++) {
                            var modelInfo = modelList[i];
                            var id = modelInfo.modelInfo;
                            var title = modelInfo.name;
                            if (i == 0) {
                                viewDialog.find('#modelSubTabs').tabs('add',{
                                    id : id,
                                    title : title,
                                    href : modulePath + 'view/viewFields?viewFields=true' + hrefPost + "&modelId=" + id,
                                    selected:true
                                });
                            } else {
                                viewDialog.find('#modelSubTabs').tabs('add',{
                                    id : id,
                                    title : title,
                                    href : modulePath + 'view/viewFields?viewFields=true' + hrefPost + "&modelId=" + id,
                                    selected:true,
                                    onLoad:function(){
                                        viewDialog.find('#modelSubTabs').tabs('select', 0);
                                    }
                                });
                            }
                        }
                    }
                });
            },
            buttons:[{
                text:'关闭',
                handler:function(){
                    $.topDialog('close',viewDialog);
                }
            }]
        });
    }

    /**
     * 角色树名称模糊匹配
     * @param value
     */
    this.selectRoleSearch = function (value) {
        if(value == null || value == ''){
            return;
        }
        /*var children = userTree.tree('getChildren', userTree.tree('getRoot').target);
        var pareTotal = 0;
        var pareMap = {};
        var countMap = {};
        // 获取查询结果
        if(children != null && children.length > 0) {
            for (var i = 0; i < children.length; i++) {
                var child = children[i];
                var roleId = child.id;
                var roleName = child.text;
                // 匹配模型信息
                if(roleName.indexOf(value) != -1) {
                    pareTotal = pareTotal + 1;
                    pareMap[roleId] = pareTotal;
                    countMap[pareTotal] = roleId;
                }
            }
        }
        // 循环选中查询结果
        if(pareTotal > 0) {
            var selectedNode = userTree.tree("getSelected");
            var nextId = "";
            if(selectedNode == null) {
                nextId = countMap[1];
            } else {
                var selectedIndex = pareMap[selectedNode.id];
                if(selectedIndex == 'undefined' || selectedIndex == null || selectedIndex == '') {
                    nextId = countMap[1];
                } else {
                    if(selectedIndex < pareTotal) {
                        nextId = countMap[selectedIndex + 1];
                    } else {
                        nextId = countMap[1];
                    }
                }
            }
            // 获取节点，展开树结构，选中查询结果节点
            var nextNode = userTree.tree("find", nextId);
            userTree.tree("expandTo", nextNode.target);
            userTree.tree("select", nextNode.target);
            userTree.scrollTop($(nextNode.target).offset().top - userTree.offset().top);
            userGrid.datagrid('load', {roleId: nextId});
        }*/
        var selectedNode = userTree.tree("getSelected");
        $m('dam/metamanage/privilege/view').selectUserLimitViewTree(selectedNode);
    }

    /**
     * 模型树名称模糊匹配
     * @param value
     */
    this.selectModelSearch = function (value) {
        if(value == null || value == ''){
            return;
        }
        var selectedNode = dataTree.tree("getSelected");
        $m('dam/metamanage/privilege/view').selectDataLimitViewTree(selectedNode);
    }
    /**
     * 拼接过滤输入框HTML字符串
     * */
    var appendFilterHtml = this.appendFilterHtml = function (fieldArray) {
        var filterHtml = "";
        var trHtml = '<tr class="datagrid-header-row datagrid-filter-row">';
        var endTrHtml = '</tr>';
        var divHtml = '<div class="datagrid-cell" style="width: auto;">';
        var endDivHtml = '</div>';
        var preTdHtml = '<td class="datagrid-filter-cell" field="';
        var quotHtml = '"';
        var endItemHtml = '>';
        var endTdHtml = '</td>';
        var endOptionHtml = '</option>';
        filterHtml += trHtml;
        for (var i = 0; i < fieldArray.length; i++) {
            var item = fieldArray[i];
            var type = item.type;
            var field = item.field;
            filterHtml += preTdHtml;
            filterHtml += field;
            filterHtml += quotHtml;
            filterHtml += endItemHtml;
            filterHtml += divHtml;
            if ("text" == type) {
                var preInputHtml = '<span style="padding: 0 10px;" class="l-btn-empty icon-preview"></span><input class="datagrid-selffilter-text" name="';
                var textRolesHtml = ' data-roles="mui-validatebox" class="validatebox-text"';
                filterHtml += preInputHtml;
                filterHtml += field;
                filterHtml += quotHtml;
                filterHtml += textRolesHtml;
                filterHtml += endItemHtml;
            } else if ("combo" == type) {
                var data = item.data;
                var preSelectHtml = '<span style="padding: 0 10px;" class="l-btn-empty icon-preview"></span><select class="datagrid-selffilter-combo" name="';
                var preOptionHtml = '<option value="';
                filterHtml += preSelectHtml;
                filterHtml += field;
                filterHtml += quotHtml;
                filterHtml += endItemHtml;
                filterHtml += preOptionHtml;
                filterHtml += quotHtml;
                filterHtml += ' selected="selected"';
                filterHtml += endItemHtml;
                filterHtml += '';
                filterHtml += endOptionHtml;
                if (data != 'undefined' && data != null && data.length > 0) {
                    for (var j = 0; j < data.length; j++) {
                        var option = data[j];
                        var value = option.value;
                        var text = option.text;
                        filterHtml += preOptionHtml;
                        filterHtml += value;
                        filterHtml += quotHtml;
                        filterHtml += endItemHtml;
                        filterHtml += text;
                    }
                    filterHtml += endOptionHtml;
                }
                filterHtml += '</select>';
            }
            filterHtml += endDivHtml;
            filterHtml += endTdHtml;
        }
        filterHtml += '</tr>';
        return filterHtml;
    };

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
