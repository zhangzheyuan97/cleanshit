$m('dam/datapacket/plan/model/manage', function () {
    // this.getDataList='dam-datapacket/api/datapacket/manage/api/datapacket/manage/page'
    var authorizeDialog;
    var addDialog; //配置页面弹框
    var modelConfigModulePath = '/dam-datapacket/api/datapacket/manageConfig/';
    var moduleIsPoolByCode='/dam-datapacket/api/datapacket/manage/moduleIsPoolByCode';
    var moduleByCode='/dam-datapacket/api/datapacket/manage/moduleVerFieldById';
    var moduleConfig='/dam-datapacket/api/datapacket/manage/moduleConfig';
    var code;
    var moduleGrid;
    var modelConfigDialog;
    var manageDialog;
    var manageIndex;
    this.actionFmt = function (value, rowData, index) {
        var button = "";
        button += '<a id="dsadasd" class="actionColumns l-btn l-btn-medium l-btn-normal"data-roles="mui-linkbutton" title="属性查看" group="" href="javascript:void(0)" data-options="theme:\'brand\'"\n' +
            "         onclick=\"$m('dam/datapacket/plan/model/manage').modelManageRouter('" + index + "');\">" +
            '        <span class="l-btn-left">\n' +
            '            <span class="l-btn-text">属性查看</span>\n' +
            '        </span>\n' +
            '    </a>';

        button += '<a class="actionColumns l-btn l-btn-medium l-btn-normal" group=""data-roles="mui-linkbutton"title="配置" href="javascript:void(0)" data-options="theme:\'brand\'"\n' +
            "onclick=\"$m('dam/datapacket/plan/model/manage').modelConfig('" + index + "');\">" +
            '        <span class="l-btn-left">\n' +
            '            <span class="l-btn-text">配置</span>\n' +
            '        </span>\n' +
            '    </a>';
        return button;
    }
    $(function () {
        moduleGrid = $("#dam_datapacket_moduleManage_grid");
    });

    /**
     * 获取数据库类型列表
     * @returns {*|*[]}
     */
    var getDbTypeList = function() {
        var record = $('#dam_datapacket_modelVersion_grid').datagrid('getRows')[manageIndex];
        // var dbTypeList = getDbTypeList();
        var href;
        var adContextPath = getAdContextPath("adContextPath");
        code=record.code;
        var dbTypeList;
        //查询数据库类型
        $.ajax({
            url: '/dam-datapacket/api/datapacket/manage/moduleVerFieldDataType?code=' + code,
            method: 'post',
            dataType: 'json',
            async: false,
            success: function (data) {
                if (data.success) {
                    dbTypeList = data.data;
                }
            }
        });
        return dbTypeList || [];
    }


    this.modelManageRouter=function (index) {
        manageIndex=index;
        var record = $('#dam_datapacket_modelVersion_grid').datagrid('getRows')[index];
        var dbTypeList = getDbTypeList();
        var href;
        var adContextPath = getAdContextPath("adContextPath");
        code=record.code;
        adContextPath = (adContextPath == null ? '' : adContextPath);
        href = adContextPath + '/dam-datapacket/api/datapacket/manage/view/manageIndex';
        manageDialog = $.topDialog({
            title: '模型管理',
            modal: true,
            width: 1400,
            height: 700,
            href: href,
            onClose: function(){
                var form = $('#datapacket_modelList_queryform');
                $('#dam_datapacket_modelVersion_grid').datagrid("reload", form.form('getRecord'));
            },
            onLoad: function () {
                manageDialog.find('#dam_datapacket_moduleManage_grid').datagrid({
                                    url: '/dam-datapacket/api/datapacket/manage/moduleVerFieldById?code=' + code,
                                    // rownumbers: true
                                });
                manageDialog.find('#dam_metamanage_modelverfield_grid').datagrid({
                    url: '/dam-datapacket/api/datapacket/manage/moduleVerFieldInfo?code=' + code,
                });
                $('#tableName').html(record.name);
                //数据类型下拉框
                document.getElementById('dataTypeInput').options.add(new Option('',''))
                dbTypeList.forEach(item=>{
                    var dom =document.createElement('option')
                    document.getElementById('dataTypeInput').options.add(new Option(item.name,item.name))

                })
                //启停用状态下拉框
                var enable=[{
                    value:'0',
                    label:'停用'
                },
                    {
                        value:'1',
                        label:'启用'
                    }]

                document.getElementById('statusInput').options.add(new Option('',''))
                enable.forEach(item=>{
                    var dom =document.createElement('option')
                    document.getElementById('statusInput').options.add(new Option(item.label,item.value))
                })
            }
        });
    };

    this.modelConfig = function (index) {
        var record = $('#dam_datapacket_modelVersion_grid').datagrid('getRows')[index];
        var href;
        var adContextPath = getAdContextPath("adContextPath");
        adContextPath = (adContextPath == null ? '' : adContextPath);
        href = adContextPath + modelConfigModulePath + 'view/index?code=' + record.code;
        code=record.code;
        modelConfigDialog = $.topDialog({
            title: '配置',
            modal: true,
            width: 500,
            height: 300,
            href: href,
            // closable: false,
            onClose: function(){
                var form = $('#datapacket_modelList_queryform');
                $('#dam_datapacket_modelVersion_grid').datagrid("reload", form.form('getRecord'));
            },
            onLoad: function () {
                $.ajax({
                    url: moduleIsPoolByCode,
                    method: 'post',
                    data: {'code': code},
                    dataType: 'json',
                    async: false,
                    success: function (data) {

                        if (data.success) {

                            if ('1' == data.data.isPool) {
                                modelConfigDialog.find("input[name='isPool']")[0].checked=true
                            } else {
                                modelConfigDialog.find("input[name='isPool']")[1].checked=true
                            }
                        }
                    }
                });
            },
            buttons: [
                {
                    text: '关闭',
                    handler: function () {
                        $.topDialog('close', modelConfigDialog);
                        var form = $('#datapacket_modelList_queryform');
                        $('#dam_datapacket_modelVersion_grid').datagrid("reload", form.form('getRecord'));
                        // modelConfigDialog.dialog('close');
                    }
                },
                {
                    text: '保存',
                    id: 'addSaveBtn',
                    handler: function () {
                        editSave(modelConfigDialog, record);
                    }
                }
            ]
        });
    };
    /**
     * 编辑窗口保存方法
     */
    var editSave = function (modelConfigDialog, record) {
        // 数据建模初始化的生效版本模型编辑保存时执行保存+生效
        // var isPool = modelConfigDialog.find('#purpose').val();
        var isPool =modelConfigDialog.find(':radio[name="isPool"]:checked').val();
        $.ajax({
                url: moduleConfig,
                method: 'post',
                data: {'code': record.code,'isPool':isPool},
                dataType: 'json',
                async: false,
                success: function (data) {
                    if (data.success) {
                        // modelQuery();
                        var form = $('#datapacket_modelList_queryform');
                        $('#dam_datapacket_modelVersion_grid').datagrid("reload", form.form('getRecord'));
                        modelConfigDialog.dialog('close');
                    }
                }
            });
    };
    /**
     * 是否向上汇总查询
     * */
    this.isPoolQuery = function (value) {

    }
    /**
     * 查询按钮点击事件
     */
    this.modelQuery = function () {
        var form = $('#datapacket_modelList_queryform');
        $('#dam_datapacket_modelVersion_grid').datagrid("reload", form.form('getRecord'));
    };

    /**
     * 重置点击事件
     */
    this.resetBtn = function () {
        $('#moduleCode').val('');
        $('#moduleName').val('');
        $('#tableName').val('');
        var form = $('#datapacket_modelList_queryform');
        $('#dam_datapacket_modelVersion_grid').datagrid("reload", form.form('getRecord'));
    };

    /**
     * 模板管理属性查看详情页面字段信息实时查询
     */
    var busiNameQuery = this.busiNameQuery=function (){
        var record = $('#dam_datapacket_modelVersion_grid').datagrid('getRows')[manageIndex];
        var href;
        var adContextPath = getAdContextPath("adContextPath");
        adContextPath = (adContextPath == null ? '' : adContextPath);
        // href = adContextPath + modelConfigModulePath + 'view/index?code=' + record.code;
        code=record.code;
        var busiName = $('#busiNameInput').val();
        var fieldName = $('#fieldNameInput').val();
        var dataType = $('#dataTypeInput').val();
        var length = $('#lengthInput').val();
        var definition = $('#definitionInput').val();
        var sortNumber = $('#sortNumberInput').val();
        var status = $('#statusInput').val();
        $('#dam_metamanage_modelverfield_grid').datagrid({
            url: '/dam-datapacket/api/datapacket/manage/moduleVerFieldInfo',
            queryParams:{'code':code,'busiName':busiName,'fieldName':fieldName,
            'dataType':dataType,'length':length,'definition':definition,'sortNumber':sortNumber,'status':status}
        });
        var dbTypeList = getDbTypeList();
        document.getElementById('dataTypeInput').options.add(new Option('',''))
        dbTypeList.forEach(item=>{
            var dom =document.createElement('option')
            document.getElementById('dataTypeInput').options.add(new Option(item.name,item.name))

        })

        //启停用状态下拉框
        var enable=[{
            value:'0',
            label:'停用'
        },
            {
                value:'1',
                label:'启用'
            }]
        document.getElementById('statusInput').options.add(new Option('',''))
        enable.forEach(item=>{
            var dom =document.createElement('option')
            document.getElementById('statusInput').options.add(new Option(item.label,item.value))
        })
        $('#busiNameInput').val(busiName);
        $('#fieldNameInput').val(fieldName);
        $('#dataTypeInput').val(dataType);
        // $('#dataTypeInput').combobox('setValue', dataType);
        $('#lengthInput').val(length);
        $('#definitionInput').val(definition);
        $('#sortNumberInput').val(sortNumber);
        $('#statusInput').val(status);
    }
})