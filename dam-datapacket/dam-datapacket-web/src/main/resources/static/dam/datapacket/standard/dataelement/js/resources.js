//=======相关访问路径声明=======start=========
/**
 *后端提供的API的路径信息
 * rootPath 根节点信息（可直接定为到接口位置的Controller）
 * childPath 相应根节点下的所有API路径
 * :id 接口API(VariableValue部分)占位符
 */
var moduleResource = {
    element: {
        rootPath: '/dam-metamanage/api/metamanage/standard/data-element/',
        apiPath: {
            health: 'health/:module',
            getTypes: 'type-list',
            checkUsed: 'pre-type-del',
            delTypes: 'type-del',
            saveTypes: 'type-save',
            saveElements: 'element-save',
            getElements: 'element-page',
            deprecatedElement: 'element-deprecated',
            deleteElements: 'element-del',
            versions: 'version-list',
            versionDetail: 'version-detail',
            tempFile: 'temp-download',
            upload: 'element-upload',
            download: 'element-download',
            startTask: 'config-task',
            publishTask: 'publish-task',
            addElStandard: 'standard-save',
            rmElStandard: 'standard-cancel',
            getElStandard: 'standard-list',
            submitTask: 'submit-task'
        },
        viewPath: {
            add: 'view/addElement',
            edit: 'view/editElement',
            upload: 'view/uploadData',
            typeManage: 'view/typeManage',
            versionManage: 'view/versionManage',
            standardDown: 'view/standardDown',
            btnReEdit: 'view/btnReEditElement',
            view: 'view/view'
        },
        getView: function (key) {
            return moduleResource.element.rootPath + moduleResource.element.viewPath[key];
        }
    },
    databaseManage: {
        rootPath: '/dam-metamanage/api/metamanage/datasource/database/',
        apiPath: {
            list: 'database-list'
        }
    },
    modelManage: {
        rootPath: '/dam-metamanage/api/metamanage/model/manage/',
        apiPath: {
            effectModelList: 'getModelInfoByCondition',
            effectFieldList: 'modelVerFieldListByModelId'
        }
    },
    enumItem: {
        rootPath: '/dam-metamanage/api/metamanage/standard/enum-item/',
        apiPath: {
            typeList: 'type-list',
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
     * 数据元分类查询
     * @param unFiled 是否生成未分类 true/false
     */
    getTypes: function (unFiled, callback) {
        var url = Ajax.getResources('element', 'getTypes', {});
        return Ajax.post(url, {}, {unFiled: unFiled}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null, null);
    },
    /**
     * 分类批量删除预判断
     */
    checkUsed: function (ids, callback) {
        var url = Ajax.getResources('element', 'checkUsed', {});
        return Ajax.post(url, ids, {}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null, null);
    },
    /**
     * 分类批量删除预判断
     */
    delTypes: function (ids, callback) {
        var url = Ajax.getResources('element', 'delTypes', {});
        return Ajax.post(url, ids, {}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null, null);
    },
    /**
     * 数据元分类批量保存
     * @param payload 见API文档
     */
    saveTypes: function (payload, callback) {
        var url = Ajax.getResources('element', 'saveTypes', {});
        return Ajax.post(url, payload, {}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null, null);
    },
    /**
     * 新增/编辑数据元
     * @param payload 见API文档
     */
    saveElements: function (payload, callback) {
        var url = Ajax.getResources('element', 'saveElements', {});
        return Ajax.post(url, payload, {}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null, null);
    },
    /**
     *数据元分页查询
     * @param params 见API文档
     */
    getElements: function (id, params, options) {
        var url = Ajax.getResources('element', 'getElements', {});
        return Ajax.datagrid(id, url, params, options);
    },
    /**
     * 标准元废止
     * @param elementId  数据元ID
     */
    deprecatedElement: function (elementId, callback) {
        var url = Ajax.getResources('element', 'deprecatedElement', {});
        return Ajax.post(url, {}, {elementId: elementId}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null, null);
    },
    /**
     * 批量删除
     * @param elementIds 待删除的ID列表
     */
    deleteElements: function (elementIds, callback) {
        var url = Ajax.getResources('element', 'deleteElements', {});
        return Ajax.post(url, elementIds, {}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null, null);
    },
    /**
     * 版本列表获取
     * @param elementId 数据元ID
     */
    getVersions: function (elementId, callback) {
        var url = Ajax.getResources('element', 'versions', {});
        return Ajax.post(url, {}, {elementId: elementId}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null, null);
    },
    /**
     * 版本详情获取
     * @param versionId 数据元版本ID
     */
    getVersionDetail: function (versionId, callback) {
        var url = Ajax.getResources('element', 'versionDetail', {});
        return Ajax.post(url, {}, {versionId: versionId}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            var data = result.data || {};
            data['configEnum'] = StandardUtils.isNotEmpty(data.configEnumId);
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, data);
        }, null, null);
    },
    /**
     * 获取数据元模型文件
     */
    downloadTempFile: function (form, callback) {
        var url = Ajax.getResources('element', 'tempFile', {});
        return Ajax.form(form, url, {}, {}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null, null);
    },
    /**
     * 文件上传
     * @param enumId 枚举值ID
     */
    uploadElement: function (form, callback) {
        var url = Ajax.getResources('element', 'upload', {});
        return Ajax.form(form, url, {}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null);
    },
    /**
     * 下载数据元
     */
    downloadElement: function (form, callback) {
        var url = Ajax.getResources('element', 'download', {});
        return Ajax.form(form, url, {}, {}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null, null);
    },
    /**
     * 提交审核
     * @param versionId 数据元版本ID
     */
    startApproval: function (versionIds, callback) {
        var url = Ajax.getResources('element', 'startTask', {});
        return Ajax.post(url, versionIds, {}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null, null);
    },
    /**
     * 发布数据元
     * @param versionId 数据元版本ID
     */
    publishApproval: function (versionIds, callback) {
        var url = Ajax.getResources('element', 'publishTask', {});
        return Ajax.post(url, versionIds, {}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null, null);
    },

    /**
     * 发布数据元申请
     * @param versionId 数据元版本ID
     */
    submitTask: function (versionIds, callback) {
        var url = Ajax.getResources('element', 'submitTask', {});
        return Ajax.post(url, versionIds, {}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null, null);
    },

    /**
     * 有效数据源获取
     */
    getEffectDatabase: function (callback) {
        var filterDbs = ['hbase', 'mongodb'];
        var param = {
            status: true,
            filterDbs: JSON.stringify(filterDbs)
        };
        var url = Ajax.getResources('databaseManage', 'list', {});
        return Ajax.post(url, {}, param, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null, null);
    },
    /**
     * 根据数据源获取有效的模型列表
     * @param databaseId 数据源Id
     */
    getEffectModel: function (databaseId, callback) {
        var payload = {
            dbId: databaseId,
            filterDataTypeStopStatus: true,
            filterModelStopStatus: true
        };
        var url = Ajax.getResources('modelManage', 'effectModelList', {});
        return Ajax.post(url, payload, {}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null, null);
    },
    /**
     * 根据模型获取有效的属性列表
     * @param modelId 模型ID
     */
    getEffectField: function (modelId, callback) {
        var url = Ajax.getResources('modelManage', 'effectFieldList', {});
        return Ajax.post(url, {}, {modelId: modelId}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null, null);
    },
    saveElStandard: function (payload, callback) {
        var url = Ajax.getResources('element', 'addElStandard', {});
        return Ajax.post(url, payload, {}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null, null);
    },
    delElStandard: function (ids, callback) {
        var url = Ajax.getResources('element', 'rmElStandard', {});
        return Ajax.post(url, ids, {}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
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
     * 枚举项列表查询
     * @param {keyword:'',structure:''} keyword   模糊查询条件 |structure 层次结构 null：全部,true:层级结构,false:非层级结构
     */
    getEnumTypeList: function (param, callback) {
        var url = Ajax.getResources('enumItem', 'typeList', {});
        return Ajax.post(url, {}, {}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null, null);
    }
};
