$m('dam/metamanage/standard/dataelement/type', function () {

    //=======对象引用声明=======start=========
    var typeTreeContainerId = 'data_element_type_tree_container';
    var typeTreeId = 'dam_matemanage_data_standard_type_tree';
    //=======对象引用声明=======end=========
    $(function () {
        initTypeTree();
        $('#'+typeTreeId).css({
            'height':'95%',
            'overflow-y':'auto'
        })
    });

    function initTypeTree() {
        Api.getTypes(true, function (result) {
            result = result || [];
            if (StandardUtils.listIsEmpty(result)) {
                $('#' + typeTreeId).tree('loadData', [{
                    id: '',
                    text: '数据元分类',
                    children: []
                }]);
                hiddenLeftDiv();
            } else {
                showLeftDiv();
                $('#' + typeTreeContainerId).css('display', 'block');
                var treeGridData = [];
                for (var idx = 0; idx < result.length; idx++) {
                    var typeItem = result[idx];
                    treeGridData.push({
                        id: typeItem.elementTypeId,
                        text: typeItem.name,
                        attributes: typeItem
                    });
                }
                $('#' + typeTreeId).tree('loadData', [{
                    id: '',
                    text: '数据元分类',
                    children: treeGridData
                }]);
            }
        })
    }

    this.reloadTypeTree = function () {
        initTypeTree();
    };

    function showLeftDiv() {
        $('#data_element_type_tree_container').css('width', '240px');
        $('#standard_data_el_manage .layout-button-left').parent().parent().parent().css('display', '');
        $('#standard_data_el_manage .layout-button-right').parent().parent().css('display', '');
        $('#standard_data_el_manage .layout-button-right').parent().parent().next().css('display', '');
        $('#standard_data_el_manage .layout-button-right').click();
    }

    function hiddenLeftDiv() {
        $('#standard_data_el_manage .layout-button-left').click();
        $('#data_element_type_tree_container').css('width', '0px');
        $('#standard_data_el_manage .layout-button-left').parent().parent().parent().css('display', 'none');
        $('#standard_data_el_manage .layout-button-right').parent().parent().css('display', 'none');
        $('#standard_data_el_manage .layout-button-right').parent().parent().next().css('display', 'none');
        $('#standard_data_el_manage .layout_center_left').css('padding-left', '0px');
        $('#standard_data_el_manage .layout-panel-center').css({
            width: $(window).width() + 16,
            left: -13
        });
        $('#standard_data_el_manage .panel-body').css({
            width: $(window).width() + 16
        });
        $('#standard_data_el_manage .panel-body').css('padding-left', '8px');
        $('#standard_data_el_manage .layout').css({
            width: $(window).width() + 16,
        });
        $('#standard_data_el_manage .panel .datagrid').css({
            width: $(window).width() + 16,
        });
    }


});
