<style>
    /*#addModelMenu{
        width: 109px !important;
    }*/
</style>
<div data-roles="mui-layout" data-options="fit: true,border:false"  id='standAloneList1'>
    <table id="dam_datapackets_stand_alone_grid" data-roles="mui-datagrid" title=""
           data-options="fitColumns:true,
                     <#--url: '<@cloud.router/>/api/datapacket/alone/page',-->
                     queryParams: $m('dam/plan/acquistion/dataAcquistion/standAlone').getQueryParam(),
					 singleSelect:false,
					 pagination:true,
                     paginationType:'advanced',
					 rownumbers: true,
					 fit: true,
<#--					 maxWidth:300,-->
					 border: false">
        <thead>
        <tr>
            <th data-options="field:'id',hidden:true">id</th>
            <th data-options="field:'code', width:60,editor:'text'">序号</th>
            <th data-options="field:'name', width:160,editor:'text'">模板名称32423423</th>
        </tr>
        </thead>
    </table>
</div>