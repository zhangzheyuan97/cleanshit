<form id="datapacket_modelList_queryform" onsubmit="return false" style="float:left;">
    <input id="dataTypeId" name="dataTypeId" hidden="true"/>
    <table>
        <td>
            <input type="text" id="moduleCode" name="moduleCode" class="validatebox-text" data-roles="mui-validatebox"
                   data-options="placeholder:'请输入模板编码'" style="width: 200px"/>
        </td>
        <td>
            <input type="text" id="moduleName" name="moduleName" class="validatebox-text" data-roles="mui-validatebox"
                   data-options="placeholder:'请输入模板名称'" style="width: 200px;margin-left: 3px"/>
        </td>
        <td>
            <input type="text" id="tableName" name="tableName" class="validatebox-text" data-roles="mui-validatebox"
                   data-options="placeholder:'请输入表名'" style="width: 200px;margin-left: 3px"/>
        </td>

        <td>
            <a id="btn" href="javascript:void(0)" data-roles="mui-linkbutton" class="l-btn  button_guide" style="margin-left: 3px"
               onclick="$m('dam/datapacket/plan/model/manage').modelQuery()">查询</a>
        </td>
        <td>
            <a id="btn" href="javascript:void(0)" data-roles="mui-linkbutton" class="l-btn  button_guide" style="background-color: #ffffff !important;border: 1px solid #E1E9F0;"
               onclick="$m('dam/datapacket/plan/model/manage').resetBtn()"><span style="color: #3D4247">重置</span></a>
        </td>
    </tr>
    </table>
</form>