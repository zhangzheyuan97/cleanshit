<form id="centerAloneList_queryform" onsubmit="return false" style="float:left;">
    <input id="nodeId" name="nodeId" hidden="true"/>
    <tr>
        <td>模板名称
            <input type="text" name="name" class="validatebox-text" data-roles="mui-validatebox"
                   data-options="placeholder:'请输入模板名称'" style="width: 160px"/>
        </td>
        <td>
            <a id="btn" href="javascript:void(0)" data-roles="mui-linkbutton" class="l-btn  button_guide"
               onclick="$m('dam/plan/acquistion/dataAcquistion/standAlone').centerListQuery()">查询</a>
        </td>
    </tr>
</form>