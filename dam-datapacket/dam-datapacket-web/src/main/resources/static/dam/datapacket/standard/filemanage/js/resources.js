//=======相关访问路径声明=======start=========
/**
 *后端提供的API的路径信息
 * rootPath 根节点信息（可直接定为到接口位置的Controller）
 * childPath 相应根节点下的所有API路径
 * :id 接口API(VariableValue部分)占位符
 */
var moduleResource = {
    file: {
        rootPath: '/dam-metamanage/api/metamanage/standard/file/',
        apiPath: {
            pageUrl: 'file-page',
            delUrl: 'file-del',
            updateUrl: 'file-update',
            downloadUrl: 'file-download',
            uploadUrl: 'file-upload',
            usedCheckUrl: 'used/:filed'
        },
        viewPath: {
            operate: 'view/operateFile'
        },
        getView: function (key) {
            return moduleResource.file.rootPath + moduleResource.file.viewPath[key];
        }
    }
};
//=======相关访问路径声明=======end=========
$(function () {
    setModuleResource(moduleResource);
});
/**
 *后端提供的API,统一管理，统一维护
 */
var Api = {
    /**
     *文件分页查询
     * @param param 查询条件
     */
    getFilePage: function (container, params, options) {
        var url = Ajax.getResources('file', 'pageUrl', {});
        return Ajax.datagrid(container, url, params, options);
    },
    /**
     *批量删除
     * @param param 查询条件
     */
    deleteFiles: function (ids, callback) {
        var url = Ajax.getResources('file', 'delUrl', {module: 'file'});
        return Ajax.post(url, ids, {}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null, null);
    },
    /**
     *文件编辑
     */
    updateFile: function (form, callback) {
        var url = Ajax.getResources('file', 'updateUrl', {});
        return Ajax.form(form, url, {}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null);
    },
    /**
     *标准文件上传新增
     */
    uploadFile: function (form, callback) {
        var url = Ajax.getResources('file', 'uploadUrl', {});
        return Ajax.form(form, url, {}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null);
    },
    /**
     *单文件下载
     * @param fileId 文件ID
     */
    downloadFile: function (form, fileId, callback) {
        var url = Ajax.getResources('file', 'downloadUrl', {});
        return Ajax.form(form, url, {fileId: fileId}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null);
    },
    /**
     *文件名和文件编码是否已被使用检查
     *
     * @param filed name|code
     * @param params    {id:'文件ID(选填)',value:'被校验值'}
     */
    checkValueIsUsed: function (filed, params, callback) {
        var url = Ajax.getResources('file', 'usedCheckUrl', {filed: filed});
        return Ajax.post(url, {}, params, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null, null);
    }
};
