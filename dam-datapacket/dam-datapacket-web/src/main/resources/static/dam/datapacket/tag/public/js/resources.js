//=======相关访问路径声明=======start=========
/**
 *后端提供的API的路径信息
 * rootPath 根节点信息（可直接定为到接口位置的Controller）
 * childPath 相应根节点下的所有API路径
 * :id 接口API(VariableValue部分)占位符
 */
var moduleResource = {
    publictag: {
        rootPath: '/dam-metamanage/api/metamanage/publictag/',
        apiPath: {
            getAll: 'get-all',
            add: 'add',
            delete: 'delete'
        }
    },
    getApi: function (key) {
        return moduleResource.publictag.rootPath + moduleResource.publictag.apiPath[key];
    }
};

/**
 *后端提供的API,统一管理，统一维护
 */
const Api = {
    isBlank: function (value) {
        //fixed 排出 0 / 1  值的影响
        return value === '' || value === null || value === undefined;
    },
    errorMsg: function (result, msg) {
        if (!result.success) {
            top$.messager.progress('close');

            top$.messager.promptInfo({
                msg: !Api.isBlank(msg) ? msg : result.message,
                icon: 'error',
            });
            return false;
        }
        return true;
    },
    errorCallbackMsg: function (msg) {
        top$.messager.progress('close');

        top$.messager.promptInfo({
            msg: msg,
            icon: 'error',
        });
    },
    /**
     * GET请求
     * @param url
     * @param data
     * @param successCallback
     * @param errorCallback
     * @constructor
     */
    GET: function (url, data, successCallback, errorCallback) {
        $.ajax({
            url: url,
            method: 'GET',
            dataType: 'JSON',
            data: data,
            success: successCallback,
            error: errorCallback
        });
    },
    /**
     * POST请求
     * @param url
     * @param data
     * @param successCallback
     * @param errorCallback
     * @constructor
     */
    POST: function (url, data, successCallback, errorCallback) {
        $.ajax({
            url: url,
            method: 'POST',
            dataType: 'JSON',
            data: data,
            success: successCallback,
            error: errorCallback
        });
    },
    /**
     * PUT请求
     * @param url
     * @param data
     * @param successCallback
     * @param errorCallback
     * @constructor
     */
    PUT: function (url, data, successCallback, errorCallback) {
        $.ajax({
            url: url,
            method: 'PUT',
            dataType: 'JSON',
            data: data,
            success: successCallback,
            error: errorCallback
        });
    },
    /**
     * Delete请求
     * @param url
     * @param data
     * @param successCallback
     * @param errorCallback
     * @constructor
     */
    DELETE: function (url, data, successCallback, errorCallback) {
        $.ajax({
            url: url,
            method: 'DELETE',
            dataType: 'JSON',
            data: data,
            success: successCallback,
            error: errorCallback
        });
    },
    getTagGroup: function (callback) {
        let url = moduleResource.getApi("getAll");
        return Api.GET(url, {tagQuery: ""}, function (result) {
            var errorMsg = Api.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, function (result) {
            Api.errorCallbackMsg('获取标签库信息失败！');
        });
    },
    delPublictag: function (id, callback) {
        let url = moduleResource.getApi("delete");
        return Api.POST(url, {"id": id}, function (result) {
            var errorMsg = Api.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, function () {
            Api.errorCallbackMsg("删除标签失败！");
        });
    },
    savePublictag: function (tag, callback) {
        let url = moduleResource.getApi("add");
        return Api.POST(url, {"tag": tag}, function (result) {
            var errorMsg = Api.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, function () {
            Api.errorCallbackMsg("新增标签失败！");
        });
    }


};
