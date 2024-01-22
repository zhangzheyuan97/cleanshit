$m('dam/metamanage/standard/integratedquery/show', function () {

    var showInfoDialog;
    var elementView = moduleResource.query.getView('element');
    var enumView = moduleResource.query.getView('enum');
    var fileView = moduleResource.query.getView('file');
    var $this = $m('dam/metamanage/standard/integratedquery/show');
    $(function () {

    });

    /**
     * 查看
     */
    this.showInfo = function (type, standardId) {
        var showInfoView = getShowInfoUrl(type);
        var dialog = dialogInfo(type);
        if (StandardUtils.isNotEmpty(showInfoView)) {
            showInfoDialog = $.topDialog({
                title: '标准详情',
                href: showInfoView,
                resizable: false,
                width: dialog['width'],
                height: dialog['height'],
                onLoad: function () {
                    switch (type) {
                        case 'ELEMENT' :
                            Api.getElementVersionDetail(standardId,function (result) {
                                dealResultData(result);
                                var form = showInfoDialog.find('#data_element');
                                loadElementData(form, result);
                                showDataTypeView(result, form);
                            });
                            break;
                        case 'ENUM' :
                            Api.getTypeOne(standardId,function (result) {
                                loadEnumData(result)
                            });
                            break;
                        case 'FILE' :
                            Api.getFileById(standardId, function (result) {
                                loadFileData(result);
                            });
                            break;
                        default:
                            break;
                    }

                },
                buttons: [
                    {
                        text: '关闭',
                        handler: function () {
                            $.topDialog('close', showInfoDialog);
                        }
                    },
                ]
            });
        }
    };

    function dialogInfo(type) {
        var dialog = {};
        switch (type) {
            case 'ELEMENT' :
                dialog = {width : 800, height :700};
                break;
            case 'ENUM' :
                dialog = {width : 800, height :800};
                break;
            case 'FILE' :
                dialog = {width : 800, height :500};
                break;
            default:
                break;
        }
        return dialog;
    }

    function loadFileData(record) {
        var form = showInfoDialog.find('#file_data');
        form.find('#code').text(record.code);
        form.find('#name').text(record.name + "." + record.extension);
        form.find('#remark').text(record.remark || '');
        form.find('#uploadTime').text(StandardUtils.timeForMatter(record.uploadTime));
        form.find('#fileId').val(record.id);
    }

    var list_columns = [[
        {field: 'code', title: '枚举值编码', width: 100, formatter: function (value, row, index) {
        return StandardUtils.formatter(value);
    }},
        {field: 'name', title: '枚举值名称', width: 100, formatter: function (value, row, index) {
                return StandardUtils.formatter(value);
            }},
        {field: 'explain', title: '枚举值说明', width: 100, formatter: function (value, row, index) {
                return StandardUtils.formatter(value);
            }}
    ]];
    var tree_columns = [[
        {field: 'code', title: '枚举值编码', width: 100, formatter: function (value, row, index) {
                return StandardUtils.formatter(value);
            }},
        {field: 'name', title: '枚举值名称', width: 100, formatter: function (value, row, index) {
                return StandardUtils.formatter(value);
            }},
        {field: 'explain', title: '枚举值说明', width: 100, formatter: function (value, row, index) {
                return StandardUtils.formatter(value);
            }},
        {field: 'parentName', title: '所属上级', width: 90, formatter: function (value, row, index) {
                return StandardUtils.formatter(value);
            }}
    ]];
    /**
     * 加载枚举项数据
     * @param result
     */
    function loadEnumData(record) {
        var form = showInfoDialog.find('#data_enum');
        form.find('#enumId').val(record.enumId);
        form.find('#code').text(record.code);
        form.find('#name').text(record.name);
        var structureName = record.structure ? '树结构' : '列表结构';
        form.find('#structure').text(structureName);
        form.find('#remark').append(formatCellTooltip(record.remark));
        showEnumItemGrid(record.structure);
        Api.getItemTree(record.enumId, function (result) {
            var enumConf = result.enumConf || {};
            var structure = enumConf.structure;
            var parentId = '';
            if (enumConf.structure) {
                var tree = showInfoDialog.find('#dam_metamanage_standard_enum_item_tree');
                var treeData = result.treeData || {};
                tree.tree('loadData', [treeData]);
                if (parentId) {
                    // 查找一个节点然后返回它
                    var node = tree.tree('find', parentId);
                    if (node) {
                        tree.tree('select', node.target);
                        getItemPage(record.enumId, structure, parentId);
                    } else {
                        getItemPage(record.enumId, structure, parentId);
                    }
                }
            } else {
                getItemPage(record.enumId, structure, parentId);
            }
        });
        // 关联数据字典查询
        Api.getDicBindRe(record.enumId, function (resultDic) {
            var historyRes = resultDic.historyRes || [];
            var refDiv = showInfoDialog.find('#ref_dic_info');
            if (StandardUtils.listIsNotEmpty(historyRes)) {
                var html = '<div class="s-item-banner-info">';
                for (var i = 0; i < historyRes.length; i++) {
                    html += '<span class="s-i-tag-info">' + historyRes[i].dicName + '</span>';
                }
                html += '</div>';
            }
            refDiv.html(html);
        })
    }
    /**
     * 鼠标悬停显示事件
     */
    var formatCellTooltip = function (value) {
        value = value ? value : '';
        var valTitle = value;
        if (StandardUtils.isNotEmpty(valTitle)) {
            valTitle = valTitle.replace(/\</g, "&lt;").replace(/\>/g, "&gt;");
            valTitle = valTitle.replace(/\"/g, "&quot;");
        }
        return '<span style="word-break: break-word;" title="' + valTitle + '">' + value + '</span>';
    };


    /**
     * 枚举列表的树名称模糊匹配
     * @param value
     */
    this.searchModel = function (value) {
        var tree = showInfoDialog.find('#dam_metamanage_standard_enum_item_tree');
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
                var enumId = child.id;
                var enumName = child.text;
                // 匹配模型信息
                if (enumName.indexOf(value) != -1) {
                    pareTotal = pareTotal + 1;
                    pareMap[enumId] = pareTotal;
                    countMap[pareTotal] = enumId;
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
                    if (selectedIndex < pareTotal) {
                        nextId = countMap[selectedIndex + 1];
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
        }
    };

    /**
     * 枚举树加载成功事件
     */
    this.enumItemTreeLoadSuccess = function () {
        var tree = showInfoDialog.find('#dam_metamanage_standard_enum_item_tree');
        var root = tree.tree('getRoot');
        tree.tree('select', root.target);
    };

    /**
     * 单文件下载
     * @param record
     * @param index
     */
    this.importFile = function () {
        var fileId = showInfoDialog.find('#fileId').val();
        var form = $('<form>').attr('method', 'post').hide();
        $('body').append(form);
        Api.downloadFile(form, fileId, function (result) {
            form.delete();
        });
    };

    /**
     * 枚举树点击事件
     * @param node
     */
    this.enumItemTreeSelected = function (node) {
        var enumId = showInfoDialog.find('#enumId').val();
        var parentId = node.id;
        getItemPage(enumId, true, parentId);
    };
    /**
     * 枚举值列表
     */
    function getItemPage(enumId, structure, parentId) {
        var pageSize = null;
        Api.getItemCount(enumId, parentId, function (result) {
           pageSize = result;
            var params = {
                enumId: enumId,
                pageSize: pageSize,
                parentId: parentId
            };
            var tempTable = showInfoDialog.find('#all_in_one_grid');
            Api.getItemPage(tempTable, params, {
                rownumbers: true,
                fitColumns: true,
                singleSelect: false,
                pagination: false,
                paginationType:'advanced',
                fit: true,
                border: false,
                columns: structure ? tree_columns : list_columns,
                onLoadSuccess: function () {
                }
            });
        });
    }
    /**
     * 处理当前展示的枚举值区域的结构
     * @param treeFlag structure
     */
    function showEnumItemGrid(treeFlag) {
        var enumLayout = showInfoDialog.find('#enum_data_div');
        if (!treeFlag) {
            enumLayout.layout('remove', 'west');
        }
    }
    /**
     * 加载数据元标准
     * @param form
     * @param result
     */
    function loadElementData(form, result) {
        form.find('#code').text(result.code);
        form.find('#elementTypeName').text(result.elementTypeName);
        form.find('#buisName').text(result.buisName);
        form.find('#publishName').text(result.publishName);
        form.find('#buisRemark').append(formatCellTooltip(result.buisRemark));
        form.find('#configEnum').text(result.configEnum);
        form.find('#configEmpty').text(result.configEmpty);
        form.find('#configUnique').text(result.configUnique);
        form.find('#configRemark').append(formatCellTooltip(result.configRemark));
        var refDiv = form.find('#ref_model_info');
        var html = '<div class="s-item-banner-info">';
        Api.getElStandard(result.elementId, function (result2) {
            if (StandardUtils.listIsNotEmpty(result2)) {
                for (var i = 0; i < result2.length; i++) {
                    html += '<span class="s-i-tag-info">' + result2[i].filedName + '</span>';
                }
                html += '</div>';
            }
            refDiv.html(html);
        });
    }



    function dealResultData(result) {
        result = result || {};
        var confEmptyVal = result['configEmpty'];
        result['configEmpty'] = (confEmptyVal || StandardUtils.equals(confEmptyVal, 1)) ? '是' : '否';
        var confUniqueVal = result['configUnique'];
        result['configUnique'] = (confUniqueVal || StandardUtils.equals(confUniqueVal, 1)) ? '是' : '否';
        var configEnumId = result['configEnumId'];
        result['configEnum'] = StandardUtils.isNotEmpty(configEnumId) ? '是' : '否';
        return result;
    }

    /**
     * 版本查看是否匹配字段事件
     * */
    var showDataTypeView = this.showDataTypeView = function (result, form) {
        var dataType = result.dataType;
        switch (dataType) {
            case 'STRING' :
                form.find('#textLengths').show();
                form.find('#dataFromats').hide();
                form.find('#dataLengths').hide();
                form.find('#dataPrecisions').hide();
                form.find('#dataType').html('文本类');
                form.find('#textLength').html(result.dataLength);
                break;
            case 'DATE' :
                form.find('#dataFromats').show();
                form.find('#textLengths').hide();
                form.find('#dataLengths').hide();
                form.find('#dataPrecisions').hide();
                form.find('#dataType').html('日期类');
                form.find('#dateFormat').html(result.dataFormat);
                break;
            case 'NUMBER' :
                form.find('#dataLengths').show();
                form.find('#textLengths').hide();
                form.find('#dataFromats').hide();
                form.find('#dataPrecisions').show();
                form.find('#dataType').html('数值类');
                form.find('#dataLength').html(result.dataLength);
                form.find('#dataPrecision').html(result.dataPrecision);
                break;
        }
    };

    /**
     * 标准详情查看路由
     */
    function getShowInfoUrl(type) {
        var url = '';
        if (StandardUtils.isNotEmpty(type)) {
            switch (type) {
                case 'ELEMENT' :
                    url =  elementView;
                    break;
                case 'ENUM' :
                    url = enumView;
                    break;
                case 'FILE' :
                    url = fileView;
                    break;
                default:
                    break;
            }
        }
        return url;
    }
});
