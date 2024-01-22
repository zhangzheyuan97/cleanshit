$m('dam/metamanage/standard/dataelement/list', function () {

    var uploadDataView = moduleResource.element.getView('upload');
    var elementListId = 'dam_matemanage_data_standard_element_grid';
    var elementScriptUrl = 'dam/metamanage/standard/dataelement';
    var elTypeManageUrl = 'dam/metamanage/standard/dataelement/typeManage';
    var versionManageUrl = 'dam/metamanage/standard/dataelement/versionManage';
    var standardDownUrl = 'dam/metamanage/standard/dataelement/standardDown';
    var curElManageUrl = 'dam/metamanage/standard/dataelement/list';

    var elementGrid;
    $(function () {
        elementGrid = $('#' + elementListId);
    });

    this.loadElementPageSuccess = function (data) {

    };

    //发布
    this.publishedTasks = function () {
        var selectRows = elementGrid.datagrid('getSelections');
        if (StandardUtils.listIsEmpty(selectRows)) {
            top$.messager.promptInfo({
                msg: '请至少选择一条记录进行发布!',
                icon: 'warning',
            });
            return;
        }
        top$.messager.progress({
            text: '处理中，请稍候....', interval: 500, width: 500
        });
        var ids = getSelectRowValues('versionId', selectRows);
        Api.submitTask(ids, function (result) {
            top$.messager.progress('close');
            result = result || {};
            top$.messager.promptInfo({
                msg: result.message,
                icon: result.success ? 'info' : 'error',
            });
            if (result.success) {
                reloadGridData();
            }
        })
    };

    //送审
    this.startTasks = function () {
        var selectRows = elementGrid.datagrid('getSelections');
        if (StandardUtils.listIsEmpty(selectRows)) {
            top$.messager.promptInfo({
                msg: '请至少选择一条记录进行送审!',
                icon: 'warning',
            });
            return;
        }
        var ids = getSelectRowValues('versionId', selectRows);
        Api.startApproval(ids, function (result) {
            result = result || {};
            top$.messager.promptInfo({
                msg: result.message,
                icon: result.success ? 'info' : 'error',
            });
            if (result.success) {
                reloadGridData();
            }
        })
    };

    //删除
    this.delElement = function () {
        var selectRows = elementGrid.datagrid('getSelections');
        var ids = getSelectRowValues('elementId', selectRows);
        if (StandardUtils.listIsEmpty(selectRows)) {
            top$.messager.promptInfo({
                msg: '请至少选择一条记录进行删除!',
                icon: 'warning',
            });
            return;
        }
        for (let i = 0; i < selectRows.length; i++) {
            var selectRow = selectRows[i];
            //审批中数据不能直接删除，需通过取消流程删除
            if (selectRow && selectRow.versionStatus && selectRow.versionStatus === 'review') {
                top$.messager.promptInfo({
                    msg: '存在不支持的删除数据，请检查!',
                    icon: 'warning',
                });
                return;
            }
        }
        $.Msg.confirm('提示', '确认删除数据吗？', function (flag) {
            if (flag) {
                Api.deleteElements(ids, function (result) {
                    result = result || {};
                    top$.messager.promptInfo({
                        msg: result.message,
                        icon: result.success ? 'info' : 'error',
                    });
                    if (result.success) {
                        reloadGridData();
                    }
                })
            }
        });
    };
    //导入
    this.uploadElement = function () {
        var uploadElDialog = $.topDialog({
            title: '导入',
            href: uploadDataView,
            width: 600,
            height: 400,
            buttons: [
                {
                    text: '取消',
                    handler: function () {
                        $.topDialog('close', uploadElDialog);
                    }
                },
                {
                    text: '确定',
                    id: 'editSave',
                    handler: function uploadData() {
                        uploadElDialog.parent().find('#editSave').linkbutton('disable');
                        var form = uploadElDialog.find("#dam_matemanage_data_standard_element_upload_id");
                        var formFile = form.find('#file');
                        var filePath = formFile.val();
                        if (StandardUtils.isNotEmpty(filePath)) {
                            var url = Ajax.getResources('element', 'upload', {});
                            //构建formData,通过ajax上传文件
                            var formData = new FormData();
                            $.each(formFile[0].files, function (i, item) {
                                formData.append("file", item, item.name);
                            });
                            $.ajax({
                                url: url,
                                type: 'post',
                                data: formData,
                                processData: false,
                                contentType: false,
                                cache: false,
                                success: function (result) {
                                    result = result || {};
                                    top$.messager.promptInfo({
                                        msg: result.message,
                                        icon: result.success ? 'successs' : 'error',
                                    });
                                    if (result.success) {
                                        $.topDialog('close', uploadElDialog);
                                        reloadGridData();
                                    }
                                },
                                error: function (rs) {
                                    if (rs.status == 0 && 'rejected' == rs.state()) {
                                        $.Msg.alert('提示', '文件可能被删除、移动、修改，请重新选择上传！');
                                        formFile.val('');
                                    }
                                },
                                complete: function (data) {
                                    uploadElDialog.parent().find('#editSave').linkbutton('enable');
                                }
                            });

                            // Api.uploadElement(form, function (result) {
                            //     result = result || {};
                            //     top$.messager.promptInfo({
                            //         msg: result.message,
                            //         icon: result.success ? 'successs' : 'error',
                            //     });
                            //     if (result.success) {
                            //         $.topDialog('close', uploadElDialog);
                            //     }
                            //     reloadGridData();
                            // });
                        }else{
                            top$.messager.promptInfo({
                                msg: '请选择导入文件',
                                icon: 'warning',
                            });
                        }
                    }
                }
            ]
        });
    };

    //模板下载
    this.downloadTempFile = function () {
        var form = $('<form>').attr('method', 'post').hide();
        $('body').append(form);
        Api.downloadTempFile(form, function (result) {
            form.delete();
        });
    };

    //导出
    this.downloadElement = function () {
        var form = $('<form>').attr('method', 'post').hide();
        var selectRows = elementGrid.datagrid('getSelections');
        if (StandardUtils.listIsNotEmpty(selectRows)) {
            var ids = getSelectRowValues('versionId', selectRows);
            var inputFiled = $("<input>");
            inputFiled.attr("type", "hidden");
            inputFiled.attr("name", "ids");
            inputFiled.attr("value", ids.join(","));
            form.append(inputFiled);
        }
        $('body').append(form);
        Api.downloadElement(form, function (result) {
            form.delete();
        });
    };

    //数据元分类
    this.typeManage = function () {
        $m(elTypeManageUrl).initELTypeManage();
    };

    //格式化数据类型
    this.fmtDataType = function (value, row, index) {
        var fmtOps = {
            NUMBER: '数值型',
            STRING: '字符型',
            DATE: '日期型'
        };
        value = fmtOps[value];
        return StandardUtils.isNotEmpty(value) ? value : '';
    };

    //格式化状态
    this.fmtVersionStatus = function (value, row, index) {
        var fmtOps = {
            edit: {label: '编辑中', color: '#2E8AE6'},
            reject: {label: '已驳回', color: '#F04D5D'},
            review: {label: '审核中', color: '#F09D3F'},
            published: {label: '已发布', color: '#88C24E'},
            abolished: {label: '已废止', color: '#F04D5D'},
            history: {label: '历史', color: '#91869f'}
        };
        value = fmtOps[value];
        if (value) {
            var label = value.label;
            var color = value.color;
            return "<span style='color:" + color + ";font:bold;'>" + label + "</span>";
        }
        return null;
    };

    //格式化版本
    this.fmtVersion = function (value, row, index) {
        return Number(value) + 1;
    };

    //格式化创建时间
    this.fmtCreateTime = function (value, row, index) {
        if (StandardUtils.isNotEmpty(value)) {
            var valueFmt = StandardUtils.dateFmtTime(value);
            var value= "<p style='cursor: pointer'><span class='bubbling_hint' title='" + valueFmt + "'>" + valueFmt + "</span></p>";
            return value;
        }
    };

    /**
     * 冒泡
     */
    this.formatType = function(value, row, index){
        value = value ? value : '';
        return "<p style='cursor: pointer'><span class='bubbling_hint' title='" + value + "'>" + value + "</span></p>";
    };
    /**
     *
     * @param value
     * @param row
     * @param index
     * @returns {*|string}
     */
    //操作栏的展示信息
    this.actions = function (value, row, index) {
        var status = row.versionStatus;
        var config = getActionBtnConfig();
        var btns = statusConfig[status] || [];
        btns = sortBtnList(config, btns);
        var actionHtml = '';
        for (var idx = 0; idx < btns.length; idx++) {
            switch (btns[idx]) {
                case 'edit'://编辑
                    var btnFun = " onclick=\"$m('" + curElManageUrl + "').btnEditElement('" + row.versionId + "');\"";
                    actionHtml += creatBtnLink(btnFun, '编辑', ' #2E8AE6');
                    break;
                case 'version'://版本查看
                    var btnFun = " onclick=\"$m('" + curElManageUrl + "').btnVersionElement('" + row.elementId + "');\"";
                    actionHtml += creatBtnLink(btnFun, '版本查看', ' #2E8AE6');
                    break;
                case 'del'://删除
                    var btnFun = " onclick=\"$m('" + curElManageUrl + "').btnDelElement('" + row.elementId + "');\"";
                    actionHtml += creatBtnLink(btnFun, '删除', ' #2E8AE6');
                    break;
                case 'reEdit'://修订
                    var btnFun = " onclick=\"$m('" + curElManageUrl + "').btnReEditElement('" + row.versionId + "');\"";
                    actionHtml += creatBtnLink(btnFun, '修订', ' #2E8AE6');
                    break;
                case 'standardDown'://标准下发
                    var btnFun = " onclick=\"$m('" + curElManageUrl + "').btnStandardDownElement('" + row.elementId + "');\"";
                    actionHtml += creatBtnLink(btnFun, '标准下发', ' #2E8AE6');
                    break;
                case 'stop'://废止
                    var btnFun = " onclick=\"$m('" + curElManageUrl + "').btnStopElement('" + row.elementId + "');\"";
                    actionHtml += creatBtnLink(btnFun, '废止', ' #2E8AE6');
                    break;
                default:
                    break;
            }
        }
        return actionHtml;
    };

    //列表-编辑
    this.btnEditElement = function (versionId) {
        $m('dam/metamanage/standard/dataelement/edit').editElement(versionId);
    };

    //列表-版本查看
    this.btnVersionElement = function (elementId) {
        $m(versionManageUrl).initVersionManage(elementId);
    };

    //列表-修订
    this.btnReEditElement = function (versionId) {
        $m('dam/metamanage/standard/dataelement/edit').reEditElement(versionId);
    };

    //列表-标准下发
    this.btnStandardDownElement = function (elementId) {
        $m(standardDownUrl).initStandardDown(elementId);
    };

    //列表-删除
    this.btnDelElement = function (elementId) {
        Api.deleteElements([elementId], function (result) {
            if (result) {
                top$.messager.promptInfo({
                    msg: '删除成功',
                    icon: 'success',
                });
                reloadGridData();
            }
        });
    };

    //列表-废止
    this.btnStopElement = function (elementId) {
        $.Msg.confirm('提示', '数据元标准废止后不可再用，请确认！', function (flag) {
            if (flag) {
                Api.deprecatedElement(elementId, function (result) {
                    if (result) {
                        top$.messager.promptInfo({
                            msg: '废止成功',
                            icon: 'success',
                        });
                        reloadGridData();
                    }
                });
            }
        });
    };

    var creatBtnLink = this.creatBtnLink = function (fun, label, color) {
        return "<a href=\"javascript:void(0)\" data-roles=\"mui-linkbutton\" title=\"" + label + "\"  " +
            "class=\"l-btn l-btn-medium l-btn-normal\" " + fun + " >" +
            "        <span class=\"l-btn-left\">\n" +
            "            <span class=\"l-btn-text\">" + label + "</span>\n" +
            "        </span>\n" +
            "</a>";
    };

    /**
     *|           | 状态   | 支持操作                       |
     *| --------- | ------ | ------------------------------ |
     *| edit      | 编辑中 | 编辑、版本查看、删除、送审     |
     *| reject    | 已驳回 | 编辑、版本查看、删除、送审     |
     *| review    | 审核中 | 版本查看                       |
     *| published | 已发布 | 修订、版本查看、标准下发、废止 |
     *| abolished | 已废止 | 版本查看                       |
     *| history   | 历史   |                                |
     *
     */
    var statusConfig = {
        edit: ['edit', 'version', 'del', 'task'],
        reject: ['edit', 'version', 'del', 'task'],
        review: ['version'],
        published: ['stop', 'reEdit', 'version', 'standardDown'],
        abolished: ['version'],
        history: ['']
    };

    function getActionBtnConfig() {
        var rows = elementGrid.datagrid('getRows') || [];
        var actionConfig = {
            edit: 0,//编辑
            version: 0,//版本查看
            del: 0,//删除
            task: 0,//送审
            stop: 0,//废止
            reEdit: 0,//修订
            standardDown: 0//标准下发
        };
        for (var idx = 0; idx < rows.length; idx++) {
            var item = rows[idx] || {};
            var versionStatus = item.versionStatus;
            var actionBtns = statusConfig[versionStatus] || [];
            for (var iBtn = 0; iBtn < actionBtns.length; iBtn++) {
                var actionBtn = actionBtns[iBtn];
                var btnCount = actionConfig[actionBtn] || 0;
                btnCount++;
                actionConfig[actionBtn] = btnCount;
            }
        }
        return actionConfig;
    }

    function sortBtnList(config, btns) {
        config = config || {};
        btns = btns || [];
        var btnArr = [];
        for (var key in config) {
            if (btns.indexOf(key) > -1) {
                btnArr.push({
                    key: key,
                    value: config[key]
                });
            }
        }
        var arr = querySortBtn(btnArr);
        arr = arr || [];
        var sortBtnArr = [];
        for (var idx = 0; idx < arr.length; idx++) {
            sortBtnArr.push(arr[idx].key)
        }
        return sortBtnArr;
    }

    function querySortBtn(arr) {
        arr = arr || [];
        if (arr.length <= 1) return arr;
        var pivotIdx = Math.floor(arr.length / 2);
        var pivot = arr.splice(pivotIdx, 1)[0];
        var pivotKey = pivot.key;
        var pivotValue = pivot.value;
        var left = [];
        var right = [];
        for (var idx = 0; idx < arr.length; idx++) {
            var item = arr[idx];
            var itemKey = item.key;
            var itemValue = item.value;
            if (itemValue > pivotValue) {
                left.push(item);
            } else if (itemValue === pivotValue) {
                if (itemKey > pivotKey) {
                    left.push(item);
                } else {
                    right.push(item);
                }
            } else {
                right.push(item);
            }
        }
        return querySortBtn(left).concat([pivot], querySortBtn(right));
    }

    /**
     * 根据key获取列表值
     * @param key key属性
     * @param selectRows 当前选择的行
     * @returns {[]}
     */
    function getSelectRowValues(key, selectRows) {
        selectRows = selectRows || [];
        var ids = [];
        for (var i = 0; i < selectRows.length; i++) {
            var row = selectRows[i];
            ids.push(row[key]);
        }
        return ids;
    }

    function reloadGridData() {
        $m(elementScriptUrl).reloadGrid();
    }
});
