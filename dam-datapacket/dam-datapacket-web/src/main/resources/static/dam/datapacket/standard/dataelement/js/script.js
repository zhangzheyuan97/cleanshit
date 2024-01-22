$m('dam/metamanage/standard/dataelement', function () {

    //=======对象引用声明=======start=========
    var elementListId = 'dam_matemanage_data_standard_element_grid';
    var elementManageUrl = 'dam/metamanage/standard/dataelement/list';
    var selectNode;
    //=======对象引用声明=======end=========

    $(function () {
    });

    this.selectTree = function (node) {
        selectNode = node;
        getElementGridData(node.id, $("#keyword").val());
    };

    this.treeLoadSuccess = function (node, data) {
        var treeDom = $('#dam_matemanage_data_standard_type_tree');
        if (selectNode && StandardUtils.isNotEmpty(selectNode.id)) {
            var findNode = treeDom.tree('find', selectNode.id);
            if (findNode) {
                treeDom.tree('select', findNode.target);
            } else {
                var root = treeDom.tree('getRoot');
                treeDom.tree('select', root.target);
            }
        } else {
            var root = treeDom.tree('getRoot');
            treeDom.tree('select', root.target);
        }
    };

    //检索
    this.search = function () {
        getElementGridData(selectNode.id, $("#keyword").val());
    };

    function getElementGridData(typeId, keyword) {
        var status = $('#status').combobox("getValue");
        Api.getElements(elementListId, {typeId: typeId, keyword: keyword, status: status}, {
            onSelect: function (rowIndex, rowData) {

            },
            onLoadSuccess: function (data) {
                $m(elementManageUrl).loadElementPageSuccess(data);
            }
        });
    }

    //检索
    var reloadGrid = this.reloadGrid = function () {
        var typeId = selectNode ? selectNode.id : '';
        getElementGridData(typeId, $("#keyword").val());
    };

});
