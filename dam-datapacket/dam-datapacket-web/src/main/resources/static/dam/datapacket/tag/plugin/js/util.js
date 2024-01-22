/**
 * 打标签工具方法
 * @date 2021/3/17 15:07
 * @author zhangmy
 */
$m('dam/meta/tag/util', function () {
    var this_ = $m('dam/meta/tag/util');
    var modulePath = '/dam-metamanage/api/metamanage/';
    var tagWinPath = modulePath + 'publictag/view/tag';
    var tagListWinPath = modulePath + 'publictag/view/tagList';
    var tagDialog;//标签维护窗口
    var tagListDialog;
    var PUBLIC_FLAG = "PUBLIC";
    var PRIVATE_FLAG = "PRIVATE";
    var ADDTYPE_FLAG = "ADD";
    var EDITTYPE_FLAG = "EDIT";
    var VIEWTYPE_FLAG = "VIEW";
    var toSelectedTags = {};//待选框的标签
    var selectedTags = {};//选中框的标签
    /**
     * 资产目录页面打开标签展示页面
     * @param tagOuterTdId
     * @param dialog
     * @param resourceId
     */
    this.openTagListDialog = function (resourceId, privilege) {
        tagListDialog = $.topDialog({
            title: '标签',
            width: 800,
            height: 600,
            href: tagListWinPath,
            onLoad: function () {
                //如果是PUBLIC，则显示公共标签，否则隐藏
                if (privilege == PUBLIC_FLAG) {
                    this_.ajaxGetResourceTag("PUBLIC", 'EDIT', "resourcePublicTags", tagListDialog, resourceId);
                    tagListDialog.find('#publicTagDivId').show();
                }
                this_.ajaxGetResourceTag("PRIVATE", 'EDIT', "resourcePrivateTags", tagListDialog, resourceId);
            }
        });
    };

    /**
     * 打开标签窗口
     * @param tagType 标签类型PUBLIC,PRIVATE
     * @param pageType 页面类型ADD 或者 EDIT
     * @param tagOuterTdId 标签标签框id
     * @param resourceId 资源id
     * @param dialog 弹出框
     */
    this.openTagDialog = function (tagType, pageType, tagOuterTdId, dialog, resourceId) {
        let selectedTag = dialog.find('#' + tagOuterTdId + ' .sTags-onlyRead').children();
        if (pageType == ADDTYPE_FLAG && selectedTag.length == 0) {
            selectedTags[tagOuterTdId] = [];//如果是新增且未编辑，清空选中
        }
        //判断是否是tagList页面，taglist页面在点击保存按钮时直接存到后台
        let saveButtonText='确认';
        if (isTagList(dialog)) {
            saveButtonText="保存";
        }
        tagDialog = $.topDialog({
            title: '',
            width: 520,
            height: 400,
            href: tagWinPath,
            onLoad: function () {
                this_.ajaxLoadTagDialogData(tagType, pageType, tagOuterTdId, resourceId);
            },
            buttons: [
                {
                    id: "TagSaveCancle", text: "取消", handler: function () {
                        $.topDialog('close', tagDialog);
                    }
                },
                {
                    id: "TagSave", text: saveButtonText, handler: function () {
                        var inputIsEdit=tagDialog.find('.validatebox-text').validatebox('getValue')!=undefined&&
                            tagDialog.find('.validatebox-text').validatebox('getValue').length>0;
                        let selectTagEles = tagDialog.find('.badge-info');
                        if(inputIsEdit){
                            warnPromptInfo('存在正在编辑的标签，请先保存');
                        }else{
                            this_.tagSaveAction(tagType, tagOuterTdId, dialog, resourceId);
                        }

                    }
                },
            ]
        });
    };
    /**
     * 获取资源弹出框的标签
     * @param tagType 标签类型
     * @param pageType 页面类型
     * @param tagOuterTdId 标签框id
     * @param resourceId 资源id
     * @param dialog 弹出框
     */
    this.ajaxGetResourceTag = function (tagType, pageType, tagOuterTdId, dialog, resourceId) {
        /*if(dialog.find('#'+tagOuterTdId)[0]==undefined){
            //页面上把标签删除了
            return;
        }*/
        let successCallback = function (result) {
            if (result.success) {
                if (result.data.length > 0) {
                    for (let i = 0; i < result.data.length; i++) {
                        result.data[i].id = result.data[i].publicTagId;
                    }
                    selectedTags[tagOuterTdId] = result.data;
                } else {
                    selectedTags[tagOuterTdId] = [];
                }
                this_.initResourceTag(tagOuterTdId, dialog);
            } else {
                errorPromptInfo(result.message);
            }
        };
        if (tagType == PUBLIC_FLAG) {
            Api.getResourceSelectedPublicTag({"resourceId": resourceId}, successCallback, function () {
                errorPromptInfo('获取标签失败');
            });
        } else if (tagType == PRIVATE_FLAG) {
            Api.getResourceSelectedPrivateTag({"resourceId": resourceId}, successCallback, function () {
                errorPromptInfo('获取标签失败');
            });
        }
        if (pageType != VIEWTYPE_FLAG) {
            this_.boundOpenTagDialogEventToButton(tagType, pageType, tagOuterTdId, dialog, resourceId);
        }

    };
    /**
     * 打标签按钮绑定点击事件
     * @param tagType
     * @param pageType
     * @param tagOuterTdId
     * @param dialog
     * @param resourceId
     */
    this.boundOpenTagDialogEventToButton = function (tagType, pageType, tagOuterTdId, dialog, resourceId) {
        dialog.find('#' + tagOuterTdId + ' button').click(function () {
            this_.openTagDialog(tagType, pageType, tagOuterTdId, dialog, resourceId);
        });
    };
    /**
     * 加载标签弹出框数据
     * @param tagType public or private
     * @param pageType add or edit
     * @param tagOuterTdId 标签框id
     * @param resourceId 资源id
     */
    this.ajaxLoadTagDialogData = function (tagType, pageType, tagOuterTdId, resourceId) {
        if (pageType == ADDTYPE_FLAG) {
            resourceId = "";
        }
        let successCallback = function (result) {
            if (result.success) {
                toSelectedTags[tagOuterTdId] = result.data;
                this_.initTagDialog(tagOuterTdId);
            } else {
                errorPromptInfo(result.message);
            }
        };

        if (tagType == PUBLIC_FLAG) {
            Api.getResourceToSelectPublicTag({"resourceId": resourceId}, successCallback, function () {
                errorPromptInfo("获取资源标签失败");
            });
        } else if (tagType == PRIVATE_FLAG) {
            Api.getResourceToSelectPrivateTag({"resourceId": resourceId}, successCallback, function () {
                errorPromptInfo("获取资源标签失败");
            });
        }


    };

    /**
     * 加载标签弹出框已选项
     * @param tagOuterTdId 标签框id
     */
    this.initTagDialog = function (tagOuterTdId) {
        let toSelectedTagStauts=toSelectedTags[tagOuterTdId];
        let selectedTag=selectedTags[tagOuterTdId];
        let previousTag="";
        var distinctToSelectedTagStatus=[];
        if(toSelectedTagStauts.length>0){
            for(var i=0;i<toSelectedTagStauts.length;i++){
                //判断标签是否和前一个一样
                if(previousTag!=toSelectedTagStauts[i].tag){
                    for(var j=0;j<selectedTag.length;j++){
                        if(toSelectedTagStauts[i].tag==selectedTag[j].tag){
                            toSelectedTagStauts[i].selected=true;
                            break;
                        }
                    }
                    distinctToSelectedTagStatus.push(toSelectedTagStauts[i]);
                }
                previousTag=toSelectedTagStauts[i].tag;
            }
        }
        tagDialog.find("#resourceDataTag").sTags({
            selectedTag: selectedTag,
            data: distinctToSelectedTagStatus
        })
    };
    /**
     * 加载 新增编辑页面资源的标签
     * @param tagOuterTdId
     * @param dialog 弹出框
     */
    this.initResourceTag = function (tagOuterTdId, dialog) {
        dialog.find("#" + tagOuterTdId + ' input').sTags({
            id: tagOuterTdId,
            clearBefore: true,
            data: selectedTags[tagOuterTdId],
            screen: false,
            sTagsSelectedCss: "sTags-onlyRead"
        });
    };
    /**
     * 标签弹出框的保存按钮操作
     * @param tagOuterTdId
     * @param dialog
     * @param resourceId 资源id
     */
    this.tagSaveAction = function (tagType, tagOuterTdId, dialog, resourceId) {
        let selectTagEles = tagDialog.find('.badge-info');
        let selectedTag = [];
        if (selectTagEles.size() > 0) {
            for (let i = 0; i < selectTagEles.size(); i++) {
                selectedTag.push({
                    id: selectTagEles.get(i).getAttribute('tagid'),
                    tag: selectTagEles.get(i).getAttribute('title'),
                    tagInitial: selectTagEles.get(i).getAttribute('tagInitial'),
                    tagType: selectTagEles.get(i).getAttribute('tagType')
                });
            }
        }
        //更新选中的标签
        selectedTags[tagOuterTdId] = selectedTag;

        //判断是否是tagList页面，taglist页面在点击保存按钮时直接存到后台
        if (isTagList(dialog)) {
            this_.saveResourceTagsAction(tagType, tagOuterTdId, resourceId);
        }

        this_.initResourceTag(tagOuterTdId, dialog);
        $.topDialog('close', tagDialog);
    };
    /**
     * 保存资源标签
     * @param tagType
     * @param resourceId
     * @param tagOuterTdId
     */
    this.saveResourceTagsAction = function (tagType, tagOuterTdId, resourceId) {
        if (selectedTags[tagOuterTdId] == undefined) {
            return;
        }
        var successCallback = function (result) {
            if (!result.success) {
                errorPromptInfo(result.message);
            }
        };
        if (tagType == PUBLIC_FLAG) {
            Api.saveResourcePublicTag({
                "resourceId": resourceId,
                "tags": JSON.stringify(selectedTags[tagOuterTdId])
            }, successCallback, function () {
                errorPromptInfo('保存资源标签失败');
            });
        } else if (tagType == PRIVATE_FLAG) {
            Api.saveResourcePrivateTag({
                "resourceId": resourceId,
                "tags": JSON.stringify(selectedTags[tagOuterTdId])
            }, successCallback, function () {
                errorPromptInfo('保存资源标签失败');
            });
        }


    };
    /**
     * 批量删除资源所有标签
     * @param resourceIds 资源id
     */
    this.deleteResourceAllTagBatch = function (resourceIds) {
        for (let i = 0; i < resourceIds.length; i++) {
            this_.deleteResourceAllTag(resourceIds[i]);
        }
    };


    /**
     * 删除单个资源的标签
     * @param resourceId 资源id
     */
    this.deleteResourceAllTag = function (resourceId) {
        Api.deleteResourceAllTags({
            "resourceId": resourceId
        }, function (result) {
            if (!result.success) {
                errorPromptInfo(result.message);
            }
        }, function () {
            errorPromptInfo('删除资源标签失败');
        });

    };

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
    function warnPromptInfo(msg) {
        top$.messager.promptInfo({
            msg: msg,
            icon: 'warn',
        });
    }
    //判断是否是tagList页面
    function isTagList(dialog){
        return dialog.find('.tagListOutterDiv').children().length > 1;
    }
});