<ul id="dam_datapacket_acquistion_dataAcquistion_tree"
	data-roles="mui-tree"
	data-options="onClick:$m('dam/plan/acquistion/dataAcquistion').selectTree,
	onLoadSuccess: $m('dam/plan/acquistion/dataAcquistion').loadSuccess,
	onDblClick:$m('dam/plan/acquistion/dataAcquistion').dbClickTree,
				searchbox:{
					enable:true,
					width:224,
					prompt:'请输入关键字'
				},
				url: '/dam-datapacket/api/datapacket/gather/nodeTree'">
</ul>