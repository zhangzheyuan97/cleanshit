/*
	jQuery 标签插件 	ver 0.1

	https://github.com/28269890/sTags

	DEMO:https://28269890.github.io/sTags/
*/

(function ($) {

    $.fn.sTags = function (options) {
        var o = $.extend({}, $.fn.sTags.defaults, options);
        var id = Date.now() + "" + Math.ceil(Math.random() * 1000);
        //回填已选tag
        if (o.selectedTag.length > 0) {
            for (let i = 0; i < o.selectedTag.length; i++) {
                tagSelectedInput.addFromObject(o.selectedTag[i].tag, true, o.selectedTag[i]);
            }
        }
        if (o.tagName == "") {
            o.tagName = "div"
        }

        var queryArea = $('<div>', {//自定义查询区域div
            "id": "tag-query-area",
            class: o.queryAreaCss
        });
        var tagListClass = o.tagListCSS;
        if (!o.screen) {
            tagListClass = o.tagListReadCss;
        }
        var tagList = $('<div/>', {//定义选择数据的div
            class: tagListClass,
            "tag-list-id": id,
            "id": o.id + 'temp'
        });
        var list = function (target) {//列出数据 target目标div指 定义选择数据的div

            if (o.screen) {//如果启用筛选
                $('#tag-query-area').prepend('<span>所有标签</span>');
                //定义筛选框
                $("<input>", o.screenInput).keyup(function () {
                    var skey = $(this).val();
                    if (skey == "") {
                        $(target + ">" + o.tagName + "[screen]").show();
                    } else {
                        $(target + ">" + o.tagName + "[screen]").hide();
                        $(target + ">" + o.tagName + "[screen*='" + skey + "']").show();
                    }
                }).appendTo("[id=tag-query-area]");
            }


            if (o.data.length > 0) {//如果有标签数据

                for (var i in o.data) {//循环标签数据

                    var attr = {};
                    attr.tagid = o.data[i].id; //定义标签id
                    attr.tagname = o.data[i].tag;//定义标签显示
                    attr.tagInitial = o.data[i].tagInitial;//定义标签首字母
                    attr.tagType = o.data[i].tagType == undefined ? "PUBLIC" : o.data[i].tagType;//定义标签类型
                    if (o.data[i].selected) {
                        attr.class = o.sTagsSelectedCss;
                    }
                    //attr.selected=o.data[i].selected;//定义标签选中状态
                    if (o.screen) { //如果启用筛选 则添加标签属性
                        attr.screen = o.data[i].tag;
                    }

                    var E = $("<" + o.tagName + "/>", attr);
                    var tag_substr = o.data[i].tag.length > o.tagShowLength ? (o.data[i].tag.substring(0, o.tagShowLength) + '...') : o.data[i].tag;
                    if (o.tagHtml) {
                        E.html(o.tagHtml.replace('{name}', o.data[i].tag).replace('{id}', o.data[i].id));
                    } else {
                        E.html('<span title="' + TempoUtils.htmlEscape(o.data[i].tag) + '">' + TempoUtils.htmlEscape(tag_substr) + '</span>');
                    }

                    E.data("fn", o.data[i].fn);

                    E.appendTo(target);
                };
                /*
                let manager_tag_button=$('<button type="button" class="addTagButton">+ 管理标签</button>');
                manager_tag_button.appendTo(target);
                */
            }
            $(target).prepend(o.tagTXT)//添加标签文本

        };


        if ($(this).prop("tagName") == "INPUT") { //如果作用域输入框

            $(this).hide();
            if (o.clearBefore) {
                $('.tagOuterTd #' + o.id + 'temp').remove();
            }
            $(this).after(tagList);
            if (o.screen) {//如果启用筛选
                $(this).after(queryArea);
            }
            list("[tag-list-id=" + id + "]");
        }

    };

    $.fn.sTags.defaults = {
        id: 'meritTagList',
        data: [],//格式:{id:数字,name:文本,screen:筛选文本}
        selectedTag: [],//已选标签
        tagShowLength: 7,//标签显示多少字
        clearBefore: false,
        tagListCSS: "sTags",//列表css
        tagListReadCss: "sTags sTags-onlyRead",
        queryAreaCss: "sTagsqueryArea",//查询区div
        sTagsSelectedCss: "sTags-selected",
        screen: true,//是否启用筛选功能
        screenInput: {
            type: "text",
            size: 8,
            placeholder: "搜索"
        },//筛选输入框属性,
        tagTXT: "",//标签列表前缀
        tagName: "",//标签列表使用的html标签，默认为div，如要改为div和a之外的其他标签则需要修改css
        tagHtml: "",//自定义标签列表中的html内容。{name} 替换为 tag.name {id}将转换为 tag.id
    };
})(window.jQuery);

