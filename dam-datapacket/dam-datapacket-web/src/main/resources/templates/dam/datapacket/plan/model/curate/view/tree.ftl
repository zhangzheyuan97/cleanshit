<ul id="dam_datapacket_model_tree"
	data-roles="mui-tree"
	data-options="onClick:$m('dam/plan/model/curate').selectTree,
	onDblClick:$m('dam/plan/model/curate').dbClickTree,
				searchbox:{
					enable:true,
					width:224,
					prompt:'请输入关键字'
				},
				url: '/dam-datapacket/api/datapacket/planning/nodeTree'">
</ul>