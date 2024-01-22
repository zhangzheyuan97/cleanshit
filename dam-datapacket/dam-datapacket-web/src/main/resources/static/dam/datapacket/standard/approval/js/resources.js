//=======相关访问路径声明=======start=========
/**
 *后端提供的API的路径信息
 * rootPath 根节点信息（可直接定为到接口位置的Controller）
 * childPath 相应根节点下的所有API路径
 * :id 接口API(VariableValue部分)占位符
 */
var moduleResource = {
    approval: {
        rootPath: '/dam-metamanage/api/metamanage/standard/approval/',
        apiPath: {
            health: 'health/:module'
        },
        viewPath: {
            add: 'view/add'
        },
        getView: function (key) {
            return moduleResource.approval.rootPath + moduleResource.approval.viewPath[key];
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
     * temp:心跳检查
     *
     * @param payload
     * @param variableValue
     * @param queryString
     * @param callback
     * @returns {*|void}
     */
    health: function (queryString, callback) {
        var url = Ajax.getResources('approval', 'health', {module: 'approval'});
        return Ajax.post(url, {}, queryString, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null, null);
    }
};
