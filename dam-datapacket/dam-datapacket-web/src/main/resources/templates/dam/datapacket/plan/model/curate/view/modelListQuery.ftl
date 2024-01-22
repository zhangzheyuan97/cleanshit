<form id="module_modelList_queryform" onsubmit="return false" style="float:left;">
    <input id="nodeId" name="nodeId" hidden="true"/>
    <tr>

        <td>
            <input type="text" name="code" class="validatebox-text" data-roles="mui-validatebox"
                   data-options="placeholder:'请输入模板编码'" style="width: 160px"/>
        </td>
        <td>
            <input type="text" name="name" class="validatebox-text" data-roles="mui-validatebox"
                   data-options="placeholder:'请输入模板名称'" style="width: 160px"/>
        </td>
        <td>
            <input type="text" name="tableName" class="validatebox-text" data-roles="mui-validatebox"
                   data-options="placeholder:'请输入表名'" style="width: 160px"/>
        </td>


        <td>
            <a id="btn" href="javascript:void(0)" data-roles="mui-linkbutton" class="l-btn  button_guide"
               onclick="$m('dam/plan/model/curate').modelQuery()">查询</a>
        </td>
    </tr>
</form>