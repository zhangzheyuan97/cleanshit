<form id="datapacket_exhibitionList_queryform" onsubmit="return false" style="float:left;">
    <input id="dataTypeId1" name="dataTypeId1" hidden="true"/>
    <table>
        <td>
            <input type="text" id="physicalNoInfo" name="physicalNoInfo" class="validatebox-text" data-roles="mui-validatebox"
                   data-options="placeholder:'请输入实物号'" style="width: 160px"/>
        </td>
        <td>
            <a id="btn" href="javascript:void(0)" data-roles="mui-linkbutton" class="l-btn  button_guide" style="margin-left: 3px"
               onclick="$m('dam/plan/acquistion/dataAcquistion').modelQuery()">查询</a>
        </td>
        <td>
            <a id="btn" href="javascript:void(0)" data-roles="mui-linkbutton" class="l-btn  button_guide" style="background-color: #ffffff !important;border: 1px solid #E1E9F0;"
               onclick="$m('dam/plan/acquistion/dataAcquistion').reset()"><span style="color: #3D4247">重置</span></a>
        </td>
        </tr>
    </table>
    <div style="margin-top:20px;margin-bottom: 7px">
        <tr>
            <td>
                <a id="btn" href="javascript:void(0)" data-roles="mui-linkbutton" class="l-btn  button_guide"
                   onclick="$m('dam/plan/acquistion/dataAcquistion').modelAdd()">新增</a>
            </td>
            <td>
                <a id="btn" href="javascript:void(0)" data-roles="mui-linkbutton" class="l-btn  button_guide"
                   onclick="$m('dam/plan/acquistion/dataAcquistion').modelDelete()">删除</a>
            </td>
        </tr>
    </div>
</form>