<style>
	#addModelMenu{
		width: 109px !important;
	}
</style>
<div data-options="region:'center',title:'',border:false" id='modelVerListContainer'>
	<table id="dam_datapacket_moduleManage_grid" data-roles="mui-datagrid" title=""
		   data-options="fitColumns:true
<#--                     url: '<@cloud.router/>/api/datapacket/manage/page',-->
					 ">
		<thead>
		<tr>
<#--			<th data-options="field:'ck',checkbox:true"></th>-->
			<#--            <th data-options="field:'id',hidden:true">id</th>-->
			<th data-options="field:'code', width:160,editor:'text'">模板编码</th>
			<th data-options="field:'name', width:160,editor:'text'">模板名称</th>
			<th data-options="field:'tableName', width:160,editor:'text'">表名</th>
		</tr>
		</thead>
	</table>
<#--	<div id="dam_datapacket_model_manage_info" class="datagrid-toolbar toolbar_height">-->
<#--		<#include "modelVerFieldList.ftl">-->
<#--	</div>-->
</div>