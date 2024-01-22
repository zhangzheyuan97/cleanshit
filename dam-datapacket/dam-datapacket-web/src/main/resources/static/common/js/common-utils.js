var StandardUtils = {
    isBlank: function (value) {
        //fixed 排出 0 / 1  值的影响
        return value === '' || value === null || value === undefined;
    },
    isNotEmpty: function (value) {
        return !StandardUtils.isBlank(value);
    },
    /**
     * 比对两个值是否相等
     * e.g: Utils.equals('0',0) = false; 不同类型的值返回false
     *
     * @param a
     * @param b
     * @returns {boolean}
     */
    equals: function (a, b) {
        if (StandardUtils.isBlank(a) && StandardUtils.isBlank(b)) {
            return true;
        } else if (StandardUtils.isNotEmpty(a) && StandardUtils.isNotEmpty(b)) {
            var aVar = typeof a;
            var bVar = typeof b;
            if (aVar != bVar) {
                return false;
            }
            return a === b;
        }
        return false;
    },
    listIsEmpty: function (value) {
        return !StandardUtils.listIsNotEmpty(value);
    },
    listIsNotEmpty: function (value) {
        value = value || [];
        return value.length > 0;
    },
    /**
     * null 值处理
     * @param value
     * @returns {string}
     */
    fmtBlankValue: function (value) {
        return StandardUtils.isBlank(value) ? '' : value;
    },
    /**
     * Boolean值转译
     * @param value
     * @returns {*}
     */
    fmtBooleanValue: function (value) {
        var options = [true, 1];
        return StandardUtils.isBlank(value) ? '否' : options.indexOf(value) > -1 ? '是' : '否';
    },
    /**
     * 自定义转译
     * @param value
     * @param fmtOpt
     * @returns {*|string}
     */
    fmtValue: function (value, fmtOpt) {
        if (StandardUtils.isBlank(value)) {
            return '';
        }
        fmtOpt = fmtOpt || {};
        return StandardUtils.fmtBlankValue(fmtOpt[value]);
    },
    stringify: function (obj) {
        if (typeof obj === 'string') {
            return obj;
        }
        if (typeof obj === 'object') {
            return JSON.stringify(obj);
        }
        return null;
    },
    parse: function (objStr) {
        if (typeof objStr === 'string') {
            return JSON.parse(objStr);
        }
        if (typeof objStr === 'object') {
            return objStr;
        }
        return null;
    },
    promptMag: function (result) {
        result = result || {};
        var msg = result['message'];
        var success = result['success'];
        if (StandardUtils.isNotEmpty(msg)) {
            top$.messager.promptInfo({
                msg: msg,
                icon: success ? 'successs' : 'error',
            });
        }
    },
    formatter: function (value, row, index) {
        value = value ? value : '';
        return "<p style='cursor: pointer'><span class='bubbling_hint' title='" + value + "'>" + value + "</span></p>";
    },
    dateFmtDay: function (datetime) {
        var vFmt = StandardUtils.dateFmtTime(datetime);
        return StandardUtils.isNotEmpty(vFmt) ? vFmt.substr(0, 10) : vFmt;
    },
    dateFmtTime: function (datetime) {
        if (typeof datetime === "number") {
            function prefix(v) {
                return (v > 9 ? '' : '0') + v;
            }

            var date = new Date(datetime);
            var year = prefix(date.getFullYear());
            var month = prefix(date.getMonth() + 1);
            var day = prefix(date.getDate());
            var hours = prefix(date.getHours());
            var minutes = prefix(date.getMinutes());
            var seconds = prefix(date.getSeconds());
            return year + '-' + month + '-' + day + ' ' + hours + ':' + minutes + ':' + seconds;
        }
        return '';
    },
    arrSplice: function (arr, v) {
        arr = arr || [];
        if (StandardUtils.isNotEmpty(v)) {
            var idx = arr.indexOf(v);
            if (!StandardUtils.equals(idx, -1)) {
                arr.splice(idx, 1);
            }
        }
        return arr;
    },
    arrPush: function (arr, v) {
        arr = arr || [];
        if (StandardUtils.isNotEmpty(v) && StandardUtils.equals(arr.indexOf(v), -1)) {
            arr.push(v);
        }
        return arr;
    },

    timeForMatter: function (value, row, index) {
        if (value) {
            return DateUtils.formatDate(value, "yyyy-MM-dd HH:mm:ss");
        } else {
            return value;
        }
    }
};
function getPathVariable(path, variables) {
    var pathVariable = '';
    if (path === undefined || path === '' || path === null) {
        return pathVariable;
    }
    variables = variables || {};
    var pathValue = JSON.parse(JSON.stringify(variables));
    var pathParams = path.split('/');
    pathParams = pathParams || [];
    if (pathParams.length > 0) {
        for (var idx = 0, length = pathParams.length; idx < length; idx++) {
            var pathParam = pathParams[idx];
            if (pathParam === undefined || pathParam === '' || pathParam === null) {
                continue;
            }
            var pathParamValue = pathParam;
            if (pathParam.indexOf(":") === 0) {
                pathParam = pathParam.substr(1);
                pathParamValue = pathValue[pathParam];
                if (pathParamValue === undefined || pathParamValue === '' || pathParamValue === null) {
                    if (!$.isEmptyObject(pathValue)) {
                        throw new Error('请求参数缺失【' + pathParam + '】');
                    }
                } else {
                    delete pathValue[pathParam];
                    pathVariable += ('/' + pathParamValue);
                }
            } else {
                pathVariable += ('/' + pathParamValue);
            }
        }
    }
    return pathVariable;
}
/**
 * 添加操作菜单参数
 * @param url
 * @returns {string}
 */
function buildUrl(url) {
    var params = {};
    var menuInfo = TempoUtils.getPathLists();
    params['module'] = menuInfo.module.name;
    params['menu'] = menuInfo.menu.name;
    var queryString = '';
    if (url.indexOf('?') < 1) {
        queryString += '?';
    }
    for (var field in params) {
        if (!queryString) {
            queryString += '&'
        }
        var value = params[field] || '';
        queryString += (field + '=' + value);

    }
    return url +　encodeURI(queryString);
}
var Ajax={
    _RESOURCES :{},
    contentType : {
        json: 'application/json',
        form: 'application/x-www-form-urlencoded',
        stream: 'application/octet-stream'
    },
    init : function () {
        return new _AJAX();
    },
    getResources:function (module, child, variableValue) {
        var moduleResource = this._RESOURCES[module];
        if (StandardUtils.isBlank(moduleResource) || $.isEmptyObject(moduleResource)) {
            throw new Error('无效的路径请求资源【' + module + '】');
        }
        var childResource = moduleResource.apiPath[child];
        if (StandardUtils.isBlank(childResource) || $.isEmptyObject(childResource)) {
            throw new Error('无效的路径请求资源【' + module + '.' + child + '】');
        }
        var pathVariable = getPathVariable(childResource, variableValue);
        var path = moduleResource.rootPath + pathVariable;
        path = path.replace(/\/\//g, '/');
        return path;
    },
    setResource :function(moduleResource) {
        if (!(moduleResource === undefined || moduleResource === '' || moduleResource === null)) {
            if ($.isEmptyObject(moduleResource)) {
                throw new Error('无效的模块资源 ');
            }
            for (var module in moduleResource) {
                this._RESOURCES[module] = moduleResource[module];
            }
        }
    },
    form: function (form, url, queryString, onSuccess, onError) {
        var path = url + this.getQueryString(queryString);
        path = path.replace(/\/\//g, '/');
        form.form('submit', {
            url: path,
            type: 'post',
            success: function (data) {
                data = StandardUtils.parse(data);
                onSuccess && $.isFunction(onSuccess) && onSuccess.call(onSuccess, data);
            },
            error: function () {
                top$.messager.progress('close');
                onError && $.isFunction(onError) && onError.call(onError);
            }
        });
    },
    getQueryString: function (params) {
        params = params || {};
        var menuInfo = TempoUtils.getPathLists();
        params['module'] = menuInfo.module.name;
        params['menu'] = menuInfo.menu.name;
        var queryString = '';
        for (var field in params) {
            var value = params[field];
            if (value === undefined || value === '' || value === null) {
                continue;
            }
            if (queryString === '') {
                queryString += '?'
            } else {
                queryString += '&'
            }
            value = encodeURIComponent(value);
            queryString += (field + '=' + value)
        }
        return queryString;
    },
    errorMsg: function (result, msg) {
        if (!result.success) {
            top$.messager.progress('close');

            top$.messager.promptInfo({
                msg: StandardUtils.isNotEmpty(msg) ? msg : result.message,
                icon: 'error',
            });
            return false;
        }
        return true;
    }
};

var _AJAX = function () {
    this.options = {
        url: '',
        method: 'get',
        type: 'json',
        contentType: "application/x-www-form-urlencoded",
    }
};

_AJAX.prototype = {
    contentType: function (contentType) {
        this.options.contentType = contentType;
        return this;
    },
    async: function () {//是否同步
        this.options.async = false;
        return this;
    },
    get: function (url, data) {
        this.options.url = buildUrl(url);
        this.options.method = 'get';
        this.options.data = data;
        this.always();
        $.ajax(this.options);
    },
    post: function (url, data) {
        this.options.url = buildUrl(url);
        this.options.method = 'post';
        if (this.options.contentType === 'application/json') {
            data = JSON.stringify(data);
        }
        this.options.data = data;
        this.always();
        $.ajax(this.options);
    },
    success:function(onSuccess){
        this.options.success=function(data){
            $.isFunction(onSuccess) && onSuccess.call(onSuccess, data.status, data.data, data.message);
        };
        return this;
    },
    error:function(onError){
        this.options.error=function(jqXHR,textStatus,errorThrown){
            try {
                top$.messager.progress('close');
            } catch (e) {
            }
            $.isFunction(onError) && onError.call(onError, jqXHR,textStatus,errorThrown);
        };
        return this;
    },
    always:function(onComplete){
        this.options.complete=function(jqXHR,textStatus){
            try {
                top$.messager.progress('close');
            } catch (e) {
                $.messager.progress('close');
            }
            $.isFunction(onComplete) && onComplete.call(onComplete, jqXHR,textStatus);
        };
        return this;
    }
};

//提示框扩展
$.extend($.Msg, {
    success: function (msg) {
        top$.messager.promptInfo({
            msg: msg,
            icon: 'success'
        });
    },
    error: function (msg) {
        top$.messager.promptInfo({
            msg: msg,
            icon: 'error'
        });
    },
    info: function (msg) {
        top$.messager.promptInfo({
            msg: msg,
            icon: 'info'
        });
    },
    warn: function (msg) {
        top$.messager.promptInfo({
            msg: msg,
            icon: 'warning'
        });
    }
});