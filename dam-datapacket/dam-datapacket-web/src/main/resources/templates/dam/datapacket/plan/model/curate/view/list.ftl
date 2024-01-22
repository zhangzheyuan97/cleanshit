<style>
    #addModelMenu{
        width: 109px !important;
    }
</style>
<div data-options="region:'center',title:'',border:false" id='modelVerListContainer'>
    <table id="dam_datapacket_curate_grid" data-roles="mui-datagrid" title=""
           data-options="fitColumns:true,
                     url: '<@cloud.router/>/api/datapacket/planning/page',
                     queryParams: $m('dam/plan/model/curate').getQueryParam(),
					 singleSelect:false,
					 pagination:true,
                     paginationType:'advanced',
					 pageSize:'20',
					 rownumbers: true,
					 fit: true,
					 border: false,
					 toolbar:'#dam_metamanage_model_manage_gridToolbar'">
        <thead>
        <tr>
            <th data-options="field:'id',hidden:true">id</th>
            <th data-options="field:'code', width:160,editor:'text'">模板编码</th>
            <th data-options="field:'name', width:160,editor:'text'">模板名称</th>
            <th data-options="field:'tableName', width:160,editor:'text'">表名</th>
            <th data-options="field:'isPackage', width:160,editor:'text',formatter:function (value, row, index){
			try{
				 if(value == 1){
				    return '是';
				 }else if (value == 0){
				    return '否';
				 }
			}catch(e){
			}


			}">成包是否包含
            </th>
            <th data-options="field:'action',width:150,
			formatter:$m('dam/plan/model/curate').actionFmt">操作
            </th>
        </tr>
        </thead>
    </table>
    <div id="dam_metamanage_model_manage_gridToolbar" class="datagrid-toolbar toolbar_height">
        <#include "modelListQuery.ftl">
    </div>
</div>