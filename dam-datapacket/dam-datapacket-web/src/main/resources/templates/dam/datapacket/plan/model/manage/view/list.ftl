<style>
    #addModelMenu{
        width: 109px !important;
    }
</style>
<div data-options="region:'center',title:'',border:false" id='modelVerListContainer'>
    <table id="dam_datapacket_modelVersion_grid" data-roles="mui-datagrid" title=""
           data-options="fitColumns:true,
                     url: '<@cloud.router/>/api/datapacket/manage/page',
					 singleSelect:false,
					 pagination:true,
                     paginationType:'advanced',
					 pageSize:'20',
					 fit: true,
					 border: false,
					 rownumbers: true,
					 toolbar:'#dam_datapacket_model_manage_gridToolbar'">
        <thead>
        <tr>
            <th data-options="field:'ck',checkbox:true"></th>
            <th data-options="field:'id',hidden:true">id</th>
<#--                <th data-options="field:'sort', width:30,editor:'text'">序号</th>-->
            <th data-options="field:'code', width:140,editor:'text'">模板编码</th>
            <th data-options="field:'name', width:160,editor:'text'">模板名称</th>
            <th data-options="field:'tableName', width:140,editor:'text'">表名</th>
            <th data-options="field:'isPool', width:80,editor:'text',formatter:function (value, row, index){
			try{
				 if(value == 1){
				    return '是';
				 }else if (value == 0){
				    return '否';
				 }
			}catch(e){
			}
			}">是否向上汇总
            </th>
            <th data-options="field:'action',width:150,
			formatter:$m('dam/datapacket/plan/model/manage').actionFmt">操作
            </th>
<#--            <th data-options="field:'action',width:150,-->
<#--			formatter:$m('dam/plan/model/manage').actionFmt">操作-->
<#--            </th>-->
        </tr>
        </thead>
    </table>
    <div id="dam_datapacket_model_manage_gridToolbar" class="datagrid-toolbar toolbar_height">
            <#include "moduleListQuery.ftl">
        </div>
    </div>
</div>

<style>
    .datagrid-header-rownumber, .datagrid-cell-rownumber{
        width: 50px !important;
    }
</style>