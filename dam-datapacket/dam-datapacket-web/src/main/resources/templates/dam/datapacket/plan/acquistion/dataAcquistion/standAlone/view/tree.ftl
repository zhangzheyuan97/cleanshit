<ul id="dam_datapacket_stand_alone_tree"
	data-roles="mui-tree"
	data-options="onClick:$m('dam/plan/acquistion/dataAcquistion/standAlone').selectTree,
	onDblClick:$m('dam/plan/acquistion/dataAcquistion/standAlone').dbClickTree,
	onLoadSuccess:$m('dam/plan/acquistion/dataAcquistion/standAlone').loadSuccess,
				searchbox:{
					enable:true,
<#--					width:224,-->
					prompt:'请输入关键字'
				},
				url: '/dam-datapacket/api/datapacket/alone/nodeTree'">
</ul>