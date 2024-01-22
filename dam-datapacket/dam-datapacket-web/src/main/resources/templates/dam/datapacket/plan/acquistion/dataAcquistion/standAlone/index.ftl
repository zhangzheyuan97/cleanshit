<!DOCTYPE html>
<html>
<head>
    <@cloud.mui/>
    <script type="text/javascript" src="/dam-datapacket/dam/datapacket/plan/acquistion/dataAcquistion/standAlone/js/script.js"></script>
<#--    <link rel="stylesheet" type="text/css" href="/dam-datapacket/dam/datapacket/plan/acquistion/show/css/modelmanage.css">-->
    <style>
        .layout_center_left_df {
            width: 35%;
            /*height: 100%;*/
            float: left;
        }

        .layout_center_left_dd{
            width: 65%;
            /*height: 100%;*/
        }
    </style>
</head>
<body data-roles="mui-layout">
<div data-options="region:'west', title:'数据包采集_单机',split:true,collapsible:true" style="height:100%;width:240px;overflow: hidden;padding:8px">
    <div data-roles="mui-layout" data-options="fit: true,border:false" >
        <#include "view/tree.ftl">
    </div>
</div>
<div data-options="region:'center',border:false,split:true" class="layout_center_left_df">
<#--    <div data-roles="mui-layout" data-options="fit: true,border:false" >-->
        <#include "view/centerList.ftl">
<#--    </div>-->
</div>
<div data-options="region:'east',border:false,split:true" class="layout_center_left_dd">
<#--    <div data-roles="mui-layout" data-options="fit: true,border:false" >-->
        <#include "view/eastList.ftl">
<#--    </div>-->
</div>
</body>
</html>