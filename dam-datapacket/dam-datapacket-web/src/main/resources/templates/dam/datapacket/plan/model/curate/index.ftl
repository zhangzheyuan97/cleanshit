<!DOCTYPE html>
<html>
	<head>
		<@cloud.mui/>
		<script type="text/javascript" src="/dam-datapacket/dam/datapacket/plan/model/curate/js/script.js"></script>
		<link rel="stylesheet" type="text/css" href="/dam-datapacket/dam/datapacket/plan/model/curate/css/modelmanage.css">
	</head>
	<body data-roles="mui-layout" >
	<div data-options="region:'west', title:'模型树',split:true,collapsible:true" style="height:100%;width:240px;overflow: hidden;padding:8px">
		<#include "view/tree.ftl">
	</div>
<#--		<div data-options="region:'west',title:'数据包域',split:true,collapsible:true" style="height:100%;width:240px;overflow: hidden;padding:8px">-->
<#--			<#include "view/tree.ftl">-->
<#--		</div>s-->
<#--		<div data-options="region:'west',title:'数据包域',split:true,collapsible:true" style="height:100%;width:240px;overflow: hidden;padding:8px">-->
<#--		</div>-->
		<div data-roles="mui-panel" data-options="region:'center',border:false" class="layout_center_left">
			<div data-roles="mui-layout" data-options="fit: true,border:false" >
				 <#include "view/list.ftl">
			</div>
		</div>
	</body>
</html>