//=======相关访问路径声明=======start=========
/**
 *后端提供的API的路径信息
 * rootPath 根节点信息（可直接定为到接口位置的Controller）
 * childPath 相应根节点下的所有API路径
 * :id 接口API(VariableValue部分)占位符
 */
var moduleResource = {
    graph: {
        rootPath: '/dam-metamanage/api/metamanage/standard/graph/',
        apiPath: {
            health: 'health/:module',
            statistic: 'standard-statistic',
            element: 'element-status',
            down : 'standard-down',
            top : 'report-top',
            range: 'score-range'
        },
        viewPath: {
            add: 'view/add'
        },
        getView: function (key) {
            return moduleResource.graph.rootPath + moduleResource.graph.viewPath[key];
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
        var url = Ajax.getResources('graph', 'health', {module: 'query'});
        return Ajax.post(url, {}, queryString, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null, null);
    },
    /**
     * API-7-1.基础统计/各标准统计分析
     * @param callback
     * @returns {*|void}
     */
    statistic: function (callback) {
        var url = Ajax.getResources('graph', 'statistic', {});
        return Ajax.post(url, {}, {}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null, null);
    },
    /**
     * API-7-2.数据元标准状态
     * @param callback
     * @returns {*|void}
     */
    element: function (callback) {
        var url = Ajax.getResources('graph', 'element', {});
        return Ajax.post(url, {}, {}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null, null);
    },
    /**
     * API-7-3.标准下发统计
     * @param callback
     * @returns {*|void}
     */
    down: function (top, callback) {
        var url = Ajax.getResources('graph', 'down', {});
        return Ajax.post(url, {}, {top:top}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null, null);
    },
    /**
     * API-7-4.评分Top10统计
     * @param top
     * @param callback
     * @returns {*|void}
     */
    top: function (top, callback) {
        var url = Ajax.getResources('graph', 'top', {});
        return Ajax.post(url, {}, {top:top}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null, null);
    },

    /**
     * API-7-5.评分分布
     * @param type
     * @param callback
     * @returns {*|void}
     */
    range: function (type, callback) {
        var url = Ajax.getResources('graph', 'range', {});
        return Ajax.post(url, {}, {type:type}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null, null);
    }
};
