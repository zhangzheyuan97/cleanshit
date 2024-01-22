<!DOCTYPE html>
<html>
<head>
    <@cloud.mui/>
    <#--        <script type="text/javascript" src="/dam-datapacket/dam/datapacket/tag/plugin/js/resource.js"></script>-->
    <#--        <script type="text/javascript" src="/dam-datapacket/dam/datapacket/tag/plugin/js/util.js"></script>-->
    <#--		<script type="text/javascript" src="/dam-datapacket/dam/datapacket/plan/model/manage/js/hiveScript.js"></script>-->
    <#--		<script type="text/javascript" src="/dam-datapacket/dam/datapacket/plan/model/manage/js/mongoManage.js"></script>-->
    <#--		<script type="text/javascript" src="/dam-datapacket/dam/datapacket/plan/model/manage/js/hbaseManage.js"></script>-->
    <script type="text/javascript" src="/dam-datapacket/dam/datapacket/plan/model/manage/js/script.js"></script>
    <#--		<script type="text/javascript" src="/dam-datapacket/dam/datapacket/plan/model/manage/js/extractCreateModel.js"></script>-->
    <#--		<script type="text/javascript" src="/dam-datapacket/dam/datapacket/plan/datasource/database/js/commonDataSource.js"></script>-->
    <link rel="stylesheet" type="text/css" href="/dam-datapacket/dam/datapacket/plan/model/manage/css/modelmanage.css">
</head>
<body data-roles="mui-layout" >
<#--		<div data-options="region:'west',title:'数据主题',split:true,collapsible:true" style="height:100%;width:240px;overflow: hidden;padding:8px">-->
<#--		</div>-->
<div data-roles="mui-panel" data-options="region:'center',border:false" class="layout_center_left">
    <div data-roles="mui-layout" data-options="fit: true,border:false" >
        <#include "view/list.ftl">
    </div>
</div>
<#--		<script>-->
<#--			// 模型初始化全局变量赋值-->
<#--			// $m('dam/meta/model/manage').setModelManageType('model');-->
<#--		</script>-->
</body>
</html>