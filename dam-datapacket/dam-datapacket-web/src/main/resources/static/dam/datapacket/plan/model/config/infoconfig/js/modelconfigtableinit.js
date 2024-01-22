$m('dam/metamanage/model/config/tableinit',function(){
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
	this.initPage = function(){
		debugger
		var tabs = $("#modelConfigTab");
		var dbType = $("#dataBaseType").val();
		tabs.tabs({
			onSelect: function(title,index){
				var selectTab = tabs.tabs('getSelected');
				var url ="";
				var adContextPath = getAdContextPath("adContextPath");
				adContextPath = (adContextPath == null ? '' : adContextPath);
				if(title == '属性配置'){
					url = adContextPath + "/dam-metamanage/api/metamanage/modelconfig/field/view/index";
				} else if(title == '页面展示'){
					url = adContextPath + "/dam-metamanage/api/metamanage/model/infoconfig/view/modelshowconfig";
				}
				/*else if(title == '模板设置'){
					url = "/dam-metamanage/api/metamanage/model/templateconfig/view/templatelist";
				}*/
				var iframe =  selectTab.find("iframe");
				iframe.attr("src",url);
			}
		});
		if(dbType =='mongodb'){
			tabs.tabs("close","页面展示");
		}
		if(dbType =='hbase'){
			tabs.tabs("close","属性配置");
			tabs.tabs("close","页面展示");
		}
	}
});