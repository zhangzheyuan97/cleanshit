$m('dam/plan/acquistion/maintain', function () {


    //=======相关访问路径声明=======start=========
    var modulePath = routerPath +'/api/datapacket/maintain';
    var dataAcqusitionListURL='/dam-datapacket/api/datapacket/maintain/'
    //=======全局变量信息=======start=========
    var XHNodeID;  //用于点击中间列表查询右侧表头信息
    $(function () {
        // debugger
        $("#maintain_center_list").parent().css("margin-top","-8px")

        // $('.pagination-page-list').css("visibility","hidden");
    });

    /**
     * 选中模型树节点事件
     * @param node
     */
    var nodeInfo;
    this.selectTree = function (node) {
        debugger
        if(node.attributes.tempID!=undefined){
            XHNodeID=node.attributes.tempID;
        }
        nodeInfo=node;
        var id=node.id;
        var text=node.text;
        var maintainModuleName= $('#maintainModuleName').val();
        var maintainGrid= $('#dam_maintain_center_version_grid');
        var attributes=node.attributes;
        nodeId = node.id;
        maintainGrid.datagrid({
            url:dataAcqusitionListURL+'centerPage',
            queryParams: {'nodeId': id,"text":text,"name":maintainModuleName,"attributes":JSON.stringify(attributes)}
        });
    };
    /**
     * 双击模型树节点，触发事件
     * */
    this.dbClickTree = function dbClickTree(node) {
        var state = node.state;
        if (state == 'closed') {
            $('#dam_datapacket_maintain_tree').tree('expand', node.target);
        } else {
            $('#dam_datapacket_maintain_tree').tree('collapse', node.target);
        }
    };

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

    /**
     * 查询按钮点击事件
     */
    var modelQuery= this.modelQuery = function () {
        var node = $("#dam_datapacket_maintain_tree").tree('getSelected');
        var id=node.id;
        var text=node.text;
        var maintainModuleName= $('#maintainModuleName').val();
        var maintainGrid= $('#dam_maintain_center_version_grid');
        var attributes=node.attributes;
        nodeId = node.id;
        maintainGrid.datagrid({
            url:dataAcqusitionListURL+'centerPage',
            queryParams: {'nodeId': id,"text":text,"name":maintainModuleName,"attributes":JSON.stringify(attributes)}
        });
    };

    /**
     * 重置点击事件
     */
    this.resetBtn = function () {
        $('#maintainModuleName').val('');
        this.modelQuery();
    };

    /**
     * 表格点击事件
     */
    this.onSelectMaintainData = function (rowIndex, rowData) {
        debugger
    }

});



