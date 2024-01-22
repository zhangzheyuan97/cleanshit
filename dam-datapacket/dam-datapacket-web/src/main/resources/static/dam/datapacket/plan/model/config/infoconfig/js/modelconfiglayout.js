$m('dam/metamanage/model/config/layout',function(){
    var modelconfigindex = "/dam-metamanage/api/metamanage/model/infoconfig/view/index";
	var modelMoudlePath = '/dam-metamanage/api/metamanage/model/manage/';

	this.initPage = function(){
		$('#dam_modelConfig_tree').tree({
			onSelect:modelConfigTreeClick,
			url: '/dam-metamanage/api/metamanage/tree/dataTypeTreeByModel',
			onLoadSuccess:function(node, data){
				var root = $(this).tree('getRoot');
				var firstModelNode = getFirstLeafNode($(this), root);
				//默认选中第已个模型
				var isModel = !firstModelNode.attributes.leaf;
				if(isModel){
					$(this).tree('select',firstModelNode.target);
				}
			},
			onBeforeSelect:function(node){
				var isModel = !node.attributes.leaf;
				// 只允许选中模型节点
				return !!isModel;
			},
			searchbox:{enable:true,ignoreCase:true,onlyLeaf:true,prompt:'请输入查询模型名称'}
		});
	};

	/**
	 * 获取节点的第一个叶子节点
	 * @param treeJq
	 * @param node
	 * @returns {*}
	 */
	function getFirstLeafNode(treeJq, node){
		var children = treeJq.tree('getChildren',node.target);
		if(children.length > 0){
			var first = children[0];
			return getFirstLeafNode(treeJq, first);
		}else{
			return node;
		}
	}

	/**
	 * 获取AD域信息
	 * @param name
	 * @returns {string}
	 */
	var getAdContextPath = function(name) {
		var arr, reg = new RegExp("(^| )" + name + "=([^;]*)(;|$)");
		if (arr = document.cookie.match(reg)) {
			window.adContextPath = unescape(arr[2]);
			return unescape(arr[2]);
		} else {
			window.adContextPath = '';
			return '';
		}
	};
	/**
	 * 模型配置模型树选择事件
	 */
    var modelConfigTreeClick = this.modelConfigTreeClick = function(){
		var node = $("#dam_modelConfig_tree").tree('getSelected');
        var modelVerId= node.attributes.id;
        getModelInfo(modelVerId,function(data){
            if(data && data['success']){
                var modelInfo= data['data'];
                var database = modelInfo["database"];
				var adContextPath = getAdContextPath("adContextPath");
				adContextPath = (adContextPath == null ? '' : adContextPath);
				var src = adContextPath +"?modelverid="+modelInfo["id"]+"&modelId="+modelInfo["modelInfo"]+"&dataBaseType=" +database["type"]+"&modelName="+modelInfo["name"];
                var iframe =  $("#model_config_frame");
                iframe.attr("src",src);
            }
        });
	};

    var getModelInfo = function(modelId,successCallBack){
    	$.ajax({
			url: modelMoudlePath+"getModelVersionById",
			type: "POST",
			data: {"id": modelId},
			dataType: "JSON",
			success: successCallBack
		});
	};

});