<link rel="stylesheet" type="text/css" href="/dam-datapacket/dam/datapacket/plan/acquistion/dataAcquistion/css/jquery.merit.sTags.css">
<link rel="stylesheet" type="text/css" href="/dam-datapacket/dam/datapacket/plan/acquistion/dataAcquistion/css/form.css">
<script type="text/javascript" src="/dam-datapacket/dam/datapacket/plan/acquistion/dataAcquistion/js/read.js"></script>
<#--<div id='addlityView'>-->
	<form id="lityAddForm" class="daiglog-form">
		<div class="form-row-item-xh">
			<label id="xh_label" class="form-lable required">型号</label>
			<input id="model" name="model" class="validatebox-text" data-roles="mui-validatebox" disabled  />
		</div>
		<div class="form-row-item-fc">
			<label id="name_label" class="form-lable required">发次</label>
			<input id="tableName" class="form-input-fc" name="tableName" data-roles="mui-validatebox" data-options="required:true,validType:'tableName' "/>
		</div>
	</form>
<#--</div>-->

<#--<script>-->
<#--	$.extend($.fn.validatebox.defaults.rules, {-->
<#--		tableName: {-->
<#--			validator: function(value) {-->
<#--				return /^([a-zA-Z])+[a-zA-Z0-9_]*$/.test(value) && value.length<=300;-->
<#--			},-->
<#--			message: '表名称由字母、数字、下划线组成，须以字母开头，长度不超过300'-->
<#--		}-->
<#--	});-->
<#--</script>-->
<style>
	#dialogId{
		overflow: auto!important;
	}
	.form-group{
		margin-top: 32px;
		margin-bottom: 16px;
		height: 80px;
		width: 488px;
	}
	.form-row-item-xh{
		padding-bottom: 20px;
		margin-left: 87px;
	}
	.form-row-item-fc {
		margin-left: 87px;
	}

</style>