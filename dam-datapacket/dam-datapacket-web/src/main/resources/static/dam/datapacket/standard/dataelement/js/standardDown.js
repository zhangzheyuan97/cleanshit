$m('dam/metamanage/standard/dataelement/standardDown', function () {

    var standardDownView = moduleResource.element.getView('standardDown');
    var elementLManageUrl = 'dam/metamanage/standard/dataelement/list';
    var curSDManageUrl = 'dam/metamanage/standard/dataelement/standardDown';
    var sdElementGridId = 'dam_matemanage_data_standard_el_sd_grid';
    var standardDownElementId;
    var standardDownDialog;
    var newRowObj = undefined;
    var databaseNameTarget;
    var modelNameTarget;
    var filedNameTarget;
    var historyFieldIds = [];
    var databaseData = [];
    var modelData = [];
    var filedData = [];
    var selectedDatabaseData = undefined;
    var selectedModelData = undefined;
    var selectedFiledData = undefined;
    $(function () {
    });

    this.initStandardDown = function (elementId) {
        newRowObj = undefined;
        standardDownElementId = elementId;
        standardDownDialog = $.topDialog({
            title: '标准下发',
            href: standardDownView,
            width: 800,
            height: 600,
            resizable: false,
            onLoad: function () {
                loadSDGrid();
                standardDownDialog.find("#dam_matemanage_data_standard_el_sd_gridToolbar").on('click', function (ev) {
                    closeSelsdPanel();
                });
                standardDownDialog.find(".datagrid-view").on('click', function (ev) {
                    closeSelsdPanel();
                });
            }
        });
    };

    function loadSDGrid() {
        openProgresss();
        historyFieldIds = [];
        Api.getElStandard(standardDownElementId, function (result) {
            closeProgresss();
            result = result || [];
            for (var i = 0; i < result.length; i++) {
                historyFieldIds.push(result[i].filedId);
            }
            standardDownDialog.find('#' + sdElementGridId).datagrid('loadData', result);
        });
    }
    /**
     * 关闭进度条
     */
    var closeProgresss = function () {
        try {
            top$.messager.progress('close');
        } catch (e) {
            $.messager.progress('close');
        }
    };
    /**
     * 打开进度条
     */
    var openProgresss = function () {
        try {
            top$.messager.progress({
                text: '正在加载，请稍候....',
                interval: 500,
                width: 500
            });
        } catch (e) {
            $.messager.progress({
                text: '正在加载，请稍候....',
                interval: 500,
                width: 500
            });
        }
    };

    this.sdGridSelected = function (rowIndex, rowData) {
        closeSelsdPanel();
    };

    this.addStandardDownLine = function () {
        var grid = standardDownDialog.find('#' + sdElementGridId);
        grid.datagrid({
            rowStyler: function(index,row){
                return 'background: oldlace;';
            }
        });
        newRowObj = {
            databaseName: '',
            modelName: '',
            filedName: ''
        };
        grid.datagrid('appendRow', newRowObj);
        var idx = grid.datagrid('getRows').length;
        var curIdx = idx - 1;
        grid.datagrid('beginEdit', curIdx);
        standardDownDialog.find('#el_sd_re_add_btn').linkbutton('disable');
        grid.datagrid('beginEdit', curIdx);
        getCurEditor(grid, curIdx);
        loadDatabaseTree();
    };

    function getCurEditor(grid, curIdx) {
        var editors = grid.datagrid('getEditors', curIdx);
        for (var i = 0; i < editors.length; i++) {
            var editor = editors[i];
            var fieldLabel = editor.field;
            if (StandardUtils.equals(fieldLabel, 'databaseName')) {
                databaseNameTarget = editor.target;
            }
            if (StandardUtils.equals(fieldLabel, 'modelName')) {
                modelNameTarget = editor.target;
            }
            if (StandardUtils.equals(fieldLabel, 'filedName')) {
                filedNameTarget = editor.target;
            }
        }
    }

    var panelShow = false;
    this.confHidePanel = function () {
        if (panelShow) {
            panelShow = false;
            configComboPanel(true);
        }
    };

    this.confShowPanel = function () {
        if (!panelShow) {
            panelShow = true;
            configComboPanel(true);
            openSelsdPanel();
        }
    };

    function closeSelsdPanel() {
        standardDownDialog.find("#dam_matemanage_data_selsd_panel").hide();
    }

    function openSelsdPanel() {
        var grid = standardDownDialog.find('#' + sdElementGridId);
        var gridParent = grid.parent();
        var idx = grid.datagrid('getRows').length - 1;
        var top = gridParent.find('tr[datagrid-row-index="' + idx + '"]')[0].offsetTop;
        var scrollTop = gridParent.find('.datagrid-body')[0].scrollTop;
        top = top - scrollTop;
        top = 480 - top > 280 ? top : top - 36 - 280;
        standardDownDialog.find("#dam_matemanage_data_selsd_panel").show();
        standardDownDialog.find("#dam_matemanage_data_selsd_panel").css({
            'top': top + 200 + 'px',
        });
    }

    function configComboPanel(hidden) {
        databaseNameTarget.combobox(hidden ? 'hidePanel' : 'showPanel');
        modelNameTarget.combobox(hidden ? 'hidePanel' : 'showPanel');
        filedNameTarget.combobox(hidden ? 'hidePanel' : 'showPanel');
    }

    this.fmtActionBtn = function (value, row, index) {
        var reId = row.id;
        if (StandardUtils.isNotEmpty(reId)) {
            var detailFun = " onclick=\"$m('" + curSDManageUrl + "').reDelRow(this,'" + reId + "');\" row-action-type='del'";
            return $m(elementLManageUrl).creatBtnLink(detailFun, '删除', '#2E8AE6');
        } else {
            var actionHtml = '';
            var detailFun = " onclick=\"$m('" + curSDManageUrl + "').reCancelEditRow(this);\" ";
            actionHtml += $m(elementLManageUrl).creatBtnLink(detailFun, '取消', '#F09D3F');
            var detailFun = " onclick=\"$m('" + curSDManageUrl + "').reSaveRow(this);\" ";
            actionHtml += $m(elementLManageUrl).creatBtnLink(detailFun, '保存', '#2E8AE6');
            return actionHtml;
        }
    };

    this.reSaveRow = function (e) {
        if (!selectedDatabaseData) {
            top$.messager.promptInfo({
                msg: '请选择数据源!',
                icon: 'warning',
            });
            return;
        }
        if (!selectedModelData) {
            top$.messager.promptInfo({
                msg: '请选择数据模型!',
                icon: 'warning',
            });
            return;
        }
        if (!selectedFiledData) {
            top$.messager.promptInfo({
                msg: '请选择数据属性!',
                icon: 'warning',
            });
            return;
        }
        var reObj = {
            elementId: standardDownElementId,
            databaseId: selectedDatabaseData.id,
            modelId: selectedModelData.id,
            filedId: selectedFiledData.id
        };
        Api.saveElStandard(reObj, function (result) {
            newRowObj = undefined;
            standardDownDialog.find('#el_sd_re_add_btn').linkbutton('enable');
            loadSDGrid();
            top$.messager.promptInfo({
                msg: '下发成功!',
                icon: 'success',
            });
        });
    };

    this.reCancelEditRow = function (e) {
        newRowObj = undefined;
        standardDownDialog.find('#el_sd_re_add_btn').linkbutton('enable');
        var grid = standardDownDialog.find('#' + sdElementGridId);
        grid.datagrid('deleteRow', grid.datagrid('getRows').length - 1);
        //delLinkOpt(false);
    };

    this.fmtDatabaseName = function (value, row, index) {
        return fmtStatusLabel(value, row.databaseStatus);
    };

    this.fmtModelName = function (value, row, index) {
        return fmtStatusLabel(value, row.modelStatus);
    };

    this.fmtFiledName = function (value, row, index) {
        return fmtStatusLabel(value, row.filedStatus);
    };

    this.reDelRow = function (e, reId) {
        Api.delElStandard([reId], function (result) {
            if (result) {
                top$.messager.promptInfo({
                    msg: '删除成功',
                    icon: 'success',
                });
                loadSDGrid();
            }
        });
    };

    function fmtStatusLabel(value, status) {
        var suffix = StandardUtils.equals(status, 1) ? '' : '<span style="color: red;">（停用）</span>';
        return value + suffix;
    }


    /**
     * 控制编辑按钮是否展示
     * @param hidden
     */
    function delLinkOpt(hidden) {
        var editedBtns = standardDownDialog.find('a[row-action-type="del"]') || [];
        for (var idx = 0; idx < editedBtns.length; idx++) {
            $(editedBtns[idx]).css("display", hidden ? 'none' : '');
        }
    }

    /**
     * 选择属性数据级联-start
     */

    function loadDatabaseTree() {
        clearDatabaseData();
        clearModelData();
        clearFieldData();
        Api.getEffectDatabase(function (result) {
            databaseData = result;
            loadTreeData('dam_m_selsd_panel_database_tree', result, 'id', 'name');
        });
    }

    this.selectDatabaseTree = function (node) {
        selectedDatabaseData = node;
        databaseNameTarget.combobox('setValue', node.text);
        clearModelData();
        clearFieldData();
        Api.getEffectModel(node.id, function (result) {
            modelData = result;
            loadTreeData('dam_m_selsd_panel_model_tree', result, 'modelInfo', 'name');
        });
    };

    this.selectModelTree = function (node) {
        modelNameTarget.combobox('setValue', node.text);
        selectedModelData = node;
        clearFieldData();
        Api.getEffectField(node.id, function (result) {
            filedData = result;
            loadTreeData('dam_m_selsd_panel_filed_tree', result, 'modelFieldId', 'busiName');
        });
    };

    this.selectFieldTree = function (node) {
        filedNameTarget.combobox('setValue', node.text);
        selectedFiledData = node;
    };

    this.searchDatabaseData = function (value) {
        var tree = standardDownDialog.find('#dam_m_selsd_panel_database_tree');
        treeNodeSearch(tree, value)
    };

    this.searchModelData = function (value) {
        var tree = standardDownDialog.find('#dam_m_selsd_panel_model_tree');
        treeNodeSearch(tree, value)
    };

    this.searchFieldData = function (value) {
        var tree = standardDownDialog.find('#dam_m_selsd_panel_filed_tree');
        treeNodeSearch(tree, value)
    };

    function treeNodeSearch(treeDom, keyword) {
        var nodes = treeDom.tree('getRoots') || [];
        if (StandardUtils.isBlank(keyword)) {
            for (var idx = 0; idx < nodes.length; idx++) {
                var node = nodes[idx];
                $( node.target).show();
                // node.target.hidden = false;
            }
            return;
        }
        // 获取查询结果
        for (var idx = 0; idx < nodes.length; idx++) {
            var node = nodes[idx];
            // node.target.hidden = false;
            $( node.target).show();
            var nodeName = node.text;
            if (nodeName.indexOf(keyword) == -1) {
                // node.target.hidden = true;
                $( node.target).hide();
            }
        }
    }

    function clearDatabaseData() {
        databaseNameTarget.combobox('setValue', '');
        databaseData = [];
        selectedDatabaseData = undefined;
        loadTreeData('dam_m_selsd_panel_database_tree', databaseData, 'id', 'name');
    }

    function clearModelData() {
        modelNameTarget.combobox('setValue', '');
        modelData = [];
        selectedModelData = undefined;
        loadTreeData('dam_m_selsd_panel_model_tree', modelData, 'modelInfo', 'name');

    }

    function clearFieldData() {
        filedNameTarget.combobox('setValue', '');
        filedData = [];
        selectedFiledData = undefined;
        loadTreeData('dam_m_selsd_panel_filed_tree', filedData, 'modelFieldId', 'busiName');
    }

    function loadTreeData(treeId, treeData, idKey, textKey) {
        treeData = treeData || [];
        var children = [];
        if (StandardUtils.listIsNotEmpty(treeData)) {
            for (var idx = 0; idx < treeData.length; idx++) {
                var item = treeData[idx];
                var id = item[idKey];
                //var filedTreeFlag = StandardUtils.equals(treeId, 'dam_m_selsd_panel_filed_tree');
                //if (!filedTreeFlag || historyFieldIds.indexOf(id) === -1) {
                var text = item[textKey];
                children.push({
                    id: id,
                    text: text,
                    attributes: item
                });
                //}
            }
        }
        var culUlTree = standardDownDialog.find('#' + treeId);
        culUlTree.tree('loadData', children);
        setTitleToLi(culUlTree);
    }

    function setTitleToLi(dom) {
        var lis = dom.find(".tree-title");
        if (StandardUtils.listIsNotEmpty(lis)) {
            for (var idx = 0; idx < lis.length; idx++) {
                var li = lis[idx];
                var text = $(li).text();
                $(li).attr('title', text);
            }
        }
    }

    /**
     * 选择属性数据级联-end
     */
});
