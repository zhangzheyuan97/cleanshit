$m('dam/plan/acquistion/dataAcquistion', function () {

    //=======获取当前页面菜单信息============

    var lityMatterBasePath = '/dam-datapacket/api/datapacket/gather/';
    var modelName;
    var lity;
    var lityAddDialog;
    //当前节点
    var currentNode;
    var moduleGrid;
    var lityGrid;
    var attributes;
    //单机名称
    var currentName;

    //是否管理到实物
    var isManageObject = 1;

    $(function () {
        moduleGrid = $("#dam_datapacket_acquistion_dataAcquistion_grid");
        lityGrid = $("#dam_exhibition_version_lity_grid");
    });

    this.changeManageObject = function (type) {
        isManageObject = type;
    }

    /**
     * 模型树加载成功事件
     * @param node
     * @param data
     */
    this.loadSuccess = function (node, data) {
        // 选中第一个根节点
        if (!node && data && data.length > 0) {
            var roots = $(this).tree('getRoots');
            $(this).tree('select', roots[0].target);
        }
    };

    this.selectTree = function (node) {
        currentNode = node;
        attributes = node.attributes;
        if(!node.attributes.nodeType || node.attributes.nodeType=="1" || node.attributes.nodeType=="0"){
            $('#dj').hide();
            $('#fxt').show();
            modelName = node.text;
            moduleGrid.datagrid({
                url:lityMatterBasePath + 'page',
                queryParams: {'modelName': modelName,'lity':lity,'attributes':JSON.stringify(attributes),'page': 1, 'rows': 10}
            });
        } else {
            $('#fxt').hide();
            $('#dj').show();
            var id=node.id;
            var text=node.text;
            var classIfication= $('#classIficationInfo').val();
            var drawingNo=$('#drawingNoInfo').val();
            var name=$('#nameInfo').val();
            var batchNo=$('#batchNoInfo').val();
            var physicalNo=$('#physicalNoInfo').val();
            var attributes=node.attributes;
            var exhibitionGrid= $('#dam_exhibition_version_lity_grid');
            nodeId = node.id;
            var exhibitionListURL='/dam-datapacket/api/datapacket/show/exhibitionList'
            exhibitionGrid.datagrid({
                url:exhibitionListURL,
                queryParams: {'id': id,"classIfication":classIfication,
                    "drawingNo":drawingNo,"name":name,"batchNo":batchNo,"physicalNo":physicalNo,"attributes":JSON.stringify(attributes),"text":text},
                onLoadSuccess:function () {
                    currentName = $('#dam_exhibition_version_lity_grid').datagrid('getRows')[0].name;
                }
            });
        }
    };

    /**
     * 查询列表获取树节点id
     */
    this.getQueryParam = function () {
        return {modelName: modelName};
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
            "onclick=\"$m('dam/plan/acquistion/dataAcquistion').modelConfig('" + index + "');\">" +
            '        <span class="l-btn-left">\n' +
            '            <span class="l-btn-text">配置</span>\n' +
            '        </span>\n' +
            '    </a>';
        return button;
    };

    this.modelConfig = function (index) {
        debugger
        var record = $('#dam_datapacket_acquistion_dataAcquistion_grid').datagrid('getRows')[index];
        // isPackage = record.isPackage;
        var href;
        var adContextPath = getAdContextPath("adContextPath");
        adContextPath = (adContextPath == null ? '' : adContextPath);
        href = adContextPath + lityMatterBasePath + 'view/config';
        debugger
        // //模板名称
        // code = record.code;
        modelConfigDialog = $.topDialog({
            title: '配置',
            modal: true,
            width: 800,
            height: 500,
            href: href,
            // onLoad: function () {
            //     modelConfigDialog.find("#codeVal").val(code);
            //     if (isPackage == '0') {
            //         $("#dam_datapacket_curate_dialog_form_radio").attr("checked","checked")
            //         $("#dam_datapacket_curate_dialog_form_radio1").removeAttr("checked")
            //     } else {
            //         $("#dam_datapacket_curate_dialog_form_radio1").attr("checked","checked");
            //         $("#dam_datapacket_curate_dialog_form_radio").removeAttr("checked");
            //     }
            //     modelConfigDialog.find('#dam_datapacket_curate_code_grid').datagrid({
            //         url: '/dam-datapacket/api/datapacket/config/column/list?moduleCode=' + code+'&nodeId='+nodeId,
            //         rownumbers: true,
            //         onLoadSuccess:function () {
            //             var record = $('#dam_datapacket_curate_code_grid').datagrid('getRows');
            //             var find = modelConfigDialog.find('#dam_datapacket_curate_code_grid');
            //             for (var i = 0; i < record.length; i++) {
            //                 //打开行编辑
            //                 find.datagrid('beginEdit',i);
            //                 var lookup =  find.datagrid('getEditor', {index: i, field: 'lookup'});
            //                 if (record[i].lookup) {
            //                     lookup.target.combobox('select',"");
            //                     lookup.target.combobox('loadData',"");
            //                     lookup.target.combobox('loadData',JSON.parse(record[i].lookup));
            //                 }
            //             }
            //         }
            //     });
            // },
            buttons: [
                {
                    text: '取消',
                    handler: function () {
                        modelConfigDialog.dialog('close');
                    }
                },
                {
                    text: '确定',
                    id: 'addSaveBtn'
                    // handler: function () {
                    //     var record = $('#dam_datapacket_curate_code_grid').datagrid('getRows');
                    //     var find = modelConfigDialog.find('#dam_datapacket_curate_code_grid');
                    //     for (var i = 0; i < record.length; i++) {
                    //         //关闭行编辑
                    //         find.datagrid('endEdit',i);
                    //     }
                    //
                    //     $.ajax({
                    //         url: "/dam-datapacket/api/datapacket/config/column/config/save",
                    //         method: 'post',
                    //         data: {'isPackage':isPackage, 'treeNode': nodeId, 'code': code, 'columnConfigList': JSON.stringify(record)},
                    //         async: false,
                    //         success: function (data) {
                    //             if (data.status) {
                    //                 $.topDialog('close', modelConfigDialog);
                    //                 $('#dam_datapacket_curate_grid').datagrid('reload');
                    //                 successDialogMessage("保存成功")
                    //             }
                    //         }
                    //     })
                    // }
                }],
        })
    };

    /**
     * 查询按钮点击事件
     */
    this.modelQuery = function () {
        if(!currentNode.attributes.nodeType || currentNode.attributes.nodeType=="1" || currentNode.attributes.nodeType=="0"){
            var form = $('#module_modelList_dataAcquistion_queryform');
            var node = $("#dam_datapacket_acquistion_dataAcquistion_tree").tree('getSelected');
            if (node == undefined) {
                form.find("#modelName").val('-1');
            } else {
                form.find("#modelName").val(node.text);
                form.find("#attributes").val(JSON.stringify(node.attributes));
            }
            $('#dam_datapacket_acquistion_dataAcquistion_grid').datagrid("reload", form.form('getRecord'));
        } else {
            var node = $("#dam_datapacket_acquistion_dataAcquistion_tree").tree('getSelected');
            var id=node.id;
            var text=node.text;
            // var busSystem=node.attributes.material_object;
            var classIfication= $('#classIficationInfo').val();
            var drawingNo=$('#drawingNoInfo').val();
            var name=$('#nameInfo').val();
            var batchNo=$('#batchNoInfo').val();
            var physicalNo=$('#physicalNoInfo').val();
            var attributes=node.attributes;
            var exhibitionGrid= $('#dam_exhibition_version_lity_grid');
            nodeId = node.id;
            var exhibitionListURL='/dam-datapacket/api/datapacket/show/exhibitionList'
            exhibitionGrid.datagrid({
                url:exhibitionListURL,
                queryParams: {'id': id,"classIfication":classIfication,
                    "drawingNo":drawingNo,"name":name,"batchNo":batchNo,"physicalNo":physicalNo,"attributes":JSON.stringify(attributes),"text":text}
            });
        }

    };

    /**
     * 重置按钮点击事件
     */
    this.reset = function () {
        if(!currentNode.attributes.nodeType || currentNode.attributes.nodeType=="1" || currentNode.attributes.nodeType=="0"){
            var form = $('#module_modelList_dataAcquistion_queryform');
            var node = $("#dam_datapacket_acquistion_dataAcquistion_tree").tree('getSelected');
            if (node == undefined) {
                form.find("#modelName").val('-1');
            } else {
                form.find("#modelName").val(node.text);
                form.find("#type").val(node.attributes);
                $("#lity").val("");

            }
            $('#dam_datapacket_acquistion_dataAcquistion_grid').datagrid("reload", form.form('getRecord'));
        } else {
            $('#classIficationInfo').val('');
            $('#drawingNoInfo').val('');
            $('#nameInfo').val('');
            $('#batchNoInfo').val('');
            $('#physicalNoInfo').val('');
            this.modelQuery();
        }

    };



    /**
     * 双击模型树节点，触发事件
     * */
    this.dbClickTree = function dbClickTree(node) {
        var state = node.state;
        if (state == 'closed') {
            $('#dam_datapacket_acquistion_dataAcquistion_tree').tree('expand', node.target);
        } else {
            $('#dam_datapacket_acquistion_dataAcquistion_tree').tree('collapse', node.target);
        }
    };

    this.modelAdd = function () {
        var href;
        var attr = attributes;
        var adContextPath = getAdContextPath("adContextPath");
        adContextPath = (adContextPath == null ? '' : adContextPath);
        if (!currentNode.attributes.nodeType || currentNode.attributes.nodeType=="1" || currentNode.attributes.nodeType=="0") {
            href = adContextPath + lityMatterBasePath + 'view/add';
            lityAddDialog = $.topDialog({
                title: '新增',
                modal: true,
                width: 500,
                height: 260,
                href: href,
                onLoad: function() {
                    $("#model").val(modelName);
                },
                buttons: [
                    {id: "litySaveCancle", text: "取消", handler: $m('dam/plan/acquistion/dataAcquistion').lityAddCancle},
                    {id: "litySave", text: "保存", handler: $m('dam/plan/acquistion/dataAcquistion').lityAddSave},
                ]
            });
        } else {
            console.log(currentNode)
            href = adContextPath + lityMatterBasePath + 'view/addDj';
            lityAddDialog = $.topDialog({
                title: '新增',
                modal: true,
                width: 500,
                height: 480,
                href: href,
                onLoad: function() {
                    debugger
                    if (currentNode.attributes.nodeType == "3") {
                        //类别
                        $("#category").val(currentNode.attributes.secondNode);
                        $('#manageMatter').hide();
                    } else {
                        $('#manageMatter').show();
                    }
                    //图号
                    $("#figure").val(currentNode.attributes.thirdlyNode);
                    //名称
                    $("#text").val(currentName);
                    //批次号
                    $("#lityNo").val(currentNode.text);
                },
                buttons: [
                    {id: "litySaveCancle", text: "取消", handler: $m('dam/plan/acquistion/dataAcquistion').lityAddCancle},
                    {id: "litySave", text: "保存", handler: $m('dam/plan/acquistion/dataAcquistion').addPhysical},
                ]
            });
        }

    }

    /**
     * 删除模型
     */
    this.modelDelete = function () {
        if (currentNode.attributes.nodeType == "0" || currentNode.attributes.nodeType == "1") {
            //模块或者分系统
            if (moduleGrid) {
                var selections = moduleGrid.datagrid("getChecked");
                if (!selections || selections.length < 1) {
                    top$.messager.promptInfo({msg: '请选择一条数据', icon: 'warning'});
                    return;
                }
                var data = new Array();
                for (var i = 0; i < selections.length; i++) {
                    var info = {};
                    info["F_M_SYS_ID"] = selections[i].id;
                    info["S_M_SYS_DATAID"] = selections[i].id;
                    info["S_M_SYS_VERSIONSTATUS"] = 1;
                    data.push(info);
                }
                $.Msg.confirm('提示', '确认要删除吗?', function (r) {
                    if (r) {
                        openProgresss();
                        debugger
                        console.log(data)
                        $.ajax({
                            url: lityMatterBasePath + "deleteModelData",
                            method: "POST",
                            data: {'selectGridData':JSON.stringify(data),'tableName':"TIMES_MATERIAL_OBJECT"},
                            // contentType:"application/json",
                            // dataType: "JSON",
                            success: function (result) {
                                closeProgresss();
                                if (result && result.status != 1) {
                                    top$.messager.promptInfo({
                                        msg: '删除成功!',
                                        icon: 'success',
                                    });
                                } else {
                                    $.Msg.alert('提示', "删除模型失败");
                                }
                                reloadGrid();
                            },
                            error: function () {
                                closeProgresss();
                                $.Msg.alert('提示', "删除模型失败");
                            }
                        });
                    }
                });
            }
        } else {
            //单机或者总装直属件
        }
    }
    // 刷新
    function reloadGrid() {
        $('#dam_metamanage_modelVersion_grid').datagrid("reload");
    }

    //打开进度条
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

    //关闭进度条
    var closeProgresss = function (fieldVerGrid) {
        try {
            top$.messager.progress('close');
        } catch (e) {
            $.messager.progress('close');
        }
    };


    this.lityAddCancle = function () {
        if (lityAddDialog) {
            lityAddDialog.dialog('close');
        }
    };

    this.lityAddSave = function () {
        if (lityAddDialog) {
            var isValid = lityAddDialog.find("#lityAddForm").form('validate');
            if (!isValid) {
                return;
            }
            var attr = currentNode.attributes;
            var lity = lityAddDialog.find("#lityAddForm").serialize().split("=")[1];
            lityAddDialog.parent().find('#litySave').linkbutton('disable');
            $.ajax({
                method: "POST",
                url: lityMatterBasePath + "addLity",
                data: {'model':modelName, 'lity': lity, 'attributes': JSON.stringify(attr)},
                async: false,
                success: function (result) {
                    if (result.success) {
                        lityAddDialog.dialog('close');
                        successDialogMessage("保存成功");
                        setTimeout(function () {
                            $m('dam/plan/acquistion/dataAcquistion').selectTree(currentNode);
                        },500)

                    } else {
                        $.Msg.alert('提示', result.message);
                    }
                },
                error: function (result) {
                    $.Msg.alert('提示', '网络异常');
                }
            })
        }
    };

    this.addPhysical = function () {
        if (lityAddDialog) {
            var isValid = lityAddDialog.find("#lityMatterAddForm").form('validate');
            if (!isValid) {
                return;
            }
            var attr = currentNode.attributes;
            debugger
            //分类
            var classIfication = "";
            //图号
            var drawingNo = ""
            if (attr.nodeType=='3') {
                classIfication = attr.secondNode
                drawingNo = attr.thirdlyNode;
            } else {
                drawingNo = attr.secondNode;
            }

            var batchNo = $("#lityNo").val();
            var physicalNo = $("#matterNo").val();
            lityAddDialog.parent().find('#litySave').linkbutton('disable');
            $.ajax({
                method: "POST",
                url: lityMatterBasePath + "addPhysical",
                data: {'isManageObject':isManageObject,'classIfication':classIfication, 'drawingNo': drawingNo, 'name': currentName, 'batchNo':batchNo,'physicalNo':physicalNo,'attributes': JSON.stringify(attr)},
                async: false,
                success: function (result) {
                    if (result.success) {
                        lityAddDialog.dialog('close');
                        successDialogMessage("保存成功");
                        setTimeout(function () {
                            $m('dam/plan/acquistion/dataAcquistion').selectTree(currentNode);
                        },500)

                    } else {
                        $.Msg.alert('提示', result.message);
                    }
                },
                error: function (result) {
                    $.Msg.alert('提示', '网络异常');
                }
            })
        }
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
});
