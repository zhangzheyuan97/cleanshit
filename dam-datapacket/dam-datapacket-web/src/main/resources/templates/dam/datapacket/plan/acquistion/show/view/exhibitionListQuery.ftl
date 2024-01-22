<form id="datapacket_exhibitionList_queryform" onsubmit="return false" style="float:left;">
    <input id="dataTypeId" name="dataTypeId" hidden="true"/>
    <table>
        <td>
            <input type="text" id="classIficationInfo" name="classIficationInfo" class="validatebox-text" data-roles="mui-validatebox"
                   data-options="placeholder:'请输入类别'" style="width: 200px"/>
        </td>
        <td>
            <input type="text" id="drawingNoInfo" name="drawingNoInfo" class="validatebox-text" data-roles="mui-validatebox"
                   data-options="placeholder:'请输入图号'" style="width: 200px;margin-left: 3px"/>
        </td>
        <td>
            <input type="text" id="nameInfo" name="nameInfo" class="validatebox-text" data-roles="mui-validatebox"
                   data-options="placeholder:'请输入名称'" style="width: 200px;margin-left: 3px"/>
        </td>
        <td>
            <input type="text" id="batchNoInfo" name="batchNoInfo" class="validatebox-text" data-roles="mui-validatebox"
                   data-options="placeholder:'请输入批次号'" style="width: 200px;margin-left: 3px"/>
        </td>
        <td>
            <input type="text" id="physicalNoInfo" name="physicalNoInfo" class="validatebox-text" data-roles="mui-validatebox"
                   data-options="placeholder:'请输入实物号'" style="width: 200px;margin-left: 3px"/>
        </td>
        <td>
            <a id="btn" href="javascript:void(0)" data-roles="mui-linkbutton" class="l-btn  button_guide" style="margin-left: 3px"
               onclick="$m('dam/plan/acquistion/show').modelQuery()">查询</a>
        </td>
        <td>
            <a id="btn" href="javascript:void(0)" data-roles="mui-linkbutton" class="l-btn  button_guide" style="background-color: #ffffff !important;border: 1px solid #E1E9F0;"
               onclick="$m('dam/plan/acquistion/show').resetBtn()"><span style="color: #3D4247">重置</span></a>
        </td>
        </tr>
    </table>
</form>