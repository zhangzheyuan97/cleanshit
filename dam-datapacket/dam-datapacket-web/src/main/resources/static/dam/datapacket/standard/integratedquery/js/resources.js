//=======相关访问路径声明=======start=========
/**
 *后端提供的API的路径信息
 * rootPath 根节点信息（可直接定为到接口位置的Controller）
 * childPath 相应根节点下的所有API路径
 * :id 接口API(VariableValue部分)占位符
 */
var moduleResource = {
    query: {
        rootPath: '/dam-metamanage/api/metamanage/standard/integrated-query/',
        apiPath: {
            page: 'page'
        },
        viewPath: {
            add: 'view/add',
            element: 'view/elementInfo',
            enum: 'view/enumInfo',
            file: 'view/fileInfo'
        },
        getView: function (key) {
            return moduleResource.query.rootPath + moduleResource.query.viewPath[key];
        }
    },
    element: {
        rootPath: '/dam-metamanage/api/metamanage/standard/data-element/',
        apiPath: {
            versionDetail: 'version-detail',
            getElStandard: 'standard-list'
        }
    },
    enumItem: {
        rootPath: '/dam-metamanage/api/metamanage/standard/enum-item/',
        apiPath: {
            getTypeOne: 'type/:id',
            itemTree: 'item-tree',
            itemCount: 'item-count',
            itemPage: 'item-page',
            getDicRe: 'type-dic-re',
        }
    },
    file: {
        rootPath: '/dam-metamanage/api/metamanage/standard/file/',
        apiPath: {
            getFileById : 'file-by-id',
            downloadUrl: 'file-download'
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
    queryPage: function (params, types, callback) {
        params = params || {};
        if (StandardUtils.listIsNotEmpty(types)) {
            params['types'] = types.join(",");
        }
        var url = Ajax.getResources('query', 'page', {});
        return Ajax.post(url, {}, params, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null, null);
    },

    /**
     * 数据源标准版本详情获取
     * @param versionId 数据元版本ID
     */
    getElementVersionDetail: function (versionId, callback) {
        var url = Ajax.getResources('element', 'versionDetail', {});
        return Ajax.post(url, {}, {versionId: versionId}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            var data = result.data || {};
            data['configEnum'] = StandardUtils.isNotEmpty(data.configEnumId);
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, data);
        }, null, null);
    },
    getElStandard: function (elementId, callback) {
        var url = Ajax.getResources('element', 'getElStandard', {});
        return Ajax.post(url, {}, {elementId: elementId}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null, null);
    },
    /**
     *
     * API-1-3.枚举项获取
     * @param id 枚举项ID
     */
    getTypeOne: function (id, callback) {
        var url = Ajax.getResources('enumItem', 'getTypeOne', {id: id});
        return Ajax.post(url, {}, {}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null, null);
    },
    /**
     * API-1-9.枚举值树列表
     * @param enumId 枚举项ID
     */
    getItemTree: function (enumId, callback) {
        var url = Ajax.getResources('enumItem', 'itemTree', {});
        return Ajax.post(url, {}, {enumId: enumId}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null, null);
    },
    /**
     * API-1-14.枚举值获取
     * @param enumId 枚举值ID
     */
    getItemPage: function (container, queryString, options) {
        var url = Ajax.getResources('enumItem', 'itemPage', {});
        return Ajax.datagrid(container, url, queryString, options);
    },
    /**
     * API-1-9.枚举值树列表
     * @param enumId 枚举项ID
     */
    getItemCount: function (enumId, parentId, callback) {
        var url = Ajax.getResources('enumItem', 'itemCount', {});
        return Ajax.post(url, {}, {enumId: enumId, parentId: parentId}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null, null);
    },
    /**
     * API-1-6.获取数据字典/获取已关联字典
     * @param enumId 枚举项ID
     */
    getDicBindRe: function (enumId, callback) {
        var url = Ajax.getResources('enumItem', 'getDicRe', {});
        return Ajax.post(url, {}, {enumId: enumId}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null, null);
    },
    /**
     * 根据文件id获取文件信息
     * @param fileId
     * @param callback
     * @returns {*|void}
     */
    getFileById: function (fileId, callback) {
        var url = Ajax.getResources('file', 'getFileById', {});
        return Ajax.post(url, {}, {fileId: fileId}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null, null);
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
};
