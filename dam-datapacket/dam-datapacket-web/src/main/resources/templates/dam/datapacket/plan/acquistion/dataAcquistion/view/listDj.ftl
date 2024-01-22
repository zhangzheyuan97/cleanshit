<style>
    /*#addModelMenu{*/
    /*    width: 109px !important;*/
    /*}*/
</style>
<div data-options="region:'center',title:'',border:false" id='modelVerListContainer'>
    <table id="dam_exhibition_version_lity_grid" data-roles="mui-datagrid" title=""
           data-options="fitColumns:true,
<#--                     url:$m('dam/plan/acquistion/show').pagePath,-->
					 singleSelect:false,
					 pagination:true,
                     paginationType:'advanced',
					 pageSize:'20',
					 fit: true,
					 border: false,
					 rownumbers: false,
					 toolbar:'#dam_exhibition_version_gridToolbar'">
        <thead>
        <tr>
            <th data-options="field:'ck',checkbox:true"></th>
            <th data-options="field:'id',hidden:true">id</th>
<#--            <th data-options="field:'sort', width:30,editor:'text'">序号</th>-->
            <th data-options="field:'classIfication', width:140,editor:'text'">类别</th>
            <th data-options="field:'drawingNo', width:160,editor:'text'">图号</th>
            <th data-options="field:'name', width:140,editor:'text'">名称</th>
            <th data-options="field:'batchNo', width:140,editor:'text'">批次号</th>
            <th data-options="field:'physicalNo', width:140,editor:'text'">实物号</th>
            <th data-options="field:'isManageObject', width:160,editor:'text',formatter:function (value, row, index){
			try{
				 if(value == 1){
				    return '是';
				 }else if (value == 0){
				    return '否';
				 }
			}catch(e){
			}
			}">是否管理到实物
            </th>
        </tr>
        </thead>
    </table>
    <div id="dam_exhibition_version_gridToolbar" class="datagrid-toolbar toolbar_height">
        <#include "exhibitionListQuery.ftl">
    </div>
</div>
<#--    <div id="dam_datapacket_model_manage_gridToolbar" class="datagrid-toolbar toolbar_height">-->
<#--        <#include "moduleListQuery.ftl">-->
<#--    </div>-->
</div>
</div>

<style>
    .datagrid-header-rownumber, .datagrid-cell-rownumber{
        width: 50px !important;
    }
</style>