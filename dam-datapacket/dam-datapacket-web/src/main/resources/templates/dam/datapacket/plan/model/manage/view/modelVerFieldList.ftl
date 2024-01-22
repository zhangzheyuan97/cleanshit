<div data-options="region:'center',title:'',border:false" id='modelVerListContainer'>
<table id="dam_metamanage_modelverfield_grid" data-roles="mui-datagrid" title=""
           data-options="fitColumns:true,
           rownumbers: true,
                        toolbar:'#dam_datapacket_model_manage_modelverfield'
<#--                     url: '<@cloud.router/>/api/datapacket/manage/page',-->
					 ">
        <thead>
        <tr>
            <th data-options="field:'busiName', width:100,editor:'text'">属性名称</th>
            <th data-options="field:'fieldName', width:100,editor:'text'">英文名称</th>
            <th data-options="field:'dataType', width:100,editor:'text'">数据类型</th>
            <th data-options="field:'length', width:100,editor:'text'">长度</th>
            <th data-options="field:'definition', width:100,editor:'text'">精度</th>
            <th data-options="field:'sortNumber', width:100,editor:'text'">排序</th>
            <th data-options="field:'status', width:200,editor:'text',formatter:function (value, row, index){
                try{
                if(value == 1){
                return '启用';
            }else if (value == 0){
            return '停用';
            }
            }catch(e){
            }
            }">状态</th>
        </tr>
        <tr>
<#--            <th></th>-->
            <th><input type="text" id="busiNameInput" name="busiNameInput" style="width:70px;height: 20px;float: left" onChange="$m('dam/datapacket/plan/model/manage').busiNameQuery()" /></th>
            <th><input type="text" id="fieldNameInput" name="fieldNameInput" style="width:70px;height: 20px;float: left" onChange="$m('dam/datapacket/plan/model/manage').busiNameQuery()" /></th>
            <th>
                <select id="dataTypeInput" name="dataTypeInput" style="width:100px;height: 20px;" onChange="$m('dam/datapacket/plan/model/manage').busiNameQuery()">
<#--                    <option>eee</option>-->
                </select>
            </th>
            <th><input type="text" id="lengthInput" name="lengthInput" style="width:70px;height: 20px;float: left" onChange="$m('dam/datapacket/plan/model/manage').busiNameQuery()" /></th>
            <th><input type="text" id="definitionInput" name="definitionInput" style="width:70px;height: 20px;float: left" onChange="$m('dam/datapacket/plan/model/manage').busiNameQuery()" /></th>
            <th><input type="text" id="sortNumberInput" name="sortNumberInput" style="width:70px;height: 20px;float: left" onChange="$m('dam/datapacket/plan/model/manage').busiNameQuery()" /></th>
<#--            <th><input type="text" id="statusInput" name="statusInput" style="width:50px;height: 20px;float: left" onChange="$m('dam/datapacket/plan/model/manage').busiNameQuery()" /></th>-->
            <th>
                <select id="statusInput" name="statusInput" style="width:100px;height: 20px;" onChange="$m('dam/datapacket/plan/model/manage').busiNameQuery()">
                </select>
            </th>
        </tr>
        </thead>
</table>
</div>

<style>

    .div_layout_form-lable {
        width: 300px !important;
    }

    .datagrid-cell-group {
        text-align: left !important;
        /*text-overflow: ellipsis;*/
    }
</style>

        <#--	<div id="dam_datapacket_model_manage_info" class="datagrid-toolbar toolbar_height">-->
    <#--		<#include "modelVerFieldList.ftl">-->
    <#--	</div>-->