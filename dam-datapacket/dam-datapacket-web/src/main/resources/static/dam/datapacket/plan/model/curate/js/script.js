$m('dam/plan/model/curate', function () {

    //=======获取当前页面菜单信息============
    var modelConfigDialog;
    var code;
    var nodeId;
    var modelConfigModulePath = '/dam-datapacket/api/datapacket/config/';
    var moduleGrid;
    var isPackage;

    $(function () {
        moduleGrid = $("#dam_datapacket_curate_grid");
    });

    $('#lookUpInfo').combo({
        required:true,
        multiple:true
    });

    this.changeType = function (param) {
        isPackage = param;
    };

    /**
     * 查询列表获取树节点id
     */
    this.getQueryParam = function () {
        return {nodeId: nodeId};
    };

    /**
     * 查询按钮点击事件
     */
    this.modelQuery = function () {
        var form = $('#module_modelList_queryform');
        var node = $("#dam_datapacket_model_tree").tree('getSelected');
        if (node == undefined) {
            form.find("#nodeId").val('-1');
        } else {
            form.find("#nodeId").val(node.id);
        }
        $('#dam_datapacket_curate_grid').datagrid("reload", form.form('getRecord'));
    };

    /**
     * 操作跳转
     * @param value
     * @param rowData
     * @param index
     * @returns {string}
     */
    this.actionFmt = function (value, rowData, index) {
        var button = '<a class="actionColumns l-btn l-btn-medium l-btn-normal" group=""data-roles="mui-linkbutton"title="配置" href="javascript:void(0)" data-options="theme:\'brand\'"\n' +
            "onclick=\"$m('dam/plan/model/curate').modelConfig('" + index + "');\">" +
            '        <span class="l-btn-left">\n' +
            '            <span class="l-btn-text">配置</span>\n' +
            '        </span>\n' +
            '    </a>';
        return button;
    };

    this.modelConfig = function (index) {
        var record = $('#dam_datapacket_curate_grid').datagrid('getRows')[index];
        var modelInfo=record.modelInfo;
        isPackage = record.isPackage;
        var href;
        var adContextPath = getAdContextPath("adContextPath");
        adContextPath = (adContextPath == null ? '' : adContextPath);
        href = adContextPath + modelConfigModulePath + 'view/index';
        //模板名称
        code = record.code;
        modelConfigDialog = $.topDialog({
            title: '配置',
            modal: true,
            width: 800,
            height: 500,
            href: href,
            onLoad: function () {
                modelConfigDialog.find("#codeVal").val(code);
                if (isPackage == '0') {
                    $("#dam_datapacket_curate_dialog_form_radio").attr("checked","checked")
                    $("#dam_datapacket_curate_dialog_form_radio1").removeAttr("checked")
                } else {
                    $("#dam_datapacket_curate_dialog_form_radio1").attr("checked","checked");
                    $("#dam_datapacket_curate_dialog_form_radio").removeAttr("checked");
                }
                modelConfigDialog.find('#dam_datapacket_curate_code_grid').datagrid({
                    url: '/dam-datapacket/api/datapacket/config/column/list?moduleCode=' + code+'&nodeId='+nodeId,
                    rownumbers: true,
                    onLoadSuccess:function () {
                        var record = $('#dam_datapacket_curate_code_grid').datagrid('getRows');
                        var find = modelConfigDialog.find('#dam_datapacket_curate_code_grid');
                        for (var i = 0; i < record.length; i++) {
                            //打开行编辑
                            find.datagrid('beginEdit',i);
                            var lookup =  find.datagrid('getEditor', {index: i, field: 'lookup'});
                            if (record[i].lookup) {
                                lookup.target.combobox('select',"");
                                lookup.target.combobox('loadData',"");
                                lookup.target.combobox('loadData',JSON.parse(record[i].lookup));
                            }
                        }
                    }
                });
            },
            buttons: [
                {
                    text: '取消',
                    handler: function () {
                        modelConfigDialog.dialog('close');
                    }
                },
                {
                    text: '确定',
                    id: 'addSaveBtn',
                    handler: function () {
                        var record = $('#dam_datapacket_curate_code_grid').datagrid('getRows');
                        var find = modelConfigDialog.find('#dam_datapacket_curate_code_grid');
                        for (var i = 0; i < record.length; i++) {
                            //关闭行编辑
                            find.datagrid('endEdit',i);
                        }

                        $.ajax({
                            url: "/dam-datapacket/api/datapacket/config/column/config/save",
                            method: 'post',
                            data: {'modelInfo':modelInfo,'isPackage':isPackage, 'treeNode': nodeId, 'code': code, 'columnConfigList': JSON.stringify(record)},
                            async: false,
                            success: function (data) {
                                if (data.status) {
                                    $.topDialog('close', modelConfigDialog);
                                    $('#dam_datapacket_curate_grid').datagrid('reload');
                                    successDialogMessage("保存成功")
                                }
                            }
                        })
                    }
                }],
        })
    };


    /**
     * 成功结果弹窗
     * @param message
     */
    var successDialogMessage = function (message) {
        top$.messager.promptInfo({
            msg: message,
            icon: 'success'
        });
    };


    /**
     * 双击模型树节点，触发事件
     * */
    this.dbClickTree = function dbClickTree(node) {
        var state = node.state;
        if (state == 'closed') {
            $('#dam_datapacket_model_tree').tree('expand', node.target);
        } else {
            $('#dam_datapacket_model_tree').tree('collapse', node.target);
        }
    };


    this.configSave = function (row) {
    }
    this.selectTree = function (node) {
        nodeId = node.id;
        moduleGrid.datagrid({
            url:'/dam-datapacket/api/datapacket/planning/page',
            queryParams: {'nodeId': nodeId, 'page': 1, 'rows': 10}
        });
    };

    /**
     * 查询页面查询树方法
     */
    this.searchDataTypeList = function (value) {
        debugger
    };

    this.tree = function (value) {
        debugger
    };



    /**
     * 所需字段初始化
     * @param value
     * @param row
     * @param index
     * @returns {string}
     */
    this.needColumnFormatter = function (value, row, index) {
        var html = '';
        if (row.needColumn == '1') {
            html += '<input type="checkbox" role="hasPrivilege" checked="checked" id="hasPrivilege_' + index + '"  ' +
                'onchange="$m(\'dam/plan/model/curate\').changeNeedColumn(\'' + row.code + '\', \'' + index + '\')">';
        } else {
            html += '<input type="checkbox" role="hasPrivilege" id="hasPrivilege_' + index + '"  ' +
                'onchange="$m(\'dam/plan/model/curate\').changeNeedColumn(\'' + row.code + '\', \'' + index + '\')">';
        }
        return html + '<label for="hasPrivilege_' + index + '"></label>';
    };

    /**
     * 查询条件初始化
     * @param value
     * @param row
     * @param index
     * @returns {string}
     */
    this.isSearchFormatter = function (value, row, index) {
        var html = '';
        if (row.isSearch == '1') {
            html += '<input type="checkbox" role="hasPrivilege" checked="checked" id="hasPrivilege_' + index + '"  ' +
                'onchange="$m(\'dam/plan/model/curate\').changeIsSearch(\'' + row.code + '\', \'' + index + '\')">';
        } else {
            html += '<input type="checkbox" role="hasPrivilege" id="hasPrivilege_' + index + '"  ' +
                'onchange="$m(\'dam/plan/model/curate\').changeIsSearch(\'' + row.code + '\', \'' + index + '\')">';
        }
        return html + '<label for="hasPrivilege_' + index + '"></label>';
    };

    this.changeNeedColumn = function (code, index) {
        debugger
        var record = $('#dam_datapacket_curate_code_grid').datagrid('getRows')[index];
        if (record.needColumn == '0') {
            record.needColumn = '1';
        } else {
            record.needColumn = '0';
        }
    }

    this.changeIsSearch = function (code,index) {
        var record = $('#dam_datapacket_curate_code_grid').datagrid('getRows')[index];
        if (record.isSearch == '0') {
            record.isSearch = '1';
        } else {
            record.isSearch = '0';
        }
    }
    this.lookUpOnchange = function (index,row) {
        debugger
        console.log(index,row)
        // var record = $('#dam_datapacket_curate_code_grid').datagrid('getRows')[index];
        // if (record.isSearch == '0') {
        //     record.isSearch = '1';
        // } else {
        //     record.isSearch = '0';
        // }
    }



    this.allNeedColumn = function () {
        debugger
        var record = $('#dam_datapacket_curate_code_grid').datagrid('getRows');
        for (var i = 0; i < record.length; i++) {
            if (record[i].isSearch == '0') {
                record[i].isSearch = '1';
                $('#needColumn').attr("checkbox",true);
            } else {
                record[i].isSearch = '0';
                $('#needColumn').attr("checkbox",false);
            }
        }

    };

    this.allSearch = function () {
        debugger
        var record = $('#dam_datapacket_curate_code_grid').datagrid('getRows');
    }


});
