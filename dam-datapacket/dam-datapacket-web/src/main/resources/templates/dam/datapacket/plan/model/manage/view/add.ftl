<#--
<link rel="stylesheet" type="text/css" href="/dam-datapacket/dam/metamanage/tag/plugin/css/jquery.merit.sTags.css">
<link rel="stylesheet" type="text/css" href="<@cloud.router/>/common/css/form.css">
<script type="text/javascript" src="/dam-datapacket/dam/metamanage/tag/plugin/js/jquery.merit.sTags.read.js"></script>
<div id='addDynamicView'>
	<form id="modelAddForm" class="daiglog-form">
		<div class="form-row form-row-item">
			<label id="name_label" class="form-lable required">模型名称</label>
			<#if name?exists>
				<input id="name" class="form-input" name="name" data-roles="mui-validatebox" value="${name}" data-options="required:true,validType:'maxLength[200]'" />
			<#else>
				<input id="name" class="form-input" name="name" data-roles="mui-validatebox"  data-options="required:true,validType:'maxLength[200]'" />
			</#if>
		</div>
		<div class="form-row form-row-item">
			<label id="type_label" class="form-lable required">模型编码</label>
			<#if code?exists>
				<input id="code" name="code" class="form-input" data-roles="mui-validatebox" value="${code}" data-options="required:true,validType:'noCha[1,200]' " />
			<#else>
				<input id="code" name="code" class="form-input" data-roles="mui-validatebox" data-options="required:true,validType:'noCha[1,200]' " />
			</#if>
		</div>
		&lt;#&ndash; ---------------------------------- &ndash;&gt;
		<div class="form-row form-row-item">
			<label id="name_label" class="form-lable required">表名称</label>
			<#if tableName?exists>
				<input id="tableName" class="form-input" name="tableName" value="${tableName}" data-roles="mui-validatebox" data-options="required:true,validType:'tableName' "/>
			<#else>
				<input id="tableName" class="form-input" name="tableName" data-roles="mui-validatebox" data-options="required:true,validType:'tableName' "/>
			</#if>
		</div>
		<div class="form-row form-row-item">
			<label id="databaseId_label" class="form-lable required" >目标数据源</label>
			<input id="databaseId" name="databaseId" class="form-input"
				   data-options="editable:true,required:true,textField:'name',valueField:'id',
                    onHidePanel: function () {
						var val = $(this).combobox('getValue'); // 获取输入框值
						var allData = $(this).combobox('getData'); // 获取下拉框所有数据
						var result = true; // 为true说明在下拉框中不存在
						for (var i=0;i<allData.length;i++) {
							if (val == allData[i]['id']) {
								result = false;
							}
						}
						if (result) {
							$(this).combobox('clear');
						}
					}"
				   data-roles="mui-combobox" />
		</div>
		<div class="form-row form-row-item">
			<label id="status_label" class="form-lable required" title="管理数据版本">管理数据版本</label>
			<input type="radio" class="form-input" name="manageVersion" value="1"/><span>是&nbsp;</span>
			<input type="radio" name="manageVersion" value="0" checked="checked"
				   class="dialog_form_radio form-input"/><span>否</span>
		</div>

		<div class="form-row form-row-item">
			<label id="memo_label" class="form-lable">备注</label>
			<textarea name="remark" rows="5" data-roles="mui-validatebox" class="form-input" data-options="validType:'maxLength[1300]'" style="width:300px;resize:none;"></textarea>
		</div>
		<div class="form-row form-row-item" style="display:none">
			<label id="dataTypeId_label">数据分类id</label>
			<input id="dataTypeId" name="dataTypeId" class="form-input"/>
		</div>
		<div id="resourceTags" class="form-row form-row-item">
			<label id="tag_label" class="form-lable" style="float: left;line-height: 44px;">标签</label>
			<div style="float: left; width: 570px;">
				<input type="text" class="form-input" style="display: none;">
				<button type="button" class="addTagButton" style="outline: none">
					<img src="/dam-datapacket/dam/metamanage/tag/plugin/image/add.svg" style="width: 16px;height: 16px;margin-top: -2px">
					<span style="display: inline-block;">打标签</span>
				</button>
			</div>
		</div>
	</form>
</div>

<script>
	// $.extend($.fn.validatebox.defaults.rules, {
	// 	tableName: {
	// 		validator: function(value) {
	// 			return /^([a-zA-Z])+[a-zA-Z0-9_]*$/.test(value) && value.length<=300;
	// 		},
	// 		message: '表名称由字母、数字、下划线组成，须以字母开头，长度不超过300'
	// 	}
	// });
</script>
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
</style>-->
