<style>

</style>
<div data-roles="mui-layout" data-options="fit: true,border:false" id='standAloneList'>
    <div id="dam_standAlone_gridToolbar" class="datagrid-toolbar toolbar_height">
        <#include "centerListQuery.ftl">
    </div>
    <table id="dam_datapacket_stand_alone_grid" data-roles="mui-datagrid" title=""
           data-options="fitColumns:true,
                     url: '<@cloud.router/>/api/datapacket/alone/page',
                     queryParams: $m('dam/plan/acquistion/dataAcquistion/standAlone').getQueryParam(),
					 singleSelect:false,
					 pagination:true,
                     paginationType:'advanced',
					 rownumbers: true,
					 fit: true,
<#--					 maxWidth:350,-->
					 border: false,
                     toolbar:'#dam_standAlone_gridToolbar'">
        <thead>
        <tr>
            <th data-options="field:'id',hidden:true">id</th>
            <th data-options="field:'code', width:60,editor:'text'">序号</th>
            <th data-options="field:'name', width:160,editor:'text'">模板名称</th>
        </tr>
        </thead>
    </table>
</div>
