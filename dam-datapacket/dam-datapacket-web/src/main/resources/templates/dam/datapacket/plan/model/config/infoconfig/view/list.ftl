<style>
    #addModelMenu{
        width: 109px !important;
    }
</style>
<div data-options="region:'center',title:'',border:false" id='modelVerListContainer'>
    <div class="div_layout_form-input form-row">
        <div id="codeVal" hidden></div>
        <label class="div_layout_form-lable required">成包是否包含</label>
        <input type="radio" id="dam_datapacket_curate_dialog_form_radio1" class="dialog_form_radio" name="isPackage" value="1"  onchange="$m('dam/plan/model/curate').changeType(1)"/>
        <span>是&nbsp;</span>
        <input type="radio" id="dam_datapacket_curate_dialog_form_radio" class="dialog_form_radio" name="isPackage" value="0" checked="checked" onchange="$m('dam/plan/model/curate').changeType(0)"/>
        <span>否&nbsp;</span>
    </div>
    <table id="dam_datapacket_curate_code_grid"
           data-roles="mui-datagrid" title=""
           data-options="fitColumns:true,
					 singleSelect:false,
					 fit: true,
					 border: false"
    >
        <thead>
            <tr>
                <th data-options="field:'id',hidden:true">id</th>
                <th data-options="field:'columnName', width:160,">属性名称</th>
                <th data-options="field:'needColumn', width:160,formatter:$m('dam/plan/model/curate').needColumnFormatter"><input type="checkbox" id="needColumn" onchange="$m('dam/plan/model/curate').allNeedColumn()">      所需字段</th>
                <th data-options="field:'isSearch', width:160,formatter:$m('dam/plan/model/curate').isSearchFormatter"><input type="checkbox" id="isSearch" onchange="$m('dam/plan/model/curate').allSearch()">      查询条件</th>
                <th data-options="field:'lookup',width:100,editor:{type: 'combobox', options: {textField: 'name',valueField: 'code',editable: false,multiple:true}}">
                    数据字典值</th>
            </tr>
        </thead>
    </table>

</div>