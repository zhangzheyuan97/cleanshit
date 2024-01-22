$m('dam/metamanage/standard/dataelement/edit', function () {
    var addDataView = moduleResource.element.getView('add');
    var editDataView = moduleResource.element.getView('edit');
    var btnReElementView = moduleResource.element.getView('btnReEdit');
    var addElementView = moduleResource.element.getView('view');
    var elementScriptUrl = 'dam/metamanage/standard/dataelement';
    var addDialog;
    var editDialog;
    var btnReeditDialog;
    var addElementViewDialog;
    var dialog;
    $(function () {

    });


    //新增
    this.addElement = function () {
        addDialog = $.topDialog({
            title: '新增',
            href: addDataView,
            width: 800,
            height: 600,
            onLoad: function () {
                addDialog.find('#textLength').validatebox({required: true});
                Api.getTypes('', function (result) {
                    if (result) {
                        addDialog.find('#elementTypeId').combobox('loadData', result);
                        var typeSelected = $('#dam_matemanage_data_standard_type_tree').tree('getSelected');
                        if (typeSelected && !StandardUtils.equals('-1', typeSelected.id)) {
                            addDialog.find('#elementTypeId').combobox('setValue', typeSelected.id);
                        }
                    }
                });
                dialog = addDialog;
            },
            buttons: [
                {
                    text: '取消',
                    handler: function () {
                        $.topDialog('close', addDialog);
                    }
                },
                {
                    text: '确定',
                    id: 'addSave',
                    handler: addSave
                }
            ]
        });
    };

    function addSave() {
        if (addDialog) {
            var isValid = addDialog.find('#add_modelAddForm').form('validate');
            if (!isValid) {
                return;
            }
            var obj = {};
            obj.code = addDialog.find('#code').val();
            obj.elementTypeId = addDialog.find('#elementTypeId').combobox('getValue');
            obj.buisName = addDialog.find('#buisName').val();
            obj.publishName = addDialog.find('#publishName').val();
            obj.buisRemark = addDialog.find('#buisRemark').val();
            obj.dataType = addDialog.find('input[name="dataType"]:checked').val() || '';
            obj.dataLength = addDialog.find('#textLength').val() || addDialog.find('#dataLength').val();
            obj.dataFormat = addDialog.find('#dataFormat').combobox('getValue') || '';
            obj.dataPrecision = addDialog.find('#dataPrecision').val();
            obj.configEnum = addDialog.find('input[name="configEnum"]:checked').val();
            obj.configEnumId = addDialog.find('#enumId').combobox('getValue') || '';
            var confEmptyVal = addDialog.find('input[name="configEmpty"]:checked').val();
            var confUniqueVal = addDialog.find('input[name="configUnique"]:checked').val();
            obj.configEmpty = StandardUtils.equals(confEmptyVal, "true") ? 1 : 0;
            obj.configUnique = StandardUtils.equals(confUniqueVal, "true") ? 1 : 0;
            obj.configRemark = addDialog.find('#configRemark').val();
            Api.saveElements(obj, function (result) {
                if (result) {
                    $.topDialog('close', addDialog);
                    top$.messager.promptInfo({
                        msg: '添加成功',
                        icon: 'success',
                    });
                    $m(elementScriptUrl).reloadGrid();
                }
            });
        }
    }

    this.editElement = function (versionId) {
        editDialog = $.topDialog({
            title: '编辑',
            href: editDataView,
            width: 800,
            height: 600,
            onLoad: function () {
                editDialog.find('#textLength').validatebox({required: true});
                Api.getVersionDetail(versionId, function (result) {
                    if (result) {
                        result = dealResultData(result);
                        Api.getTypes('', function (result) {
                            if (result) {
                                editDialog.find('#elementTypeId').combobox('loadData', result);
                            }
                        });
                        isShowData(result.dataType);
                        if (result.configEnum) {
                            isShowElement(1, result.configEnumId)
                        } else {
                            isShowElement(2)
                        }
                        if (result.dataType === "STRING") {
                            result.textLength = result.dataLength;
                            result.dataLength = null;
                        }
                        editDialog.find("form").form("load", result);
                    }
                });
                dialog = editDialog;
            },
            buttons: [
                {
                    text: '取消',
                    handler: function () {
                        $.topDialog('close', editDialog);
                    }
                },
                {
                    text: '确定',
                    id: 'addSave',
                    handler: function () {
                        if (editDialog) {
                            var isValid = editDialog.find('form').form('validate');
                            if (!isValid) {
                                return;
                            }
                            var obj = {}
                            obj.elementId = editDialog.find('#elementId').val();
                            obj.deVersionId = versionId;
                            obj.code = editDialog.find('#code').val()
                            obj.elementTypeId = editDialog.find('#elementTypeId').combobox('getValue');
                            obj.buisName = editDialog.find('#buisName').val();
                            obj.publishName = editDialog.find('#publishName').val();
                            obj.buisRemark = editDialog.find('#buisRemark').val();
                            obj.dataType = editDialog.find('input[name="dataType"]:checked').val() || '';
                            obj.dataLength = editDialog.find('#textLength').val() || editDialog.find('#dataLength').val();
                            obj.dataFormat = editDialog.find('#dataFormat').combobox('getValue') || '';
                            obj.dataPrecision = editDialog.find('#dataPrecision').val();
                            obj.configEnum = editDialog.find('input[name="configEnum"]:checked').val();
                            obj.configEnumId = editDialog.find('#enumId').combobox('getValue') || '';
                            var confEmptyVal = editDialog.find('input[name="configEmpty"]:checked').val();
                            var confUniqueVal = editDialog.find('input[name="configUnique"]:checked').val();
                            obj.configEmpty = StandardUtils.equals(confEmptyVal, "true") ? 1 : 0;
                            obj.configUnique = StandardUtils.equals(confUniqueVal, "true") ? 1 : 0;
                            obj.configRemark = editDialog.find('#configRemark').val();
                            Api.saveElements(obj, function (result) {
                                if (result) {
                                    $.topDialog('close', editDialog);
                                    top$.messager.promptInfo({
                                        msg: '修改成功',
                                        icon: 'success',
                                    });
                                    $m(elementScriptUrl).reloadGrid();
                                }
                            });
                        }
                    }
                }
            ]
        });
    };
    /**
     * 是否匹配字段事件
     * */
    var isShowData = this.isShowData = function (value) {
        switch (value) {
            case 'STRING' :
                dialog.find('#textLengths').show();
                dialog.find('#textLength').validatebox({required: true});
                dialog.find('#dataLength').validatebox({required: false});
                dialog.find('#dataFromats').hide();
                dialog.find('#dataLengths').hide();
                dialog.find('#dataPrecisions').hide();
                break;
            case 'DATE' :
                dialog.find('#dataFromats').show();
                dialog.find('#textLength').validatebox({required: false});
                dialog.find('#dataLength').validatebox({required: false});
                dialog.find('#textLengths').hide();
                dialog.find('#dataLengths').hide();
                dialog.find('#dataPrecisions').hide();
                break;
            case 'NUMBER' :
                dialog.find('#dataLengths').show();
                dialog.find('#textLengths').hide();
                dialog.find('#textLength').validatebox({required: false});
                dialog.find('#dataFromats').hide();
                dialog.find('#dataPrecisions').show();
                dialog.find('#dataLength').validatebox({required: false});
                break;
        }
    };
    /**
     * 关联枚举标准
     * */
    var isShowElement = this.isShowElement = function (value, configEnumId) {
        switch (value) {
            case 1 :
                dialog.find('#configEnumIds').show();
                Api.getEnumTypeList('', function (result) {
                    if (result) {
                        dialog.find('#enumId').combobox('loadData', result);
                        if (StandardUtils.isNotEmpty(configEnumId)) {
                            dialog.find('#enumId').combobox('setValue', configEnumId);
                            dialog.find('#enumId').combobox({required: true});
                        }
                    }
                });
                break;
            case 2 :
                dialog.find('#configEnumIds').hide();
                dialog.find('#enumId').combobox({required: false});
                break;
        }
    };
    /**
     * 修订
     * */
    this.reEditElement = function (versionId) {
        btnReeditDialog = $.topDialog({
            title: '修订',
            href: btnReElementView,
            width: 800,
            height: 600,
            onLoad: function () {
                btnReeditDialog.find('#textLength').validatebox({required: true});
                Api.getVersionDetail(versionId, function (result) {
                    if (result) {
                        Api.getTypes('', function (result) {
                            if (result) {
                                btnReeditDialog.find('#elementTypeId').combobox('loadData', result);
                            }
                        });
                        result = dealResultData(result);
                        isShowData(result.dataType);
                        if (result.configEnum) {
                            isShowElement(1, result.configEnumId)
                        } else {
                            isShowElement(2)
                        }
                        if (result.dataType === "STRING") {
                            result.textLength = result.dataLength;
                            result.dataLength = null;
                        }
                        btnReeditDialog.find("form").form("load", result);
                    }
                });
                dialog = btnReeditDialog;
            },
            buttons: [
                {
                    text: '取消',
                    handler: function () {
                        $.topDialog('close', btnReeditDialog);
                    }
                },
                {
                    text: '确定',
                    id: 'addSave',
                    handler: function () {
                        if (btnReeditDialog) {
                            var isValid = btnReeditDialog.find('form').form('validate');
                            if (!isValid) {
                                return;
                            }
                            var obj = {}
                            obj.elementId = btnReeditDialog.find('#elementId').val();
                            obj.deVersionId = versionId;
                            obj.code = btnReeditDialog.find('#code').val()
                            obj.elementTypeId = btnReeditDialog.find('#elementTypeId').combobox('getValue');
                            obj.buisName = btnReeditDialog.find('#buisName').val();
                            obj.publishName = btnReeditDialog.find('#publishName').val();
                            obj.buisRemark = btnReeditDialog.find('#buisRemark').val();
                            obj.dataType = btnReeditDialog.find('input[name="dataType"]:checked').val() || '';
                            obj.dataLength = btnReeditDialog.find('#textLength').val() || btnReeditDialog.find('#dataLength').val();
                            obj.dataFormat = btnReeditDialog.find('#dataFormat').combobox('getValue') || '';
                            obj.dataPrecision = btnReeditDialog.find('#dataPrecision').val() || '0';
                            obj.configEnum = btnReeditDialog.find('input[name="configEnum"]:checked').val();
                            obj.configEnumId = btnReeditDialog.find('#enumId').combobox('getValue') || '';
                            var confEmptyVal = btnReeditDialog.find('input[name="configEmpty"]:checked').val();
                            var confUniqueVal = btnReeditDialog.find('input[name="configUnique"]:checked').val();
                            obj.configEmpty = StandardUtils.equals(confEmptyVal, "true") ? 1 : 0;
                            obj.configUnique = StandardUtils.equals(confUniqueVal, "true") ? 1 : 0;
                            obj.configRemark = btnReeditDialog.find('#configRemark').val();
                            Api.saveElements(obj, function (result) {
                                if (result) {
                                    $.topDialog('close', btnReeditDialog);
                                    top$.messager.promptInfo({
                                        msg: '修订成功',
                                        icon: 'success',
                                    });
                                    $m(elementScriptUrl).reloadGrid();
                                }
                            });
                        }
                    }
                }
            ]
        });
    };
    /**
     * 版本查看
     * */
    this.showVersion = function (versionId) {
        addElementViewDialog = $.topDialog({
            title: '查看',
            href: addElementView,
            width: 800,
            height: 600,
            onLoad: function () {
                Api.getVersionDetail(versionId, function (result) {
                    result = dealResultData(result);
                    isShowDataView(result.dataType);
                    if (result.configEnum) {
                        isShowElementView(1, result.configEnumId)
                    } else {
                        isShowElementView(2)
                    }
                    addElementViewDialog.find("form").form("load", result);
                    //审批页面日期框赋值
                    if(result.dataFormat!=undefined&&result.dataFormat!=''){
                         dialog.find('#dateFormat').combobox('setValue',result.dataFormat);
                    }
                });
                dialog = addElementViewDialog;
            },
            buttons: [
                {
                    text: '取消',
                    handler: function () {
                        $.topDialog('close', addElementViewDialog);
                    }
                },
            ]
        });
    };
    /**
     * 版本查看是否匹配字段事件
     * */
    var isShowDataView = this.isShowDataView = function (value) {
        switch (value) {
            case 'STRING' :
                dialog.find('#textLengths').show();
                dialog.find('#dataFromats').hide();
                dialog.find('#dataLengths').hide();
                dialog.find('#dataPrecisions').hide();
                break;
            case 'DATE' :
                dialog.find('#dataFromats').show();
                dialog.find('#textLengths').hide();
                dialog.find('#dataLengths').hide();
                dialog.find('#dataPrecisions').hide();
                break;
            case 'NUMBER' :
                dialog.find('#dataLengths').show();
                dialog.find('#textLengths').hide();
                dialog.find('#dataFromats').hide();
                dialog.find('#dataPrecisions').show();
                break;
        }
    };

    var isShowElementView = this.isShowElementView = function (value, configEnumId) {
        switch (value) {
            case 1 :
                dialog.find('#configEnumIds').show();
                Api.getEnumTypeList('', function (result) {
                    if (result) {
                        dialog.find('#enumId').combobox('loadData', result);
                        if (StandardUtils.isNotEmpty(configEnumId)) {
                            dialog.find('#enumId').combobox('setValue', configEnumId);
                        }
                    }
                });
                break;
            case 2 :
                dialog.find('#configEnumIds').hide();
                break;
        }
    };

    function dealResultData(result) {
        result = result || {};
        var confEmptyVal = result['configEmpty'];
        result['configEmpty'] = (confEmptyVal || StandardUtils.equals(confEmptyVal, 1)) ? true : false;
        var confUniqueVal = result['configUnique'];
        result['configUnique'] = (confUniqueVal || StandardUtils.equals(confUniqueVal, 1)) ? true : false;
        return result;
    }
});
