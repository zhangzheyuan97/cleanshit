<!DOCTYPE html>
<html>
<head>
    <@cloud.mui/>
    <script type="text/javascript" src="/dam-datapacket/dam/datapacket/plan/acquistion/maintain/js/script.js"></script>
    <link rel="stylesheet" type="text/css" href="/dam-datapacket/dam/datapacket/plan/maintain/show/css/modelmanage.css">
    <style>
        .layout_center_list {
            width: 35%;
            height: 100%;
            float: left;
            /*margin-top: -7px;*/
        }

        .layout_east_list{
            width: 65%;
            height: 100%;
        }
    </style>
</head>
<body data-roles="mui-layout" id="datapacket_data_acquistion_layout">
<div data-options="region:'west', title:'模型树',split:true,collapsible:true" style="height:100%;width:240px;overflow: hidden;padding:8px">
    <#include "view/tree.ftl">
</div>
<#--<div data-roles="mui-layout" data-options="region:'center',border:false"  class="layout_center_left">-->
<#--    <div data-roles="mui-layout" data-options="fit: true,border:false" >-->
<#--        <#include "view/list.ftl">-->
<#--    </div>-->
<#--</div>-->
<div data-options="region:'center',border:false,split:true" id="maintain_center_list" class="layout_center_list">
    <div data-roles="mui-layout" data-options="fit: true,border:false" >
        <#include "view/centerList.ftl">
    </div>
</div>
<div data-options="region:'east',border:false,split:true" id="maintain_center_list" class="layout_east_list">
    <div data-roles="mui-layout" data-options="fit: true,border:false" >
        <#include "view/eastList.ftl">
    </div>
</div>
</body>
</html>