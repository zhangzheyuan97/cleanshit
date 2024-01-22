$m('dam/metamanage/standard/integratedquery/pagination', function () {

    var btnObjId = {
        first_id: "#custom-pagination-first",
        prev_id: "#custom-pagination-prev",
        next_id: "#custom-pagination-next",
        last_id: "#custom-pagination-last"
    };
    var pageObj = {
        max: 1,//最大页码
        current: 1,//当前页码
        total: 1,//总条数
        size:20,//当前条数
        first: false,//第一页enable
        prev: false,//前一页enable
        next: false,//下一页enable
        last: false//最后页enable
    };
    var historyParams;
    var historyTypes;

    $(function () {
        pageNumBindEvent();
    });

    this.pageSizeChange = function (e) {
        pageObj.size = $(e)[0].value;
        pageObj.current = 1;
        apiQueryPage(historyParams, historyTypes);
    };

    this.pageReload = function () {
        apiQueryPage(historyParams, historyTypes);
    };

    function pageNumBindEvent() {
        $('#custom-pagination-num').on('input', function (e) {
            //只允许输入数字
            e.target.value = e.target.value.replace(/[^\d]/g, '');
        }).on('keyup', function (e) {
            //回车触发查询
            if (e.keyCode == "13") {
                var cur = $('#custom-pagination-num').val();
                var curNum = 0;
                if (StandardUtils.isNotEmpty(cur)) {
                    curNum = Number(cur);
                }
                if (StandardUtils.equals(curNum, 0) || curNum > pageObj.max || StandardUtils.equals(curNum, pageObj.current)) {
                    var current = pageObj.current;
                    $('#custom-pagination-num').val(current);
                } else {
                    pageObj.current = curNum;
                    apiQueryPage(historyParams, historyTypes);
                }
            }
        }).on('focus', function (e) {
        }).on('blur', function (e) {
            var current = pageObj.current;
            $('#custom-pagination-num').val(current);
        });
    }

    /**
     * first | prev | next | last
     * @param type
     */
    this.gotoPage = function (type) {
        switch (type) {
            case 'first':
                if (pageObj.first) {
                    pageObj.current = 1;
                    apiQueryPage(historyParams, historyTypes);
                }
                break;
            case 'prev':
                if (pageObj.prev) {
                    pageObj.current = pageObj.current - 1;
                    apiQueryPage(historyParams, historyTypes);
                }
                break;
            case 'next':
                if (pageObj.next) {
                    pageObj.current = pageObj.current + 1;
                    apiQueryPage(historyParams, historyTypes);
                }
                break;
            case 'last':
                if (pageObj.last) {
                    pageObj.current = pageObj.max;
                    apiQueryPage(historyParams, historyTypes);
                }
                break;
            default:
                break;
        }
    };

    function disableBtn(key) {
        var e = $(btnObjId[key]);
        e.addClass("l-btn-disabled");
        e.addClass("l-btn-plain-disabled");
    }

    function enableBtn(key) {
        var e = $(btnObjId[key]);
        e.removeClass("l-btn-disabled");
        e.removeClass("l-btn-plain-disabled");
    }

    var resetPageQuery = this.resetPageQuery = function (params, types) {
        historyParams = params;
        historyTypes = types;
        pageObj.current = 1;
        apiQueryPage(params, types);
    };

    function apiQueryPage(params, types) {
        params = getParams(params);
        top$.messager.progress({
            text: '正在加载，请稍候....',
            interval: 500,
            width: 500
        });
        Api.queryPage(params, types, function (result) {
            top$.messager.progress('close');
            enablePaginationBtns(result);
            $m('dam/metamanage/standard/integratedquery').showPageResult(result);
        });
    }


    function getParams(params) {
        params = params || {};
        params['pageSize'] = pageObj.size;
        params['pageNum'] = pageObj.current;
        return params;
    }

    function enablePaginationBtns(pageInfo) {
        pageInfo = pageInfo || {};
        var size = pageObj.size;
        var total = pageInfo['total'] || 0;
        var pageCount = Math.ceil(total / size) || 1;
        var current = pageObj.current;
        $('._pages_li_style').remove();
        $('._pagination_prev').remove();
        $('._pagination_next').remove();
        var ul = $('ul');
        var pagatemp = 0;
        if(pageCount > 5){
            //上一页按钮渲染
            // $('<li></li>').addClass("_pagination_prev").appendTo(ul);
            var li = createElement('li', "_pagination_prev");
            li.addEventListener('click', function () {
                if (pageObj.current > 1) {
                    pageObj.current = pageObj.current - 1;
                    apiQueryPage(historyParams, historyTypes);
                }
            });
            $(li).appendTo(ul);
            //下一页按钮渲染
            // $('<li></li>').addClass("_pagination_next").appendTo(ul);
            li = createElement('li', "_pagination_next");
            li.addEventListener('click', function () {
                if (pageObj.current < pageCount) {
                    pageObj.current = pageObj.current + 1;
                    apiQueryPage(historyParams, historyTypes);
                }
            });
            $(li).appendTo(ul);
            // $('<li>1</li>').addClass("_pages_li_style _pagination_first").attr('data-index', 1).appendTo(ul);
            li = createElement('li', "_pages_li_style");
            li.innerText = 1;
            li.setAttribute('data-index', 1);
            li.addEventListener('click', function () {
                var pageIndex = $(this).attr("data-index");
                pageObj.current = pageIndex;
                apiQueryPage(historyParams, historyTypes);
            });
            $(li).appendTo(ul);
            $('<li>...</li>').addClass("_pages_li_style _pagination_reduce").appendTo(ul);
            pagatemp = 1;
        }
        //默认展示5条
        var between = getBetween(parseInt(current), size, total);
        var betweenList = generateArray(between.min + pagatemp, between.max - pagatemp);
        var active = [];
        for (var i = 0; i < betweenList.length; i++) {
            var li = createElement('li', __spreadArrays(["_pages_li_style"], active));
            li.innerText = betweenList[i].toString();
            li.setAttribute('data-index', betweenList[i]);
            li.addEventListener('click', function () {
                var pageIndex = $(this).attr("data-index");
                pageObj.current = pageIndex;
                apiQueryPage(historyParams, historyTypes);
            });
            $(li).appendTo(ul);
        }
        if(pageCount > 5){
            $('<li>...</li>').addClass("_pages_li_style _pagination_add").appendTo(ul);
            // $('<li></li>').addClass("_pages_li_style _pagination_last").text(pageCount).attr('data-index', pageCount).appendTo(ul);
            li = createElement('li', "_pages_li_style");
            li.innerText = pageCount;
            li.setAttribute('data-index', pageCount);
            li.addEventListener('click', function () {
                var pageIndex = $(this).attr("data-index");
                pageObj.current = pageIndex;
                apiQueryPage(historyParams, historyTypes);
            });
            $(li).appendTo(ul);
        }
        ul.find("[data-index="+parseInt(current)+"]").addClass("_active_1");
        if (total > 0) {
            var start = size * (current - 1) + 1;
            var end = size * current;
            end = end > total ? total : end;
            $('#custom-pagination-total').text('显示' + start + '-' + end + '条，共' + total + '条');
        } else {
            $('#custom-pagination-total').text('没有数据');
        }
    }

    var __spreadArrays = (this && this.__spreadArrays) || function () {
        for (var s = 0, i = 0, il = arguments.length; i < il; i++) s += arguments[i].length;
        for (var r = Array(s), k = 0, i = 0; i < il; i++)
            for (var a = arguments[i], j = 0, jl = a.length; j < jl; j++, k++)
                r[k] = a[j];
        return r;
    };

    function createElement(tag, classList){
        var dom = document.createElement(tag);
        if (classList) {
            if (classList instanceof Array) {
                classList.forEach(function (v) {
                    dom.classList.add(v);
                });
            }
            else {
                dom.classList.add(classList);
            }
        }
        return dom;
    };

    function generateArray(start, end){
        return Array.from(new Array(end + 1).keys()).slice(start);
    };

    function getBetween(index, size, total){
        var betweenPageNumber = 7;
        var pageCount = Math.ceil(total/size);
        // 最小下标
        var min = index - Math.floor(betweenPageNumber / 2);
        // // 最小下标最大值
        if (min > pageCount - betweenPageNumber) {
            min = pageCount - betweenPageNumber + 1;
        }
        // // 最小值
        if (min <= 1)
            min = 1;
        // // 最大下标
        var max = index + Math.floor(betweenPageNumber / 2);
        // 最大下标最小值
        if (max < betweenPageNumber)
            max = betweenPageNumber;
        // 最大值
        if (max > pageCount)
            max = pageCount;
        return { min: min, max: max };
    };
});
