$m('dam/plan/acquistion/dataAcquistion/standAlone', function () {

    var nodeId;
    var standGrid;

    $(function () {
        debugger
        // $('.pagination-page-list').css("visibility","hidden");
        standGrid = $("#dam_datapacket_stand_alone_grid");
    });


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
        // $(this).tree("collapseAll");
    };

    /**
     * 查询按钮点击事件
     */
    this.centerListQuery = function () {
        debugger
        var form = $('#centerAloneList_queryform');
        var node = $("#dam_datapacket_stand_alone_tree").tree('getSelected');
        if (node == undefined) {
            form.find("#nodeId").val('-1');
        } else {
            form.find("#nodeId").val(node.id);
        }
        $('#dam_datapacket_stand_alone_grid').datagrid("reload", form.form('getRecord'));
    };

    /**
     * 查询列表获取树节点id
     */
    this.getQueryParam = function () {
        return {nodeId: nodeId};
    };

    /**
     * 双击模型树节点，触发事件
     * */
    this.dbClickTree = function dbClickTree(node) {
        var state = node.state;
        if (state == 'closed') {
            $('#dam_datapacket_stand_alone_tree').tree('expand', node.target);
        } else {
            $('#dam_datapacket_stand_alone_tree').tree('collapse', node.target);
        }
    };
    this.selectTree = function (node) {
        debugger
        if(node.attributes){
            nodeId = node.attributes.tempID;
            standGrid.datagrid({
                url:'/dam-datapacket/api/datapacket/alone/page',
                queryParams: {'nodeId': nodeId, 'page': 1, 'rows': 10,'name':'',nodeLevel:"4"}
            });
        }
    };

    // this.beforeExpand = function (node) {
    //     debugger
    //     return false;
    // }
});



