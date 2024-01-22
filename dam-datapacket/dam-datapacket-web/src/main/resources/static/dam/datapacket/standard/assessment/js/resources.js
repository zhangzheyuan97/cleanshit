//=======相关访问路径声明=======start=========
/**
 *后端提供的API的路径信息
 * rootPath 根节点信息（可直接定为到接口位置的Controller）
 * childPath 相应根节点下的所有API路径
 * :id 接口API(VariableValue部分)占位符
 */
var moduleResource = {
    assessment: {
        rootPath: '/dam-metamanage/api/metamanage/standard/assessment/',
        apiPath: {
            health: 'health/:module',
            taskAdd: 'task-add',
            taskPage: 'task-page',
            reportPage: 'report-page',
            logPage: 'log-page',
            taskOne: 'task-get',
            taskUpdate: 'task-update',
            taskDel: 'task-del',
            standardList: 'standard-unused',
            startTask: 'task-run',
            report: 'report-pager',
            isoPage: 'iso-detail-page',
            isoList: 'iso-detail-list'
        },
        viewPath: {
            pagerReport: 'view/pagerReport',
            selectedTask: 'view/selectedTask',
            taskStyle: 'view/taskStyle'
        },
        getView: function (key) {
            return moduleResource.assessment.rootPath + moduleResource.assessment.viewPath[key];
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
    getTaskPage: function (container, queryString, options) {
        var url = Ajax.getResources('assessment', 'taskPage', {});
        return Ajax.datagrid(container, url, queryString, options);
    },
    getReportPage: function (container, queryString, options) {
        var url = Ajax.getResources('assessment', 'reportPage', {});
        return Ajax.datagrid(container, url, queryString, options);
    },
    getLogPage: function (container, queryString, options) {
        var url = Ajax.getResources('assessment', 'logPage', {});
        return Ajax.datagrid(container, url, queryString, options);
    },
    addTasks: function (payload, callback) {
        var url = Ajax.getResources('assessment', 'taskAdd', {});
        return Ajax.post(url, payload, {}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null, null);
    },
    getStandardList: function (callback) {
        var url = Ajax.getResources('assessment', 'standardList', {});
        return Ajax.post(url, {}, {}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null, null);
    },
    startTaskById: function (taskId, callback) {
        var url = Ajax.getResources('assessment', 'startTask', {});
        return Ajax.post(url, {}, {taskId: taskId}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null, null);
    },
    getReportResult: function (reportId, callback) {
        var url = Ajax.getResources('assessment', 'report', {});
        return Ajax.post(url, {}, {reportId: reportId}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null, null);
    },
    getIsoDetailPage: function (param, callback) {
        var url = Ajax.getResources('assessment', 'isoPage', {});
        return Ajax.post(url, {}, param, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null, null);
    },
    getIsoDetailList: function (isoId, callback) {
        var url = Ajax.getResources('assessment', 'isoList', {});
        return Ajax.post(url, {}, {isoId: isoId}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null, null);
    },
    delTaskByIds: function (taskIds, callback) {
        var url = Ajax.getResources('assessment', 'taskDel', {});
        return Ajax.post(url, taskIds, {}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null, null);
    },
    updateTaskConf: function (conf, callback) {
        var url = Ajax.getResources('assessment', 'taskUpdate', {});
        return Ajax.post(url, conf, {}, function (result) {
            var errorMsg = Ajax.errorMsg(result, '');
            return errorMsg && callback && $.isFunction(callback) && callback.call(callback, result.data);
        }, null, null);
    }
};
