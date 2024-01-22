$m('dam/metamanage/model/config/infoconfig', function () {
    //=======获取当前页面菜单信息============
    //=======获取当前页面菜单信息============
    var menuInfo = TempoUtils.getPathLists();
    var modelMenu = '?module=' + encodeURIComponent(menuInfo.module.name) + '&menu=' + encodeURIComponent(menuInfo.menu.name);
    var fieldSortDataGrid; //列表排序数据表格
    var textAreaConfigDataGrid; //文本域数据表格
    var addModelTemplateDialog;//配置模型模板弹框
    var witdhConfigDataGrid;//列宽设置数据表格
    var fieldGroupAddDialog;//添加属性分组弹框
    var fieldGroupEditDialog;//编辑属性分组弹框
    var modulePath = '/dam-metamanage/api/metamanage/model/infoconfig/';
    var modelManageMoudlePath = '/dam-metamanage/api/metamanage/model/manage/'; //模型管理模块基础路径
    var sortConfigModulePath = '/dam-metamanage/api/metamanage/model/sortconfig/'; //排序信息服务基础路径
    var textAreaConfigModulePath = '/dam-metamanage/api/metamanage/model/textareaconfig/'; //文本域配置服务基础路径
    var colWidthConfigModulePath = '/dam-metamanage/api/metamanage/model/colwidthconfig/'; //列宽设置服务基础路径
    var showConfigModulePath = '/dam-metamanage/api/metamanage/model/showconfig/';//展现方式服务基础路径
    var fieldgroupModulePath = '/dam-metamanage/api/metamanage/model/fieldgroup/'; //属性分组模块基础路径
    var addModelTemplatePagePath = modulePath + 'view/templateconfig';//配置模型模板页面地址
    var fieldModulePath = '/dam-metamanage/api/metamanage/modelconfig/field/';
    var selectDataDicPath = contextPath + '/dam-metamanage/api/metamanage/standard/dic/';
    this.getSortConfigPath = sortConfigModulePath + "page";//获取排序列表
    this.getTextAreaConfigPath = textAreaConfigModulePath + "page";//文本域排序列表
    this.getColWidthConfigPath = colWidthConfigModulePath + "page";//获取列宽设置信息
    this.getFieldgroupPath = fieldgroupModulePath + "page";//获取属性分组列表
    var getModelVersionFieldPath = contextPath + '/dam-metamanage/api/metamanage/modelconfig/field/page'; //获取模型属性
    var modelFieldList = [];
    var showTypeFieldList = [];
    var showConfigGrid;
    /**
     * 文本域行高列表
     */
    var lineHightTypeList = [{
        "value": 1,
        "text": "1倍",
        "selected": true
    }, {
        "value": 2,
        "text": "2倍"
    }];

    /**
     * 获取行高下拉列表
     */
    var getLineHightTypeList = function (index, value) {
        var arr = [];
        if (!index || index < 1) {
            return arr;
        }

        for (var i = 1; i <= index; i++) {
            var element = {
                "value": i,
                "text": i + "倍",
            };
            if (value && i == value) {
                element.selected = true;
            }
            arr.push(element);
        }
        return arr;
    };

    /**
     * 将form 表单数据转成json 数据
     */
    var getFormData = function (form) {
        var formdata = {};
        var arr = form.serializeArray();
        $.each(arr, function () {
            if (formdata[this.name]) {
                if (!formdata[this.name].push) {
                    formdata[this.name] = [formdata[this.name]];
                }
                formdata[this.name].push(this.value || '');
            } else {
                formdata[this.name] = this.value || '';
            }
        });
        return formdata;
    }
    /**
     * 初始化选择对象
     */
    $(function () {
        if ($("#dam_metamanage_model_config_sort_grid").length == 1) {
            fieldSortDataGrid = $("#dam_metamanage_model_config_sort_grid");
        }
        if ($("#dam_metamanage_model_config_textarea_grid").length == 1) {
            textAreaConfigDataGrid = $("#dam_metamanage_model_config_textarea_grid");
        }
        if ($("#dam_metamanage_model_config_cloumn_width_grid").length == 1) {
            witdhConfigDataGrid = $("#dam_metamanage_model_config_cloumn_width_grid");
        }
        showConfigGrid = $("#showconfig_container");
        $m('dam/metamanage/model/config/infoconfig').loadModelFieldList(function () {

            showConfigGrid.accordion('getSelected').panel("collapse");

            showConfigGrid.find("input[name='showType']").bind("change", function () {
                if (this.value == 0) {
                    showConfigGrid.find(".showField_config").show();
                    // 显示字段必填
                    showConfigGrid.find('#showField').combobox({required: true});
                } else if (this.value == 1) {
                    showConfigGrid.find(".showField_config").hide();
                    // 显示字段必填去除
                    showConfigGrid.find('#showField').combobox({required: false});
                }
            });
            getShowConfig(showConfigLoadSuccess);
        });


    });


    /**
     * 模型展现方式加载成功事件
     * @param result
     */
    var showConfigLoadSuccess = function (result) {
        var data = result.data;
        if (data && result.status == '1') {
            showConfigGrid.find("#showConfigForm").form("load", result.data);
            if (data && data['showType'] == 1) {
                showConfigGrid.find("input[type='radio']")[0].click();
                showConfigGrid.find(".showField_config").hide();
                // 显示字段必填
                showConfigGrid.find('#showField').combobox({required: false});
            } else {
                showConfigGrid.find("input[type='radio']")[1].click();
                showConfigGrid.find(".showField_config").show();
                // 显示字段必填去除
                showConfigGrid.find('#showField').combobox({required: true});
            }
        }
        showConfigInit(result.data);
    }
    /**
     * 模型展现方式加载失败事件
     * @param result
     */
    var showConfigLoadError = function (result) {

    }
    /**
     *加载模型属性列表
     */
    this.loadModelFieldList = function (callback) {
        modelFieldList = [];
        showTypeFieldList = [];
        try {
            var modelId = $('#modelId', parent.document).val();
            //获取字段列表
            modelFieldList.splice(0);
            $.ajax({
                url: modelManageMoudlePath + 'modelFieldListByModelId',
                method: 'POST',
                dataType: "JSON",
                data: {modelId: modelId},
                success: function (result, status) {
                    var fields = result.data.rows;
                    $(fields).each(function (index, element) {
                        //过滤唯一主键
                        var unique = element.modelVerField.unique;
                        if (unique == 0) {
                            var field = {};
                            field.id = element['id'];
                            field.busiName = element.modelVerField.busiName;
                            field.value = element['id'];
                            field.text = element.modelVerField.busiName;
                            field.modelVerField = element.modelVerField;
                            showTypeFieldList.push(field);
                            modelFieldList.push(field);
                        }
                    });
                    callback();
                },
                error: function () {

                }
            });
        } catch (e) {

        }

    };
    /**
     * 展现方式combox 初始化
     */

    var showConfigInit = this.showConfigInit = function (showConfigData) {
        //查找引用对象信息
        var modelId = $('#modelId', parent.document).val();
        var fieldConfig = refObjInfo(modelId);
        var treeFieldCombox = showConfigGrid.find("#treeField");
        var treeFieldValue = '';
        var parentFieldValue = '';
        var childFieldValue = '';
        var showFieldValue = '';
        //填充已保存的展示方式信息
        if (showConfigData != undefined) {
            treeFieldValue = showConfigData.treeField;
            parentFieldValue = showConfigData.parentField;
            childFieldValue = showConfigData.childField;
            showFieldValue = showConfigData.showField;
            //树字段不为空时，填充关联对象字段
            if (treeFieldValue != '' && treeFieldValue != undefined && treeFieldValue != null) {
                //
                loadParentField(treeFieldValue, parentFieldValue, childFieldValue, showFieldValue, 'show');
            } else {
                //为空时，填充模型自身属性
                defaultFieldList(parentFieldValue, childFieldValue, showFieldValue);
            }

        }
        treeFieldCombox.combobox({
            data: fieldConfig,
            value: treeFieldValue,
            onChange: function (id) {
                //1.清空值
                showConfigClean(parentFieldValue, childFieldValue, showFieldValue);//清空选中值
                parentFieldValue = '';
                childFieldValue = '';
                showFieldValue = '';
                //2.选择空时填充表自身字段
                if (id == '') {
                    defaultFieldList(parentFieldValue, childFieldValue, showFieldValue);
                } else {
                    loadParentField(id, parentFieldValue, childFieldValue, showFieldValue, 'select');
                }

            }
        });
        //无引用对象，则显示默认属性
        if (fieldConfig.length == 0) {
            defaultFieldList(parentFieldValue, childFieldValue, showFieldValue);
        }

    };


    var defaultFieldList = function (parentFieldValue, childFieldValue, showFieldValue) {
        showConfigGrid.find("#parentField").combobox({
            data: showTypeFieldList,
            value: parentFieldValue,
        });
        showConfigGrid.find("#childField").combobox({
            data: showTypeFieldList,
            value: childFieldValue
        });
        showConfigGrid.find("#showField").combobox({
            data: showTypeFieldList,
            value: showFieldValue
        });
    }

    var loadParentField = function (id, parentFieldValue, childFieldValue, showFieldValue, flag) {
        var fieldData = childFieldChange(id);
        var refObjType = $("#refObjType").val();
        if (refObjType == 'DIC') {
            //树列表默认回填数据
            if (flag == 'select' && fieldData.struct == 'TREE') {
                parentFieldValue = fieldData.parentField;
                childFieldValue = fieldData.codeField;
                showFieldValue = fieldData.showField;
            }
            fieldData = fieldData.dicFieldList;
        }
        showConfigGrid.find("#parentField").combobox({
            data: fieldData,
            value: parentFieldValue
        });
        showConfigGrid.find("#childField").combobox({
            data: fieldData,
            value: childFieldValue
        });
        showConfigGrid.find("#showField").combobox({
            data: fieldData,
            value: showFieldValue
        });
    }


    var childFieldChange = function (id) {
        var fieldData = [];
        $.ajax({
            url: fieldModulePath + 'getFieldConfigByFieldId',
            data: {fieldId: id},
            method: 'post',
            cache: false,
            dataType: 'json',
            async: false,
            success: function (data) {
                if (data.success) {
                    var configId = data.data.id;
                    $.ajax({
                        url: fieldModulePath + 'getModelFieldConfigById',
                        data: {configId: configId},
                        method: 'post',
                        cache: false,
                        dataType: 'json',
                        async: false,
                        success: function (data) {
                            if (data.success) {
                                var fieldConfig = data.data;
                                var refObjId = fieldConfig.refObj;
                                var refObjType = fieldConfig.refObjType;
                                $("#refObjType").val(refObjType);
                                if (refObjType == 'MODEL') {
                                    $.ajax({
                                        url: fieldModulePath + 'findFieldInfo',
                                        data: {modelInfo: refObjId},
                                        method: 'post',
                                        cache: false,
                                        dataType: 'json',
                                        async: false,
                                        success: function (data) {
                                            if (data.success) {
                                                for (var i = 0; i < data.data.length; i++) {
                                                    var config = data.data[i];
                                                    var type = config.fieldDataType.busiType;
                                                    //过滤clob类型数据
                                                    if (type != 'CLOB') {
                                                        fieldData.push({
                                                            id: config.modelFieldId,
                                                            busiName: config.busiName
                                                        });
                                                    }

                                                }
                                            }
                                        }
                                    });
                                }
                                if (refObjType == 'DIC') {
                                    $.ajax({
                                        url: selectDataDicPath + "dicDataInfo",
                                        data: {dicId: refObjId},
                                        method: 'post',
                                        cache: false,
                                        dataType: 'json',
                                        async: false,
                                        success: function (data) {
                                            if (data.success) {
                                                fieldData = data.data;
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    });
                }
            }
        });
        return fieldData;
    }


    var refObjInfo = function (modelInfo) {
        var fieldConfigList = [];
        $.ajax({
            url: fieldModulePath + 'findFieldInfo',
            data: {modelInfo: modelInfo, refObjType: "1"},
            method: 'post',
            cache: false,
            dataType: 'json',
            async: false,
            success: function (data) {
                if (data.status) {
                    var filterCondition = data.data;
                    for (var i = 0; i < filterCondition.length; i++) {
                        var fieldInfo = filterCondition[i];
                        fieldConfigList.push({id: fieldInfo.modelFieldId, busiName: filterCondition[i].busiName});
                    }
                }

            }
        });
        return fieldConfigList;
    }

    /**
     * 获取模型展现方式配置信息
     */
    var getShowConfig = function (successCallBack, errorCallBack) {
        var modelId = $('#modelId', parent.document).val();
        $.ajax({
            url: showConfigModulePath + 'get?modelId=' + modelId,
            type: 'GET',
            dataType: 'JSON',
            contentType: 'application/json;charset=UTF-8',
            success: successCallBack,
            error: errorCallBack

        });
    };
    /**
     *
     */
    var getChangedShowTypeFieldList = function (list, value) {
        var arr = [];
        $(list).each(function (index, element) {
            if (element && element.value != value) {
                delete element['selected'];
                arr.push(element);
            }
        });
        return arr;

    }

    this.initModelConfig = function () {
        var dataBaseType = $('#dataBaseType', parent.document).val();
        if (dataBaseType == 'hive' || dataBaseType == 'hbase' || dataBaseType == 'mongodb') {
            $("#showconfig_container").accordion("remove", "展现方式");
            // $("#showconfig_container").accordion("remove", "查看列表排序");
        }

    };

    this.initModelFileConfig = function () {

    };
    this.clickModelConfigTree = function () {

    };
    /**
     * 清空
     */
    var showConfigClean = function () {
        showConfigGrid.find("#parentField").combobox("setValue", '');
        showConfigGrid.find("#childField").combobox("setValue", '');
        showConfigGrid.find("#showField").combobox("setValue", '');
    }


    /**
     * 清空所有下拉框的值
     */
    var showConfigCleanAll = function () {
        showConfigGrid.find("#treeField").combobox("clear");
        showConfigGrid.find("#parentField").combobox("clear");
        showConfigGrid.find("#childField").combobox("clear");
        showConfigGrid.find("#showField").combobox("clear");
    }

    /**
     * 判断是否已经存在展现方式
     */
    this.valiDataShowConfig = function () {
        getShowConfig(showConfigSave);
    }


    /**
     * 展现方式信息保存
     */
    var showConfigSave = function (result) {
        if (result.success) {
            var showConfig = result.data;
            if (showConfig != undefined && showConfig != "") {
                showConfigGrid.find("#showConfigId").val(showConfig.id);
                showConfigGrid.find("#modifyTime").val(showConfig.modifyTime);

            }
            var flag = showConfigGrid.find('form').form('validate');
            if (flag) {
                var formdata = {};
                var showType = showConfigGrid.find(':radio[name="showType"]:checked').val();
                //当单选框选择列表时，清空展现方式的值
                if (showType == '1') {
                    showConfigCleanAll();
                }
                var arr = showConfigGrid.find("#showConfigForm").serializeArray();
                var modelId = $('#modelId', parent.document).val();
                $.each(arr, function () {
                    if (formdata[this.name]) {
                        if (!formdata[this.name].push) {
                            formdata[this.name] = [formdata[this.name]];
                        }
                        formdata[this.name].push(this.value || '');
                    } else {
                        formdata[this.name] = this.value || '';
                    }
                });
                if (formdata.parentField != "" && formdata.childField != "") {
                    if (showType == '0' && formdata.parentField == formdata.childField) {
                           top$.messager.promptInfo({
                                msg:'父子段不能和子字段相同',
                                icon:'warning',
                            });
                        return false;
                    }
                }

                formdata.modelInfo = modelId;
                var url = showConfigModulePath + 'add' + modelMenu;
                //数据已经存在执行更新操作
                if (formdata && formdata['id'] && formdata['id'] != '') {
                    url = showConfigModulePath + 'update' + modelMenu;
                }
                showConfigGrid.parent().find('#save_showconfig').linkbutton('disable');
                $.ajax({
                    url: url,
                    type: 'POST',
                    async:false,
                    dataType: 'JSON',
                    contentType: 'application/json;charset=UTF-8',
                    data: JSON.stringify(formdata),
                    success: function (result) {
                        if (result.success) {
                            getShowConfig();
                            top$.messager.promptInfo({
                                msg: '保存成功',
                                icon: 'success' ,
                            });
                        } else {
                           top$.messager.promptInfo({
                                msg: '保存数据失败。',
                                icon: 'error' ,
                            });
                        }

                    },
                    error: function () {
                        top$.messager.promptInfo({
                            msg: '保存数据失败。',
                            icon: 'error' ,
                        });
                    }

                });
                showConfigGrid.parent().find('#save_showconfig').linkbutton('enable');
            } else {
                showConfigGrid.parent().find('#save_showconfig').linkbutton('enable');
            }
        }

    }
    /**
     * 添加排序字段
     */
    this.sortFieldAdd = function () {
        var record = {"fieldName": "", "dataType": "", "sortNumber": "", "sortType": ""};
        var maxIndex = fieldSortDataGrid.datagrid('getRows').length;
        fieldSortDataGrid.datagrid('appendRow', record);
        fieldSortDataGrid.datagrid('beginEdit', maxIndex);
        var sortFieldEd = fieldSortDataGrid.datagrid('getEditor', {index: maxIndex, field: 'sortField'});
        var fieldNameEd = fieldSortDataGrid.datagrid('getEditor', {index: maxIndex, field: 'fieldName'});
        var dataTypeEd = fieldSortDataGrid.datagrid('getEditor', {index: maxIndex, field: 'dataType'});
        var sortRuleEd = fieldSortDataGrid.datagrid('getEditor', {index: maxIndex, field: 'sortRule'});
        var sortPriorityEd = fieldSortDataGrid.datagrid('getEditor', {index: maxIndex, field: 'sortPriority'});

        var sortTypeList = [{
            "value": 'ASC',
            "text": "正序",
            "selected": true
        }, {
            "value": 'DESC',
            "text": "倒序"
        }];
        if (sortPriorityEd) {
            var defaultVal = maxIndex + 1
            sortPriorityEd.target.numberbox({min: 0, value: defaultVal});
        }
        if (fieldNameEd) {
            fieldNameEd.target.validatebox({required: true});
        }
        if (dataTypeEd) {
            dataTypeEd.target.validatebox({required: true});
        }

        $(modelFieldList).each(function (index, element) {
            element.selected = false;
        });
        var selectedFields = [];
        var rows = fieldSortDataGrid.datagrid('getRows');
        $(rows).each(function (index, element) {
            var tempEd = fieldSortDataGrid.datagrid('getEditor', {index: index, field: 'sortField'});
            if (tempEd) {
                var value = tempEd.target.parent().find(".combo-value").val();
                selectedFields.push(value);
            } else {
                selectedFields.push(element['sortField']);
            }
        });
        var enableFields = [];
        $(modelFieldList).each(function (index, element) {
            if (!selectedFields.includes(element['value'])) {
                enableFields.push(element);
            }

        });
        if (sortFieldEd) {
            sortFieldEd.target.combobox({
                data: enableFields,
                valueField: 'value',
                textField: 'text',
                height: 28,
                width: 191,
                required: true,
                onSelect: function (record) {
                    var modelVerField = record.modelVerField;
                    if (fieldNameEd) {
                        fieldNameEd.target.val(modelVerField['fieldName']);
                        fieldNameEd.target.attr("readonly", true);

                    }
                    if (dataTypeEd) {
                        dataTypeEd.target.val(modelVerField['dataType']);
                        dataTypeEd.target.attr("readonly", true);
                    }

                }
            });
        }
        if (sortRuleEd) {
            sortRuleEd.target.combobox({
                data: sortTypeList,
                valueField: 'value',
                textField: 'text',
                height: 28,
                width: 191,
                required: true
            });
        }


    };
    /**
     * 列表排序编辑
     */

    this.editSortConfig = function (record) {
        var index = fieldSortDataGrid.datagrid('getRowIndex', record);
        fieldSortDataGrid.datagrid('beginEdit', index);
        var sortFieldEd = fieldSortDataGrid.datagrid('getEditor', {index: index, field: 'sortField'});
        var fieldNameEd = fieldSortDataGrid.datagrid('getEditor', {index: index, field: 'fieldName'});
        var dataTypeEd = fieldSortDataGrid.datagrid('getEditor', {index: index, field: 'dataType'});
        var sortRuleEd = fieldSortDataGrid.datagrid('getEditor', {index: index, field: 'sortRule'});
        var sortField = record.sortField;
        $(modelFieldList).each(function (index, element) {
            if (element.value == sortField) {
                element.selected = true;
            } else {
                element.selected = false;
            }
        });
        var modelVerField = record['modelVerField'];
        var dataType = modelVerField['dataType'];
        dataTypeEd.target.val(dataType);
        fieldNameEd.target.val(modelVerField['busiName']);
        fieldNameEd.target.attr("readonly", true);
        dataTypeEd.target.attr("readonly", true);
        if (sortFieldEd) {
            sortFieldEd.target.combobox({
                data: modelFieldList,
                valueField: 'value',
                textField: 'text',
                height: 28,
                width: 191,
                required: true,
                onSelect: function (record) {
                    var modelVerField = record.modelVerField;
                    if (fieldNameEd) {
                        fieldNameEd.target.val(modelVerField['fieldName']);
                    }
                    if (dataTypeEd) {
                        dataTypeEd.target.val(modelVerField['dataType']);
                        dataTypeEd.target.attr("readonly",true);
                    }

                }
            });
        }
        var sortTypeList = [{
            "value": 'ASC',
            "text": "正序",
        }, {
            "value": 'DESC',
            "text": "倒序"
        }];
        var sortRule = record.sortRule;
        $(sortTypeList).each(function (index, element) {
            if (element.value == sortRule) {
                element.selected = true;
            }
        });

        if (sortRuleEd) {
            sortRuleEd.target.combobox({
                data: sortTypeList,
                valueField: 'value',
                textField: 'text',
                height: 28,
                width: 191,
                required: true
            });
        }
    };
    /**
     * 保存排序列表配置
     */
    this.saveSortConfig = function () {
        try {
            var rows = fieldSortDataGrid.datagrid("getRows");
            var fieldIds = [];
            var valiataIndex;
            var flag = true;
            var msg = "";
            $(rows).each(function (index, element) {
                if (!fieldSortDataGrid.datagrid('validateRow', index)) {
                    msg += index + 1;
                    msg += ",";
                }
            });

            if (msg != "") {
                top$.messager.promptInfo({
                    msg:'第' + msg.substring(0, msg.length - 1) + '行数据校验不通过，请检查！',
                    icon:'warning',
                });
                return;
            }
            //校验字段是否重复配置
            $(rows).each(function (index, element) {
                var tempEd = fieldSortDataGrid.datagrid('getEditor', {index: index, field: 'sortField'});
                var value;
                if (tempEd) {
                    value = tempEd.target.parent().find(".combo-value").val();
                } else {
                    value = element['sortField'];
                }
                if (fieldIds.includes(value)) {
                    valiataIndex = index + 1;
                    flag = false;
                } else {
                    fieldIds.push(value);
                }
            });
            if (!flag) {
                top$.messager.promptInfo({
                    msg:'第' + valiataIndex + '行字段重复',
                    icon:'warning',
                });
                return;
            }
            $(rows).each(function (index, element) {
                fieldSortDataGrid.datagrid('endEdit', index);
            });

            var changedRow = fieldSortDataGrid.datagrid("getChanges");
            if (changedRow && changedRow.length < 1) {
                return;
            }
            var modelId = $('#modelId', parent.document).val();

            $(changedRow).each(function (index, element) {
                element.modelInfo = modelId;
                delete element['modelVerField'];
            })
            fieldSortDataGrid.parent().find('#saveBtn').linkbutton('disable');
            $.ajax({
                url: sortConfigModulePath + 'add' + modelMenu,
                type: 'POST',
                data: JSON.stringify(changedRow),
                dataType: 'JSON',
                contentType: 'application/json;charset=UTF-8',
                success: function (result) {
                    if (!result.success) {
                        top$.messager.promptInfo({
                            msg:result.message,
                            icon:'warning',
                        });
                        fieldSortDataGrid.datagrid("reload");
                    } else {
                        top$.messager.promptInfo({
                            msg:'保存成功',
                            icon:'success',
                        });
                        fieldSortDataGrid.datagrid("reload");
                    }

                },
                error: function () {
                    fieldSortDataGrid.datagrid("reload");
                    top$.messager.promptInfo({
                        msg: '保存失败！',
                        icon: 'error' ,
                    });
                }
            })
            fieldSortDataGrid.parent().find('#saveBtn').linkbutton('enable');

        } catch (e) {

        }
    };

    /**
     * 获取查询参数
     */
    this.queryParam = function () {
        var modelId = $('#modelId', parent.document).val();

        return {modelInfo: modelId}
    };
    /**
     * 获取查询参数
     */
    this.getFieldConfigQueryParams = function () {
        var modelVersionId = $('#modelverid', parent.document).val();
        return {'modelVersionId': modelVersionId};
    };

    /**
     * 删除排序列表行
     */
    this.deleteRow = function () {
        var getCheckRows = fieldSortDataGrid.datagrid("getChecked");
        if (!getCheckRows || getCheckRows.length < 1) {
            top$.messager.promptInfo({
                msg:'请选中要删除的行。',
                icon:'warning',
            });
            return false;
        }
        var postdata = [];
        $(getCheckRows).each(function (index, element) {
            var index = fieldSortDataGrid.datagrid("getRowIndex", element);
            if (element.id) {
                postdata.push({"id": element.id});
            }
            fieldSortDataGrid.datagrid("deleteRow", index);
        });
        if (postdata.length < 1) {
            return;
        }
        $.ajax({
            url: sortConfigModulePath + "delete" + modelMenu,
            method: 'POST',
            data: JSON.stringify(postdata),
            dataType: 'JSON',
            contentType: 'application/json',
            success: function (result) {
                if (!result.success) {
                    $.Msg.alert('提示', result.message);
                } else {
                    fieldSortDataGrid.datagrid("reload");
                }
            },
            error: function () {
                top$.messager.promptInfo({
                    msg:'删除失败。',
                    icon:'error ',
                });
            }
        });


    };
    /**
     * 添加属性分组
     */
    this.openfieldGroupAddDialog = function () {
        fieldGroupAddDialog = $.topDialog({
            title: '添加',
            modal: true,
            width: 800,
            height: 607,
            href: modulePath + "view/filegroupadd",
            onLoad: function () {
                try {
                    var maxIndex = $("#dam_metamanage_model_fiel_group_grid").datagrid('getRows').length;
                    var unSelectField = fieldGroupAddDialog.find("#unSelectField");
                    fieldGroupAddDialogLoadEvent(unSelectField);
                    var record = {sortNumber: maxIndex + 1};
                    fieldGroupAddDialog.find('form').form('load', record);
                } catch (e) {
                }

            },
            buttons: [{
                text: '关闭',
                handler: function () {
                    $.topDialog('close', fieldGroupAddDialog);
                }
            },
                {
                    text: '保存',
                    id: 'addSaveBtn',
                    handler: fieldGroupAddSave
                },
            ]
        });
    };


    /**
     * 添加属性分组弹框加载成功事件
     */
    var fieldGroupAddDialogLoadEvent = function (unSelectField) {
        var modelVersionId = $('#modelverid', parent.document).val();
        $.ajax({
            url: fieldgroupModulePath + "addField" + modelMenu,
            type: 'POST',
            data: {modelVersionId: modelVersionId},
            dataType: 'JSON',
            success: function (result) {
                var rows = result.data;
                $(rows).each(function (index, row) {
                    unSelectField.append("<option id='" + row.modelFieldId + "' value='" + row.modelFieldId + "'>" + row.busiName + "[" + row.fieldName + "]" + "</option>");
                });

            },
            error: function () {

            }
        });

    }
    /**
     * 保存模型分组数据
     */
    var fieldGroupAddSave = function () {
        currentDialog = fieldGroupAddDialog;
        var data = {};
        var selectField;
        if (currentDialog) {
            var $form = currentDialog.find("form");
            data = getFormData($form);
            selectField = currentDialog.find('#selectField')[0];
        }
        var flag = currentDialog.find('form').form('validate');
        if (flag) {
            var selectFieldValue = "";
            var options = selectField.options;
            var modelFieldGroupRels = [];
            $(options).each(function (index, option) {
                var modelFieldGroupRel = {modelField: option.value};
                modelFieldGroupRels.push(modelFieldGroupRel);
            })
            if (modelFieldGroupRels.length == 0) {
                top$.messager.promptInfo({
                    msg:'一个分组至少需包含一个属性分组字段！',
                    icon:'warning',
                });
                return false;
            }
            data.modelFieldGroupRels = modelFieldGroupRels;
            var modelId = $('#modelId', parent.document).val();
            data.modelInfo = modelId;
            fieldGroupAddDialog.parent().find('#addSaveBtn').linkbutton('disable');
            $.ajax({
                url: fieldgroupModulePath + 'add' + modelMenu,
                type: 'POST',
                data: JSON.stringify(data),
                dataType: 'JSON',
                async: false,
                contentType: 'application/json',
                success: function (result) {
                    //添加成功关闭当前弹窗
                    if (result.success) {
                        $.topDialog('close', currentDialog);
                        var fileGroupDataGrid = $("#dam_metamanage_model_fiel_group_grid");
                        fileGroupDataGrid.datagrid("reload");
                    } else {
                        top$.messager.promptInfo({
                            msg:result.message,
                            icon:'warning',
                        });
                    }

                },
                error: function () {
                    currentDialog.find('#addSaveBtn').linkbutton('enable');
                    top$.messager.promptInfo({
                        msg: '删除数据失败。',
                        icon: 'error' ,
                    });
                }
            });
            fieldGroupAddDialog.parent().find('#addSaveBtn').linkbutton('enable');
        }
    }

    /**
     * 打开属性分组编辑弹框
     */
    this.openfieldGroupEditDialog = function (record) {
        fieldGroupEditDialog = $.topDialog({
            title: '编辑',
            modal: true,
            width: 800,
            height: 607,
            href: modulePath + "view/filegroupedit",
            onLoad: function () {
                try {
                    var unSelectField = fieldGroupEditDialog.find("#unSelectField");
                    var selectField = fieldGroupEditDialog.find('#selectField');
                    fieldGroupEditDialogLoadEvent(unSelectField, selectField, record);
                } catch (err) {
                }
            },
            buttons: [{
                text: '关闭',
                handler: function () {
                    $.topDialog('close', fieldGroupEditDialog);
                }
            },
                {
                    text: '保存',
                    id: 'addSaveBtn',
                    handler: fieldGroupEditSave
                },
            ]
        });
    };


    /**
     * 编辑模型分组数据提交
     */
    var fieldGroupEditSave = function () {
        currentDialog = fieldGroupEditDialog;
        var data = {};
        var selectField;
        if (currentDialog) {
            var $form = currentDialog.find("form");
            data = getFormData($form);
            selectField = currentDialog.find('#selectField')[0];
        }
        var flag = currentDialog.find('form').form('validate');
        if (flag) {
            var selectFieldValue = "";
            var options = selectField.options;
            var modelFieldGroupRels = [];
            $(options).each(function (index, option) {
                var modelFieldGroupRel = {modelField: option.value};
                modelFieldGroupRels.push(modelFieldGroupRel);
            })
            if (modelFieldGroupRels.length == 0) {
               top$.messager.promptInfo({
                    msg:'一个分组至少需包含一个属性分组字段！',
                    icon:'warning',
                });
                return false;
            }
            data.modelFieldGroupRels = modelFieldGroupRels;
            var modelId = $('#modelId', parent.document).val();
            data.modelInfo = modelId;
            fieldGroupEditDialog.parent().find('#addSaveBtn').linkbutton('disable');
            $.ajax({
                url: fieldgroupModulePath + 'add' + modelMenu,
                type: 'POST',
                data: JSON.stringify(data),
                dataType: 'JSON',
                async: false,
                contentType: 'application/json',
                success: function (result) {
                    //添加成功关闭当前弹窗
                    if (result.success) {
                        $.topDialog('close', currentDialog);
                        var fileGroupDataGrid = $("#dam_metamanage_model_fiel_group_grid");
                        fileGroupDataGrid.datagrid("reload");
                    } else {
                       top$.messager.promptInfo({
                            msg:  result.message,
                            icon: 'error' ,
                        });
                    }
                }
            });
            fieldGroupEditDialog.parent().find('#addSaveBtn').linkbutton('enable');
        }
    }

    /**
     * 属性分组编辑弹框加载后事件
     */
    var fieldGroupEditDialogLoadEvent = function (unSelectField, selectField, record) {
        $.ajax({
            url: fieldgroupModulePath + "info",
            type: 'GET',
            data: {'id': record.id},
            dataType: 'JSON',
            success: function (result) {
                fieldGroupEditDialog.find("form").form("load", result.data);
                var unSelectRows = result.data.unSelectModelVerField;
                $(unSelectRows).each(function (index, row) {
                    unSelectField.append("<option id='" + row.modelFieldId + "' value='" + row.modelFieldId + "'>" + row.busiName + "[" + row.fieldName + "]" + "</option>");
                });
                var selectRows = result.data.selectModelVerField;
                $(selectRows).each(function (index, row) {
                    selectField.append("<option id='" + row.modelFieldId + "' value='" + row.modelFieldId + "'>" + row.busiName + "[" + row.fieldName + "]" + "</option>");
                });

            },
            error: function () {

            }
        });
    }
    /**
     * 选择属性
     */
    this.toSelect = function (flag) {
        var currentDialog;
        if (flag == 'edit') {
            currentDialog = fieldGroupEditDialog;
        }
        if (flag == 'add') {
            currentDialog = fieldGroupAddDialog;
        }
        var unSelectField;
        var selectField;
        if (currentDialog) {
            unSelectField = currentDialog.find('#unSelectField');
            selectField = currentDialog.find('#selectField');
        }
        var field = unSelectField.val();
        for (var i = 0; i < field.length; i++) {
            var curSelect = currentDialog.find('#' + field[i] + '');
            if (field != null) {
                selectField.append("<option value='" + field[i] + "' id='" + field[i] + "'>" + curSelect.html() + "</option>");
                curSelect.remove();
            }
        }
    }
    /**
     * 移除已经选择的属性
     */
    this.reSelect = function (flag) {
        var currentDialog;
        if (flag == 'edit') {
            currentDialog = fieldGroupEditDialog;
        }
        if (flag == 'add') {
            currentDialog = fieldGroupAddDialog;
        }
        var unSelectField = currentDialog.find('#unSelectField');
        var selectField = currentDialog.find('#selectField');
        var field = selectField.val();

        for (var i = 0; i < field.length; i++) {
            var curSelect = currentDialog.find('#' + field[i] + '');
            if (field != null) {
                unSelectField.append("<option value='" + field[i] + "' id='" + field[i] + "'>" + curSelect.html() + "</option>");
                curSelect.remove();
            }
        }
    }

    /**
     * 删除文本域配置信息
     */
    this.delfieldGroup = function () {
        var fileGroupDataGrid = $("#dam_metamanage_model_fiel_group_grid");
        var rows = fileGroupDataGrid.datagrid("getChecked");
        if (rows && rows.length < 1) {
            top$.messager.promptInfo({
                msg:'请选择一条数据！',
                icon:'warning',
            });
            return;
        }
        $.ajax({
            url: fieldgroupModulePath + 'delete' + modelMenu,
            type: 'POST',
            dataType: 'JSON',
            contentType: 'application/json;charset=UTF-8',
            data: JSON.stringify(rows),
            success: function (result, status) {
                if (result && result.status == '1') {
                    fileGroupDataGrid.datagrid("reload");
                } else {
                   top$.messager.promptInfo({
                        msg: '删除数据失败',
                        icon: 'error',
                    });
                }

            },
            error: function () {
                top$.messager.promptInfo({
                    msg: '删除数据失败',
                    icon: 'error',
                });
            }

        });
    }
    /**
     * 文本域设置
     */
    this.textAreaConfigAdd = function () {

        if (!textAreaConfigDataGrid) {
            textAreaConfigDataGrid = $("#dam_metamanage_model_config_textarea_grid");
        }
        var record = {"sortField": "", "rowsValue": "", "colsValue": ""};
        var maxIndex = textAreaConfigDataGrid.datagrid('getRows').length;
        textAreaConfigDataGrid.datagrid('appendRow', record);
        textAreaConfigDataGrid.datagrid('beginEdit', maxIndex);
        var modelFieldEd = textAreaConfigDataGrid.datagrid('getEditor', {index: maxIndex, field: 'modelField'});
        var rowsEd = textAreaConfigDataGrid.datagrid('getEditor', {index: maxIndex, field: 'rowsValue'});
        var colsEd = textAreaConfigDataGrid.datagrid('getEditor', {index: maxIndex, field: 'colsValue'});
        var modelFieldListFilter = [];
        $(modelFieldList).each(function (index, element) {
            var modelVerField = element['modelVerField'];
            var dataType = modelVerField.fieldDataType.busiType;
            if (dataType == 'STRING' || dataType == 'CLOB') {
                modelFieldListFilter.push(element);
            }

        });
        if (modelFieldEd) {
            modelFieldEd.target.combobox({
                data: modelFieldListFilter,
                valueField: 'value',
                textField: 'text',
                width: 309,
                required: true
            });
            modelFieldEd.target.combobox('setValue', '');
        }
        if (rowsEd) {
            rowsEd.target.combobox({
                data: getLineHightTypeList(2, 2),
                valueField: 'value',
                textField: 'text',
                width: 309,
                required: true
            });
            rowsEd.target.combobox('setValue', '');
        }
        if (colsEd) {
            colsEd.target.combobox({
                data: getLineHightTypeList(10, 2),
                valueField: 'value',
                textField: 'text',
                width: 309,
                required: true
            });
            colsEd.target.combobox('setValue', '');
        }
    };

    /**
     * 保存排序列表配置
     */
    this.saveTextAreaConfig = function () {
        try {
            if (!textAreaConfigDataGrid) {
                textAreaConfigDataGrid = $("#dam_metamanage_model_config_textarea_grid");
            }
            var rows = textAreaConfigDataGrid.datagrid("getRows");
            var msg = "";
            $(rows).each(function (index, element) {
                if (!textAreaConfigDataGrid.datagrid('validateRow', index)) {
                    msg += index + 1;
                    msg += ",";
                }
            });

            if (msg != "") {
               top$.messager.promptInfo({
                    msg:'第' + msg.substring(0, msg.length - 1) + '行数据校验不通过，请检查！',
                    icon:'warning',
                });
                return;
            }
            //校验字段是否重复配置
            var valiataIndex;
            var flag = true;
            var fieldIds = [];
            $(rows).each(function (index, element) {
                var tempEd = textAreaConfigDataGrid.datagrid('getEditor', {index: index, field: 'modelField'});
                var value;
                if (tempEd) {
                    value = tempEd.target.parent().find(".combo-value").val();
                } else {
                    value = element['modelField'];
                }
                if (fieldIds.includes(value)) {
                    valiataIndex = index + 1;
                    flag = false;
                } else {
                    fieldIds.push(value);
                }
            });
            if (!flag) {
                top$.messager.promptInfo({
                    msg:'第' + valiataIndex + '行字段重复',
                    icon:'warning',
                });
                return;

            }
            $(rows).each(function (index, element) {
                textAreaConfigDataGrid.datagrid('endEdit', index);
            });

            var changedRow = textAreaConfigDataGrid.datagrid("getChanges");
            if (changedRow && changedRow.length < 1) {
                return;
            }
            var modelId = $('#modelId', parent.document).val();

            $(changedRow).each(function (index, element) {
                element.modelInfo = modelId;
            })
            textAreaConfigDataGrid.parent().find('#saveBtn').linkbutton('disable');
            $.ajax({
                url: textAreaConfigModulePath + 'add' + modelMenu,
                type: 'POST',
                data: JSON.stringify(changedRow),
                dataType: 'JSON',
                async: false,
                contentType: 'application/json;charset=UTF-8',
                success: function (result) {
                    textAreaConfigDataGrid.datagrid("reload");
                    if (!result.success) {
                       top$.messager.promptInfo({
                            msg:result.message,
                            icon:'warning',
                       });
                    } else {
                        top$.messager.promptInfo({
                            msg:'保存成功',
                            icon:'success',
                        });
                    }
                },
                error: function () {
                    textAreaConfigDataGrid.datagrid("reload");
                    top$.messager.promptInfo({
                        msg: '保存失败！',
                        icon: 'error' ,
                    });
                }
            })
            textAreaConfigDataGrid.parent().find('#saveBtn').linkbutton('enable');

        } catch (e) {
        }
    };
    /**
     * 删除文本域配置信息
     */
    this.delTextAreaConfig = function () {
        if (!textAreaConfigDataGrid) {
            textAreaConfigDataGrid = $("#dam_metamanage_model_config_textarea_grid");
        }
        var selections = textAreaConfigDataGrid.datagrid('getSelections');
        if (selections.length == 0) {
            top$.messager.promptInfo({
                msg:'请选中要删除的行！',
                icon:'warning',
            });
            return;
        }
        var postdata = [];
        $(selections).each(function (index, element) {
            var index = textAreaConfigDataGrid.datagrid("getRowIndex", element);
            if (element.id) {
                postdata.push(element);
            }
            textAreaConfigDataGrid.datagrid("deleteRow", index);
        });
        if (postdata.length < 1) {
            return;
        }

        $.ajax({
            url: textAreaConfigModulePath + 'delete' + modelMenu,
            type: 'POST',
            dataType: 'JSON',
            contentType: 'application/json;charset=UTF-8',
            data: JSON.stringify(postdata),
            success: function (result, status) {
                if (result && result.status == '1') {
                    textAreaConfigDataGrid.datagrid("reload");
                } else {
                    top$.messager.promptInfo({
                        msg: '删除数据失败！',
                        icon:'warning',
                    });
                }
            },
            error: function () {
                top$.messager.promptInfo({
                    msg: '删除数据失败！',
                    icon:'warning',
                });
            }
        });
    }

    /**
     * 文本域行数据编辑
     */

    this.editTextAreaConfig = function (record) {
        if (!textAreaConfigDataGrid) {
            textAreaConfigDataGrid = $("#dam_metamanage_model_config_textarea_grid");
        }
        var index = textAreaConfigDataGrid.datagrid('getRowIndex', record);
        var modelFieldEd = textAreaConfigDataGrid.datagrid('getEditor', {index: index, field: 'modelField'});
        var rowsEd = textAreaConfigDataGrid.datagrid('getEditor', {index: index, field: 'rowsValue'});
        var colsEd = textAreaConfigDataGrid.datagrid('getEditor', {index: index, field: 'colsValue'});
        if (rowsEd != null) {
            return;
        } else {
            textAreaConfigDataGrid.datagrid('beginEdit', index);
            modelFieldEd = textAreaConfigDataGrid.datagrid('getEditor', {index: index, field: 'modelField'});
            rowsEd = textAreaConfigDataGrid.datagrid('getEditor', {index: index, field: 'rowsValue'});
            colsEd = textAreaConfigDataGrid.datagrid('getEditor', {index: index, field: 'colsValue'});
            var modelField = record.modelField;
            $(modelFieldList).each(function (index, element) {
                if (element.value == modelField) {
                    element.selected = true;
                }
            });
            $(modelFieldList).each(function (index, element) {
                if (element.value == record.modelField) {
                    element.selected = true;
                } else {
                    element.selected = false;
                }
            });
            modelFieldEd.target.combobox({
                data: modelFieldList,
                valueField: 'value',
                textField: 'text',
                height: 22,
                width: 309,
                required: true
            });

            rowsEd.target.combobox({
                data: getLineHightTypeList(2, record['rowsValue']),
                valueField: 'value',
                textField: 'text',
                height: 22,
                width: 309,
                required: true
            });
            colsEd.target.combobox({
                data: getLineHightTypeList(10, record['colsValue']),
                valueField: 'value',
                textField: 'text',
                height: 22,
                width: 309,
                required: true
            });
        }
    };
    /**
     * 列宽设置新增行
     */
    this.columnWidthConfigAdd = function () {
        if (!witdhConfigDataGrid) {
            witdhConfigDataGrid = $("#dam_metamanage_model_config_cloumn_width_grid");
        }
        var record = {"modelField": "", "colWidth": ""};
        var maxIndex = witdhConfigDataGrid.datagrid('getRows').length;
        witdhConfigDataGrid.datagrid('appendRow', record);
        witdhConfigDataGrid.datagrid('beginEdit', maxIndex);
        var modelFieldEd = witdhConfigDataGrid.datagrid('getEditor', {index: maxIndex, field: 'modelField'});
        var colWidthEd = witdhConfigDataGrid.datagrid('getEditor', {index: maxIndex, field: 'colWidth'});
        if (modelFieldEd) {
            modelFieldEd.target.combobox({
                data: modelFieldList,
                valueField: 'value',
                textField: 'text',
                width: 471,
                required: true
            });
            modelFieldEd.target.combobox('setValue', '');
        }
        if (colWidthEd) {
            colWidthEd.target.numberbox({
                max: 1200,
                width: 471
            });
            colWidthEd.target.numberbox('setValue', '');
        }
    };

    /**
     * 列宽设置数据保存
     */
    this.columnWidthConfigSave = function () {
        if (!witdhConfigDataGrid) {
            witdhConfigDataGrid = $("#dam_metamanage_model_config_cloumn_width_grid");
        }
        var rows = witdhConfigDataGrid.datagrid("getRows");
        var msg = "";
        $(rows).each(function (index, element) {
            if (!witdhConfigDataGrid.datagrid('validateRow', index)) {
                msg += index + 1;
                msg += ",";
            }
        });

        if (msg != "") {
            top$.messager.promptInfo({
                msg:'第' + msg.substring(0, msg.length - 1) + '行数据校验不通过，请检查！',
                icon:'warning',
            });
            return;
        }
        //校验字段是否重复配置
        var valiataIndex;
        var flag = true;
        var fieldIds = [];
        $(rows).each(function (index, element) {
            var tempEd = witdhConfigDataGrid.datagrid('getEditor', {index: index, field: 'modelField'});
            var value;
            if (tempEd) {
                value = tempEd.target.parent().find(".combo-value").val();
            } else {
                value = element['modelField'];
            }
            if (fieldIds.includes(value)) {
                valiataIndex = index + 1;
                flag = false;
            } else {
                fieldIds.push(value);
            }
        });
        if (!flag) {
            top$.messager.promptInfo({
                msg:'第' + valiataIndex + '行字段重复',
                icon:'warning',
            });
            return;
        }
        $(rows).each(function (index, element) {
            witdhConfigDataGrid.datagrid('endEdit', index);
        });
        var changedRow = witdhConfigDataGrid.datagrid("getChanges");
        if (changedRow && changedRow.length < 1) {
            return;
        }
        var modelId = $('#modelId', parent.document).val();

        $(changedRow).each(function (index, element) {
            element.modelInfo = modelId;
            delete element['modelVerField'];
        });
        witdhConfigDataGrid.parent().find('#saveBtn').linkbutton('disable');
        $.ajax({
            url: colWidthConfigModulePath + 'add' + modelMenu,
            type: 'POST',
            data: JSON.stringify(changedRow),
            dataType: 'JSON',
            async: false,
            contentType: 'application/json;charset=UTF-8',
            success: function (result) {
                witdhConfigDataGrid.datagrid("reload");
                if (!result.success) {
                    top$.messager.promptInfo({
                        msg:result.message,
                        icon:'warning',
                    });
                } else {
                    top$.messager.promptInfo({
                        msg:'保存成功',
                        icon:'success',
                    });
                }
            },
            error: function () {
                witdhConfigDataGrid.datagrid("reload");
                top$.messager.promptInfo({
                    msg:'保存失败',
                    icon:'success',
                });
            }
        })
        witdhConfigDataGrid.parent().find('#saveBtn').linkbutton('enable');
    };

    /**
     * 列宽设置删除
     */
    this.columnWidthConfigdel = function () {
        if (!witdhConfigDataGrid) {
            witdhConfigDataGrid = $("#dam_metamanage_model_config_cloumn_width_grid");
        }
        var selections = witdhConfigDataGrid.datagrid('getSelections');
        if (selections.length == 0) {
           top$.messager.promptInfo({
                msg:'请选中要删除的行。',
                icon:'warning',
            });
            return;
        }
        var postdata = [];
        $(selections).each(function (index, element) {
            var index = witdhConfigDataGrid.datagrid("getRowIndex", element);
            if (element.id) {
                postdata.push(element);
            }
            witdhConfigDataGrid.datagrid("deleteRow", index);
        });
        if (postdata.length < 1) {
            return;
        }

        $.ajax({
            url: colWidthConfigModulePath + 'delete' + modelMenu,
            type: 'POST',
            dataType: 'JSON',
            contentType: 'application/json;charset=UTF-8',
            data: JSON.stringify(postdata),
            success: function (result, status) {
                if (result && result.status == '1') {
                    witdhConfigDataGrid.datagrid("reload");
                } else {
                    top$.messager.promptInfo({
                        msg:'删除数据失败。',
                        icon:'error',
                    });
                }

            },
            error: function () {
                top$.messager.promptInfo({
                    msg:'删除数据失败。',
                    icon:'error',
                });
            }

        });
    };
    /**
     * 文本域行数据编辑
     */

    this.editColumnWidthConfig = function (record) {
        if (!witdhConfigDataGrid) {
            witdhConfigDataGrid = $("#dam_metamanage_model_config_cloumn_width_grid");
        }
        var index = witdhConfigDataGrid.datagrid('getRowIndex', record);
        var modelFieldEd = witdhConfigDataGrid.datagrid('getEditor', {index: index, field: 'modelField'});
        var colWidthEd = witdhConfigDataGrid.datagrid('getEditor', {index: index, field: 'colWidth'});
        if (colWidthEd != null) {
            return;
        } else {
            witdhConfigDataGrid.datagrid('beginEdit', index);
            modelFieldEd = witdhConfigDataGrid.datagrid('getEditor', {index: index, field: 'modelField'});
            colWidthEd = witdhConfigDataGrid.datagrid('getEditor', {index: index, field: 'colWidth'});
            $(modelFieldList).each(function (index, element) {
                if (element.value == record.modelField) {
                    element.selected = true;
                } else {
                    element.selected = false;
                }
            });
            modelFieldEd.target.combobox({
                data: modelFieldList,
                valueField: 'value',
                textField: 'text',
                height: 22,
                width: 309,
                required: true
            });
            colWidthEd.target.val(record.colWidth);
        }
    };

    var dataTypeFormt = this.dataTypeFormt = function (value, row, index) {
        if (value != null && value != "") {
            value = row.modelVerField.fieldDataType.busiType;
        }
        if (value == "STRING") {
            value = "字符型";
        } else if (value == "NUMBER") {
            value = "数值型";
        } else if (value == "DATE") {
            value = "日期型";
        } else if (value == "TIME") {
            value = "时间型";
        }
        return value;
    };

});