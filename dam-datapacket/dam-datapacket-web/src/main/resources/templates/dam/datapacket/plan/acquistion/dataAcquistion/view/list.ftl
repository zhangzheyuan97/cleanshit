<style>
    /*#addModelMenu{*/
    /*    width: 109px !important;*/
    /*}*/
    .datagrid-view {
        height: 750px!important;
    }
</style>
<div data-options="region:'center',title:'',border:false" id='modelVerListContainer'>
    <table id="dam_datapacket_acquistion_dataAcquistion_grid" data-roles="mui-datagrid" title=""
           data-options="fitColumns:true,
                     url: '<@cloud.router/>/api/datapacket/gather/page',
                     queryParams: $m('dam/plan/acquistion/dataAcquistion').getQueryParam(),
					 singleSelect:false,
					 pagination:true,
                     paginationType:'advanced',
					 pageSize:'20',
					 rownumbers: false,
					 fit: true,
					 border: false,
					 toolbar:'#dam_metamanage_acquistion_dataAcquistion_gridToolbar'">
        <thead>
        <tr>
            <th data-options="field:'ck',checkbox:true"></th>
            <th data-options="field:'id',hidden:true">id</th>
            <th data-options="field:'num', width:160,editor:'text'">序号</th>
            <th data-options="field:'modelName', width:160,editor:'text'">型号</th>
            <th data-options="field:'lity', width:160,editor:'text'">发次</th>
            <th data-options="field:'action',width:150,
			formatter:$m('dam/plan/acquistion/dataAcquistion').actionFmt">操作
            </th>
        </tr>
        </thead>
    </table>
    <div id="dam_metamanage_acquistion_dataAcquistion_gridToolbar" class="datagrid-toolbar toolbar_height">
        <#include "modelListQuery.ftl">
    </div>
</div>