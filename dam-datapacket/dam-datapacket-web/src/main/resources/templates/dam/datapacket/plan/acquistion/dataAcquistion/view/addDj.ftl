<link rel="stylesheet" type="text/css" href="/dam-datapacket/dam/datapacket/plan/acquistion/dataAcquistion/css/jquery.merit.sTags.css">
<link rel="stylesheet" type="text/css" href="/dam-datapacket/dam/datapacket/plan/acquistion/dataAcquistion/css/form.css">
<script type="text/javascript" src="/dam-datapacket/dam/datapacket/plan/acquistion/dataAcquistion/js/read.js"></script>
<#--<div id='addlityView'>-->
	<form id="lityMatterAddForm" class="daiglog-form">
		<div class="form-row-item-category">
			<label id="xh_label" class="form-lable required">类别</label>
			<input id="category" name="category" class="validatebox-text" data-roles="mui-validatebox" disabled  />
		</div>
		<div class="form-row-item-figure">
			<label id="name_label" class="form-lable required">图号</label>
			<input id="figure" class="form-input-fc" name="figure" data-roles="mui-validatebox" disabled />
		</div>
		<div class="form-row-item-text">
			<label id="name_label" class="form-lable required">名称</label>
			<input id="text" class="form-input-fc" name="text" data-roles="mui-validatebox" disabled/>
		</div>
		<div class="form-row-item-lityNo">
			<label id="name_label" class="form-lable required">批次号</label>
			<input id="lityNo" class="form-input-fc" name="lityNo" data-roles="mui-validatebox"/>
		</div>
		<div class="form-row-item-matterNo">
			<label id="name_label" class="form-lable required">实物号</label>
			<input id="matterNo" class="form-input-fc" name="matterNo" data-roles="mui-validatebox" data-options="required:true,validType:'matterNo' "/>
		</div>
		<div class="manageMatter" id="manageMatter" style="width: 81%;margin-left: 12px;margin-top: 16px">
<#--			<div id="codeVal" hidden></div>-->
			<label class="div_layout_form-lable required">是否管理到实物</label>
			<input type="radio" id="dam_datapacket_curate_dialog_form_radio1" class="dialog_form_radio" name="isManageObject" value="1" checked="checked"  onchange="$m('dam/plan/acquistion/dataAcquistion').changeManageObject(1)"/>
			<span>是&nbsp;</span>
			<input type="radio" id="dam_datapacket_curate_dialog_form_radio" class="dialog_form_radio" name="isManageObject" value="0" onchange="$m('dam/plan/acquistion/dataAcquistion').changeManageObject(0)"/>
			<span>否&nbsp;</span>
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
	.form-row-item-category{
		padding-bottom: 20px;
		margin-left: 87px;
	}
	.form-row-item-fc {
		margin-left: 87px;
	}
	.form-row-item-figure {
		margin-left: 85px;
	}
	.form-row-item-text {
		margin-top: 20px;
		margin-left: 85px;
	}
	.form-row-item-lityNo {
		margin-top: 20px;
		margin-left: 71px;
	}
	.form-row-item-matterNo {
		margin-top: 20px;
		margin-left: 71px;
	}

</style>