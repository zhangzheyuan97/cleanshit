//=======相关访问路径声明=======start=========
/**
 *后端提供的API的路径信息
 * rootPath 根节点信息（可直接定为到接口位置的Controller）
 * childPath 相应根节点下的所有API路径
 */
const moduleResource = {
    approval: {
        rootPath: '/dam-metamanage/api/metamanage/',
        apiPath: {
            getResourceToSelectPublicTag: 'publictag/get-to-select-tags',
            getResourceToSelectPrivateTag: 'privatetag/get-to-select-tags',
            getResourceSelectedPublicTag: 'publictag/get-selected-tags',
            getResourceSelectedPrivateTag: 'privatetag/get-selected-tags',
            saveResourcePublicTag: 'publictag/save-resource-tags',
            saveResourcePrivateTag: 'privatetag/save-resource-tags',
            deleteResourceAllTags: 'publictag/delete-resource-alltags'
        }
    },
    getApi: function (key) {
        return moduleResource.approval.rootPath + moduleResource.approval.apiPath[key];
    }
};
/**
 *后端提供的API,统一管理，统一维护
 */
const Api = {
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
    /**
     * 保存资源的公共标签
     * @param data
     * @param successCallback
     * @param errorCallback
     */
    saveResourcePublicTag: function (data, successCallback, errorCallback) {
        let url = moduleResource.getApi("saveResourcePublicTag");
        Api.POST(url, data, successCallback, errorCallback);
    },
    /**
     * 保存资源的私有标签
     * @param data
     * @param successCallback
     * @param errorCallback
     */
    saveResourcePrivateTag: function (data, successCallback, errorCallback) {
        let url = moduleResource.getApi("saveResourcePrivateTag");
        Api.POST(url, data, successCallback, errorCallback);
    },
    /**
     * 获取资源已选的公共标签
     * @param data
     * @param successCallback
     * @param errorCallback
     */
    getResourceSelectedPublicTag: function (data, successCallback, errorCallback) {
        let url = moduleResource.getApi("getResourceSelectedPublicTag");
        Api.GET(url, data, successCallback, errorCallback);
    },
    /**
     * 获取资源已选中的私有标签
     * @param data
     * @param successCallback
     * @param errorCallback
     */
    getResourceSelectedPrivateTag: function (data, successCallback, errorCallback) {
        let url = moduleResource.getApi("getResourceSelectedPrivateTag");
        Api.GET(url, data, successCallback, errorCallback);
    },
    /**
     * 获取资源待选的公共标签
     * @param data
     * @param successCallback
     * @param errorCallback
     */
    getResourceToSelectPublicTag: function (data, successCallback, errorCallback) {
        let url = moduleResource.getApi("getResourceToSelectPublicTag");
        Api.GET(url, data, successCallback, errorCallback);
    },
    /**
     * 获取资源待选的私有标签
     * @param data
     * @param successCallback
     * @param errorCallback
     */
    getResourceToSelectPrivateTag: function (data, successCallback, errorCallback) {
        let url = moduleResource.getApi("getResourceToSelectPrivateTag");
        Api.GET(url, data, successCallback, errorCallback);
    },
    /**
     * 删除资源所有的标签，包括公共标签和私有标签
     * @param data
     * @param successCallback
     * @param errorCallback
     */
    deleteResourceAllTags: function (data, successCallback, errorCallback) {
        let url = moduleResource.getApi("deleteResourceAllTags");
        Api.POST(url, data, successCallback, errorCallback);
    }

};