$m('dam/plan/acquistion/show', function () {


    //规避右侧列表未加载，左侧树查询问题
    var treeFlag =false;
    var dataManageType;
    //=======相关访问路径声明=======start=========
    var modulePath = routerPath +'/api/datapacket/show';
    var exhibitionListURL='/dam-datapacket/api/datapacket/show/exhibitionList'
    //四个节点对应的信息
    var firstNode="";
    var secondNode="";
    var thirdlyNode="";
    var fourthlyNode="";
    /**
     * 设置当前模块为数据维护或数据初始化
     * @param data
     */
    this.setDataManageType = function (data) {
        dataManageType = data;
    };

    /**
     * 双击模型树节点，触发事件
     * */
    this.dbClickTree = function dbClickTree(node) {
        var state = node.state;
        if (state == 'closed') {
            $('#dam_datapacket_model_tree').tree('expand', node.target);
        } else {
            $('#dam_datapacket_model_tree').tree('collapse', node.target);
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
     * 选中模型树节点事件
     * @param node
     */
    var nodeInfo;
    this.selectTree = function (node) {
        nodeInfo=node;
        //输入框隐藏
        nideInput(node);
        var id=node.id;
        var text=node.text;
        var classIfication= $('#classIficationInfo').val();
        var drawingNo=$('#drawingNoInfo').val();
        var name=$('#nameInfo').val();
        var batchNo=$('#batchNoInfo').val();
        var physicalNo=$('#physicalNoInfo').val();
       var attributes=node.attributes;
       var exhibitionGrid= $('#dam_exhibition_version_grid');
        nodeId = node.id;
        exhibitionGrid.datagrid({
            url:exhibitionListURL,
            queryParams: {'id': id,"classIfication":classIfication,
            "drawingNo":drawingNo,"name":name,"batchNo":batchNo,"physicalNo":physicalNo,"attributes":JSON.stringify(attributes),"text":text}
        });
    };

    function nideInput(node) {
        var level=node.attributes.nodeLevel;
        if(level=="2"){
            //隐藏二级节点对应的输入框
            $('#classIficationInfo')[0].style.display='block'
            $('#drawingNoInfo')[0].style.display='block';
            $('#batchNoInfo')[0].style.display='block';
            $('#classIficationInfo')[0].style.display='none';
        }else if(level=="3"){
            //隐藏三级节点对应的输入框
            $('#classIficationInfo')[0].style.display='block'
            $('#drawingNoInfo')[0].style.display='block';
            $('#batchNoInfo')[0].style.display='block';
            $('#classIficationInfo')[0].style.display='none';
            $('#drawingNoInfo')[0].style.display='none';
        }else if(level=="4"){
            //隐藏四级对应的输入框
            $('#classIficationInfo')[0].style.display='block'
            $('#drawingNoInfo')[0].style.display='block';
            $('#batchNoInfo')[0].style.display='block';
            $('#classIficationInfo')[0].style.display='none';
            $('#drawingNoInfo')[0].style.display='none';
            $('#batchNoInfo')[0].style.display='none';
        }else if(level=="1"){
            $('#classIficationInfo')[0].style.display='block'
            $('#drawingNoInfo')[0].style.display='block';
            $('#batchNoInfo')[0].style.display='block';
        }

    }
    function openLoading(){
        top$.messager.progress({
            width: 160,
            msg: '<div><img src="' + imgDelUrl + '"style="width: 70px;margin-top: -30px;margin-bottom: 20px">' +
                '<br/><span style="font-family: PingFangSC-Regular;' +
                'font-size: 14px;' +
                'color: #585F66;' +
                'letter-spacing: 0;' +
                'text-align: center;' +
                'line-height: 22px;' +
                'font-weight: 400;">加载中...</span></div>',
        });
        top$(".messager-p-bar.progressbar").hide();
        top$(".window-shadow").hide();
        //top$("div.messager-body.panel-body.panel-body-noheader.panel-body-noborder.window-body.window-body-noheader")
        //此时如果通过div寻找，当top$.messager.promptInfo未关闭时，会将其样式也进行修改，因此通过.messager-window去寻找修改样式
        top$(".messager-window .messager-body.panel-body.panel-body-noheader.panel-body-noborder.window-body.window-body-noheader").css({
            'overflow': 'hidden',
            'height': '160px',
            'background': '#FFFFFF',
            'box-shadow': '0 2px 8px 0',
        });
        treeFlag = true;
    }
    /**
     * 查询按钮点击事件
     */
    var modelQuery= this.modelQuery = function () {
        var node = $("#dam_datapacket_model_tree").tree('getSelected');
        var id=node.id;
        var text=node.text;
        // var busSystem=node.attributes.material_object;
        var classIfication= $('#classIficationInfo').val();
        var drawingNo=$('#drawingNoInfo').val();
        var name=$('#nameInfo').val();
        var batchNo=$('#batchNoInfo').val();
        var physicalNo=$('#physicalNoInfo').val();
        var attributes=node.attributes;
        var exhibitionGrid= $('#dam_exhibition_version_grid');
        nodeId = node.id;
        exhibitionGrid.datagrid({
            url:exhibitionListURL,
            queryParams: {'id': id,"classIfication":classIfication,
                "drawingNo":drawingNo,"name":name,"batchNo":batchNo,"physicalNo":physicalNo,"attributes":JSON.stringify(attributes),"text":text}
        });
    };

    /**
     * 重置点击事件
     */
    this.resetBtn = function () {
        $('#classIficationInfo').val('');
        $('#drawingNoInfo').val('');
        $('#nameInfo').val('');
        $('#batchNoInfo').val('');
        $('#physicalNoInfo').val('');
        this.modelQuery();
    };
});



