$m('dam/metamanage/standard/dataelement/versionManage', function () {
    var versionManageView = moduleResource.element.getView('versionManage');
    var versionMgGridId = 'dam_matemanage_data_standard_el_version_grid';
    var curEVManageUrl = 'dam/metamanage/standard/dataelement/versionManage';
    var versionManageDialog;
    this.initVersionManage = function (elementId) {
        versionManageDialog = $.topDialog({
            title: '版本查看',
            href: versionManageView,
            width: 1250,
            height: 600,
            onLoad: function () {
                Api.getVersions(elementId, function (result) {
                    versionManageDialog.find('#' + versionMgGridId).datagrid('loadData', result);
                });
            }
        });
    };

    this.fmtConfigEnum = function (value, row, index) {
        return StandardUtils.isNotEmpty(value) ? '是' : '否';
    };

    this.fmtConfigEmpty = function (value, row, index) {
        return StandardUtils.fmtBooleanValue(value);
    };

    this.fmtConfigUnique = function (value, row, index) {
        return StandardUtils.fmtBooleanValue(value);
    };

    this.fmtActionBtn = function (value, row, index) {
        var btnFun = " onclick=\"$m('" + curEVManageUrl + "').showVersionDetail('" + row.versionId + "');\"";
        return "<a href=\"javascript:void(0)\" data-roles=\"mui-linkbutton\" title=\"查看\"  " +
            "class=\"l-btn l-btn-medium l-btn-normal\" " + btnFun + " >" +
            "        <span class=\"l-btn-left\">\n" +
            "            <span class=\"l-btn-text\">查看</span>\n" +
            "        </span>\n" +
            "</a>";
    };

    this.showVersionDetail = function (versionId) {
        $m('dam/metamanage/standard/dataelement/edit').showVersion(versionId);

    };
});
