$m('dam/metamanage/publictag', function () {
    var addDialog;
    var modulePath = "/dam-metamanage/api/metamanage/publictag";
    var addWinPath = modulePath + "/view/addTag";



    this.rmShow = function (event) {
        $(event).find('div').show();
    };

    this.rmHide = function (event) {
        $(event).find('div').hide();
    };

    this.rmBtn = function (event, val) {
        //如果一个字母只剩最后一个，删除之后就空了，则将这个字母也去掉
        if( $(event).parent().parent().parent().parent()[0].childElementCount==2){
            $(event).parent().parent().parent().parent()[0].remove();
        }else{
            $(event).parent().parent().parent()[0].remove();
        }
        Api.delPublictag(val, function (data) {
            loadPageTagData();
        })
    };
    $(function () {
        loadPageTagData();
    });

    /**
     * 加载页面标签数据
     */
    function loadPageTagData() {

        Api.getTagGroup(function (result) {

            //若无数据显示暂无数据
            if(result.length==0){
                $("#tag-cont").addClass("basic-img");
            }else{
                $("#tag-cont").removeClass("basic-img");
                var html = '';
                for (var i = 0; i < result.length; i++) {
                    var tagArray = result[i];
                    var initial = tagArray.tagInitial.substring(0, 1);//获取首字母
                    var tagHtml;
                    if (i != 0 && initial == result[i - 1].tagInitial.substring(0, 1)) {
                        tagHtml = '<div class="tag-item">';

                    } else {
                        if (i != 0) {
                            html += "</fieldset>";//一个首字母div的闭合
                        }
                        html += '<fieldset><legend class="tag-group-label"> ' + initial + '</legend>';
                        tagHtml = '<div class="tag-item">';
                    }
                    var showTag = tagArray.tag;
                    if (tagArray.tag.length > 7) {
                        showTag = TempoUtils.htmlEscape(tagArray.tag.substring(0, 7)) + '...';
                    }
                    tagHtml += '<span onmouseover="$m(\'dam/metamanage/publictag\').rmShow(this)" ' +
                        'onmouseout="$m(\'dam/metamanage/publictag\').rmHide(this)" ' +
                        'class="tag-group-span" title="' + TempoUtils.htmlEscape(tagArray.tag) + '">' + showTag + '<div id=' + tagArray.id + ' class="tag-group-span-del" style="display: inline;" >' +
                        '<button onclick="$m(\'dam/metamanage/publictag\').rmBtn(this,\'' + tagArray.id + '\')"  class="closebut">' +
                        '<img class="imgsrc" src="/dam-metamanage/dam/metamanage/tag/plugin/image/close.svg">' +
                        '</button>' +
                        '</div>' +
                        '</span></div>';
                    if (i == result.length - 1) {
                        tagHtml += "</fieldset>";//一个首字母div的闭合
                    }
                    html += tagHtml;
                }
                $("#tag-cont").html(html);
                $(".tag-group-span-del").hide();
            }

        });
    }

    //弹出层控制
    this.addTag = function () {
        addDialog = $.topDialog({
            title: "新建标签",
            href:addWinPath,
            width: 480,
            height: 240,
            buttons: [
                {
                    text: '关闭',
                    handler: function () {
                        $.topDialog('close', addDialog);
                    }
                },
                {
                    text: '保存',
                    id: 'saveBtn',
                    handler: function () {
                        var tag = addDialog.find("#tag").val();
                        //去除2边空格
                        tag=trim(tag);
                        if(tag.length==0){
                            addDialog.find('#sp').html('标签不能为空');
                            addDialog.find('#sp').css({"color":"#F56C6C"});
                            return;
                        }
                        addDialog.parent().find('#saveBtn').linkbutton('disable');
                        Api.savePublictag(tag, function (result) {
                            top$.messager.promptInfo({
                                msg: "添加成功",
                                icon: 'success',
                            });
                            $.topDialog('close', addDialog);
                            loadPageTagData();
                        })
                        addDialog.parent().find('#saveBtn').linkbutton('enable');
                    }
                }
            ]
        });
    }
});
function trim(str) {
    return str.replace(/(^\s*)|(\s*$)/g,'');
}
/**
 * 错误信息提示
 * @param msg
 */
function errorPromptInfo(msg) {
    top$.messager.promptInfo({
        msg: msg,
        icon: 'error',
    });
}