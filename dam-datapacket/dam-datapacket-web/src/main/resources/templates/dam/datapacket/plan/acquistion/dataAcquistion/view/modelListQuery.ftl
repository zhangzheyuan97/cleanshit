<form id="module_modelList_dataAcquistion_queryform" onsubmit="return false" style="float:left;">
    <input id="modelName" name="modelName" hidden="true"/>
    <input id="attributes" name="attributes" hidden="true"/>
    <div>
        <tr>
            <td>
                <input type="text" id="lity" name="lity" class="validatebox-text" data-roles="mui-validatebox"
                       data-options="placeholder:'请输入发次'" style="width: 160px"/>
            </td>
            <td>
                <a id="btn" href="javascript:void(0)" data-roles="mui-linkbutton" class="l-btn  button_guide"
                   onclick="$m('dam/plan/acquistion/dataAcquistion').modelQuery()">查询</a>
            </td>
            <td>
                <a id="btn" href="javascript:void(0)" data-roles="mui-linkbutton" class="l-btn  button_guide" style="background-color: #ffffff !important;border: 1px solid #E1E9F0;"
                   onclick="$m('dam/plan/acquistion/dataAcquistion').reset()"><span style="color: #3D4247">重置</span></a>
            </td>
        </tr>
    </div>
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