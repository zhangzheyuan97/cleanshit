//=======相关访问路径声明=======start=========
/**
 *后端提供的API的路径信息
 * rootPath 根节点信息（可直接定为到接口位置的Controller）
 * childPath 相应根节点下的所有API路径
 * :id 接口API(VariableValue部分)占位符
 */
var moduleResource = {
    enumItem: {
        rootPath: '/dam-metamanage/api/metamanage/standard/enum-item/',
        apiPath: {
            typePage: 'type-page',
            addType: 'type-add',
            getTypeOne: 'type/:id',
            typeDel: 'type-del',
            updateType: 'type-update',
            typeUsed: 'type-used',
            getDicRe: 'type-dic-re',
            saveDicRe: 'type-dic/:enumId',
            itemTree: 'item-tree',
            addItem: 'item-add',
            delItem: 'item-del',
            getItem: 'item/:itemId',
            updateItem: 'item-update',
            itemPage: 'item-page',
            upload: 'item-upload/:enumId',
            download: 'item-download',
            itemExtract: 'dic-extract',
            excelTemp: 'temp-download',
            valueUsed: 'used/:type/:label',
            preDelItem: 'pre-item-del'
        },
        viewPath: {
            add: 'view/addDataType',
            edit: 'view/editDataType',
            dic: 'view/dicenumitem',
            addEnum: 'view/addEnumitem',
            editEnum: 'view/editEnumitem',
            importData: 'view/importData',
            itemExtract: 'view/itemExtract'
        },
        getView: function (key) {
            return moduleResource.enumItem.rootPath + moduleResource.enumItem.viewPath[key];
        }
    },
    dic: {
        rootPath: '/dam-metamanage/api/metamanage/standard/dic/',
        apiPath: {
            list: 'dic-list'
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
     * API-1-1.枚举项分页查询
     */
    getTypePage: function (container, queryString, options) {
        var url = Ajax.getResources('enumItem', 'typePage', {});
        return Ajax.datagrid(container, url, queryString, options);
    },
    /**
     * API-1-1.枚举项分页查询 非datagrid查询
     */
    postTypePage: function (param, callback) {
        var url = Ajax.getResources('enumItem', 'typePage', {});
        return Ajax.post(url, {}, param, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null, null);
    },
    /**
     * API-1-2.枚举项新增
     */
    addType: function (payload, callback) {
        var url = Ajax.getResources('enumItem', 'addType', {});
        return Ajax.post(url, payload, {}, function (result) {
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
     * API-1-4.枚举项更新
     */
    updateType: function (payload, callback) {
        var url = Ajax.getResources('enumItem', 'updateType', {});
        return Ajax.post(url, payload, {}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null, null);
    },
    /**
     *API-1-5.枚举项移除
     * @param id 枚举项ID
     */
    delTypeById: function (id, callback) {
        var url = Ajax.getResources('enumItem', 'typeDel', {});
        return Ajax.post(url, [id], {}, function (result) {
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
     * API-1-7.枚举项关联字典
     * @param enumId 枚举项ID
     * @param payload [dicId]
     */
    saveDicRe: function (enumId, payload, callback) {
        var url = Ajax.getResources('enumItem', 'saveDicRe', {enumId: enumId});
        return Ajax.post(url, payload, {enumId: enumId}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null, null);
    },
    /**
     * API-1-8.枚举项使用状态
     * @param enumId 枚举项ID
     */
    checkTypeIsUsed: function (enumId, dicType, callback) {
        var url = Ajax.getResources('enumItem', 'typeUsed', {});
        return Ajax.post(url, [enumId], {dicType: dicType}, function (result) {
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
     * API-1-10.枚举值新增
     */
    addItem: function (payload, callback) {
        var url = Ajax.getResources('enumItem', 'addItem', {});
        return Ajax.post(url, payload, {}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null, null);
    },
    /**
     * API-1-11.枚举值批量删除
     */
    delItem: function (ids, callback) {
        var url = Ajax.getResources('enumItem', 'delItem', {});
        return Ajax.post(url, ids, {}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null, null);
    },
    /**
     * API-1-11-1.枚举值批量删除预判断
     */
    preDelItem: function (ids, callback) {
        var url = Ajax.getResources('enumItem', 'preDelItem', {});
        return Ajax.post(url, ids, {}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null, null);
    },
    /**
     * API-1-12.枚举值获取
     * @param enumId 枚举值ID
     */
    getItemById: function (itemId, callback) {
        var url = Ajax.getResources('enumItem', 'getItem', {itemId: itemId});
        return Ajax.post(url, {}, {}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null, null);
    },
    /**
     * API-1-13.枚举值获取
     * @param enumId 枚举值ID
     */
    updateItem: function (payload, callback) {
        var url = Ajax.getResources('enumItem', 'updateItem', {});
        return Ajax.post(url, payload, {}, function (result) {
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
     * 文件下载
     * @param enumId 枚举值ID
     */
    download: function (form, enumId, callback) {
        var url = Ajax.getResources('enumItem', 'download', {});
        return Ajax.form(form, url, {enumId: enumId}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null);
    },
    /**
     * 文件上传
     * @param enumId 枚举值ID
     */
    upload: function (form, enumId, callback) {
        var url = Ajax.getResources('enumItem', 'upload', {enumId: enumId});
        return Ajax.form(form, url, {}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null);
    },
    /**
     * 字典基础信息列表获取
     * @param struct 层级
     */
    getDicBasicList: function (treeStruct, callback) {
        var url = Ajax.getResources('dic', 'list', {});
        return Ajax.post(url, {}, {struct: treeStruct ? 'TREE' : 'LIST'}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null, null);
    },
    /**字典值全量提取
     *
     * @param dicId 字典ID
     * @param enumId  枚举项ID
     */
    itemExtractFormDic: function (dicId, enumId, callback) {
        var url = Ajax.getResources('enumItem', 'itemExtract', {});
        return Ajax.post(url, {}, {dicId: dicId, enumId: enumId}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null, null);
    },
    /**
     *模板文件下载
     * @param enumId 枚举项ID
     */
    downloadExcelTemp: function (form, enumId, callback) {
        var url = Ajax.getResources('enumItem', 'excelTemp', {});
        return Ajax.form(form, url, {enumId: enumId}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null);
    },
    /**
     * 枚举项/枚举值值重复检查
     * @param params {id:'枚举项ID/枚举值ID',value:'需要校验值',enumId:'枚举项ID，type=item时必填'}
     * @param variables {type:'枚举项/枚举值',label:'name/code'}
     * @param callback
     * @returns {*|void}
     */
    checkValueUsed: function (params, variables, callback) {
        var url = Ajax.getResources('enumItem', 'valueUsed', variables);
        return Ajax.post(url, {}, params, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null, null);
    }
};
