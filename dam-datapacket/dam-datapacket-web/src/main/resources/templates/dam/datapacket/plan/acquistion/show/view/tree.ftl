<ul id="dam_datapacket_model_tree" class="model_tree_list"
	data-roles="mui-tree"
	data-options="onSelect:$m('dam/plan/acquistion/show').selectTree,
				  onDblClick:$m('dam/plan/acquistion/show').dbClickTree,
				  onLoadSuccess: $m('dam/plan/acquistion/show').loadSuccess,
				  url:'/dam-datapacket/api/datapacket/show/exhibition-tree',
<#--				  loadFilter:$m('dam/plan/acquistion/show').loadFilterTree,-->
				  searchbox:{enable:true,width:224,ignoreCase:true,onlyLeaf:true,prompt:'请输入关键字'}">
</ul>