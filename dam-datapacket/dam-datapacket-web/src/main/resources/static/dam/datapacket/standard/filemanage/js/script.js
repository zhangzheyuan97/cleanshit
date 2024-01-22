$m('dam/metamanage/standard/filemanage', function () {
    var filePageGrid;
    var searchForm;
    var uploadForm;
    var uploadDialog;
    var editFile;
    var defaultFileRemark = '未选择任何文件';
    var operateFileView = moduleResource.file.getView('operate');
    /**
     * 初始化选择对象
     */
    $(function () {
        filePageGrid = $('#dam_matemanage_data_standard_file_grid');
        searchForm = $('#dam_matemanage_data_standard_file_grid_query_form');
        searchForm.find('input[name="keyword"]').on('keyup', function (e) {
            if (e.keyCode == "13") {
                searchPage();
            }
        });
        loadFilePage('');
    });

    var fileColumns = [[
        {field: 'ck', checkbox: true},
        {field: 'code', title: '文件编号',
            formatter: function (value, row, index) {
                return StandardUtils.formatter(value);
            },width: 100},
        {
            field: 'name', title: '文件名称', width: 100,
            formatter: function (value, row, index) {
                var extension = row.extension || '';
                if (StandardUtils.isNotEmpty(extension)) {
                    extension = '.' + extension;
                }
                var val = value + extension;
                return StandardUtils.formatter(val);
            }
        },
        {field: 'remark', title: '文件说明',
            formatter: function (value, row, index) {
                return StandardUtils.formatter(value);
            },width: 100},
        {field: 'uploadTime', title: '上传时间', width: 100, formatter: StandardUtils.timeForMatter},
        {
            field: 'action', title: '操作', width: 100,
            buttons: [
                {text: '编辑', onclick: "$m('dam/metamanage/standard/filemanage').editFile"},
                {text: '下载', onclick: "$m('dam/metamanage/standard/filemanage').singleDownload"}
            ]
        }
    ]];

    function loadFilePage(keyword) {
        Api.getFilePage(filePageGrid, {keyword: keyword}, {
            rownumbers: true,
            fitColumns: true,
            singleSelect: false,
            pagination: true,
            paginationType:'advanced',
            pageSize: '20',
            fit: true,
            border: false,
            columns: fileColumns,
            toolbar: '#dam_matemanage_data_standard_file_grid_gridToolbar',
            onLoadSuccess: function (data) {
            }
        });
    }

    /**
     * 上传文件
     */
    this.uploadFile = function () {
        openUploadDialog(undefined, '文件上传');
    };

    /**
     * 文件编辑
     */
    this.editFile = function (record, index) {
        openUploadDialog(record, '文件编辑');
    };

    /**
     * 单文件下载
     * @param record
     * @param index
     */
    var singleDownload = this.singleDownload = function (record, index) {
        var form = $('<form>').attr('method', 'post').hide();
        $('body').append(form);
        Api.downloadFile(form, record.id, function (result) {
            form.delete();
        });
    };

    /**
     * 多文件下载文件
     */
    this.multiDownload = function () {
        var selections = filePageGrid.datagrid('getSelections');
        if (selections.length < 1) {
            $.Msg.alert('提示', '请选中需要下载的文件数据!');
            return;
        }
        for (var idx = 0; idx < selections.length; idx++) {
            singleDownload(selections[idx]);
        }
    };


    /**
     * 多文件删除
     */
    this.multiDelete = function () {
        var selections = filePageGrid.datagrid('getSelections');
        if (selections.length < 1) {
            top$.messager.promptInfo({msg: '请至少选择一条记录进行操作', icon: 'warning'});
            return;
        }
        var ids = [];
        for (var i = 0; i < selections.length; i++) {
            ids.push(selections[i].id);
        }
        $.Msg.confirm('提示', '确认删除选中的文件？', function (flag) {
            if (flag) {
                Api.deleteFiles(ids, function (result) {
                    result = result || {};
                    top$.messager.promptInfo({
                        msg: result.message,
                        icon: result.success ? 'info' : 'error',
                    });
                    if (result.success) {
                        searchPage();
                    }
                });
            }
        });
    };

    /**
     * 文件查询
     */
    var searchPage = this.searchPage = function () {
        var value = searchForm.form('getRecord').keyword;
        loadFilePage(value);
    };

    function openUploadDialog(record, title) {
        editFile = record;
        uploadDialog = $.topDialog({
            title: title,
            href: operateFileView,
            width: 600,
            height: 400,
            onLoad: function () {
                uploadForm = uploadDialog.find('#dam_matemanage_dsfile_upload_form');
                fileSelectedChange();
                if (record) {
                    uploadForm.form("load", record);
                    setDefaultRemark(record);
                } else {
                    uploadForm.find('#file-remark').text(defaultFileRemark);
                }
            },
            buttons: [
                {
                    text: '关闭',
                    handler: function () {
                        $.topDialog('close', uploadDialog);
                    }
                },
                {
                    text: '保存',
                    id: 'editSaveBtn',
                    handler: sendUploadFile
                }
            ]
        });
    }

    function sendUploadFile() {
        uploadDialog.parent().find('#editSaveBtn').linkbutton('disable');
        if (!uploadForm.form('validate')) {
            uploadDialog.parent().find('#editSaveBtn').linkbutton('enable');
            return;
        }
        // if (!editFile) {
            var filePath = uploadForm.find('#file').val();
            if (StandardUtils.isBlank(filePath)) {
                uploadForm.find('#file-remark').text("");
                uploadForm.find('#file-remark').text(defaultFileRemark);
                uploadForm.find('#file-remark')[0].style.color = 'red';
                setTimeout(function () {
                    uploadForm.find('#file-remark')[0].style.color = '';
                }, 1500);
                uploadDialog.parent().find('#editSaveBtn').linkbutton('enable');
                return;
            }
        // }
        var code = uploadForm.form('getRecord').code;
        var id = editFile ? editFile.id : '';
        Api.checkValueIsUsed('code', {value: code, id: id}, function (codeCheck) {
            if (codeCheck) {
                top$.messager.promptInfo({
                    msg: '文件编号已被使用!',
                    icon: "warning",
                });
            } else {
                var name = uploadForm.form('getRecord').name;
                var extension = uploadForm.find('#file-extension').text();
                name += extension;
                Api.checkValueIsUsed('name', {value: name, id: id}, function (nameCheck) {
                    if (nameCheck) {
                        top$.messager.promptInfo({
                            msg: '文件名称已被使用!',
                            icon: "warning",
                        });
                    } else {
                        operateApi();
                    }
                });
            }
        });
    }

    /**
     * 实际发送API到后端
     */
    function operateApi() {
        top$.messager.progress({
            text: '正在加载，请稍候....',
            interval: 500,
            width: 500
        });
        var formFile = uploadForm.find('#file');
        if (editFile) {
            uploadForm.find('input[name=id]').remove();
            var inputFiled = $("<input>");
            inputFiled.attr("type", "hidden");
            inputFiled.attr("name", "id");
            inputFiled.attr("value", editFile.id);
            uploadForm.append(inputFiled);
            formFile = uploadForm.find('#file');
            //构建formData,通过ajax上传文件
            var formData = new FormData();
            $.each(formFile[0].files, function (i, item) {
                formData.append("file", item, item.name);
            });
            formData.append("id", editFile.id);
            formData.append("name", uploadForm.form('getRecord').name);
            formData.append("code", uploadForm.form('getRecord').code);
            formData.append("remark", uploadForm.form('getRecord').remark);
            var url = Ajax.getResources('file', 'updateUrl',{});
            $.ajax({
                url: url,
                type: 'post',
                data: formData,
                processData: false,
                contentType: false,
                cache: false,
                success: function (data) {
                    if (data.success) {
                        $.topDialog('close', uploadDialog);
                    }else{
                        $.Msg.alert('提示', data.message);
                    }
                    searchPage();
                },
                error: function (rs) {
                    if (rs.status == 0 && 'rejected' == rs.state()) {
                        $.Msg.alert('提示', '文件可能被删除、移动、修改，请重新选择上传！');
                        formFile.val('');
                        uploadForm.find("#file-remark").text("");
                        uploadForm.find("#name").val("")
                    }
                },
                complete: function (data) {
                    top$.messager.progress('close');
                    uploadDialog.parent().find('#editSaveBtn').linkbutton('enable');
                }
            });
            // Api.updateFile(uploadForm, function (result) {
            //     if (result) {
            //         top$.messager.progress('close');
            //         searchPage();
            //         $.topDialog('close', uploadDialog);
            //     }
            //     setTimeout(function () {
            //         uploadDialog.parent().find('#editSaveBtn').linkbutton('enable');
            //     },400);
            // });
        } else {
            //构建formData,通过ajax上传文件
            var formData = new FormData();
            $.each(formFile[0].files, function (i, item) {
                formData.append("file", item, item.name);
                formData.append("name", uploadForm.form('getRecord').name);
                formData.append("code", uploadForm.form('getRecord').code);
                formData.append("remark", uploadForm.form('getRecord').remark);
            });
            var url = Ajax.getResources('file', 'uploadUrl',{});
            $.ajax({
                url: url,
                type: 'post',
                data:  formData,
                processData: false,
                contentType: false,
                cache: false,
                success: function (data) {
                    top$.messager.progress('close');
                    if (data.success) {
                        $.topDialog('close', uploadDialog);
                    }else{
                        $.Msg.alert('提示', data.message);
                    }
                    searchPage();
                },
                error: function (rs) {
                    if (rs.status == 0 && 'rejected' == rs.state()) {
                        $.Msg.alert('提示', '文件可能被删除、移动、修改，请重新选择上传！');
                        formFile.val('');
                        uploadForm.find("#file-remark").text("");
                        uploadForm.find("#name").val("")
                    }
                },
                complete: function (data) {
                    top$.messager.progress('close');
                    uploadDialog.parent().find('#editSaveBtn').linkbutton('enable');
                }
            });
            // Api.uploadFile(uploadForm, function (result) {
            //     if (result) {
            //         top$.messager.progress('close');
            //         searchPage();
            //         $.topDialog('close', uploadDialog);
            //
            //     }
            //     setTimeout(function () {
            //         uploadDialog.parent().find('#editSaveBtn').linkbutton('enable');
            //     },400);
            // });
        }
        setTimeout(function () {
            uploadDialog.parent().find('#editSaveBtn').linkbutton('enable');
        },400);
    }

    /**
     * 文件动态选择
     * @param e
     */
    function fileSelectedChange() {
        uploadForm.find('#file').on('change', function (e) {
            var files = e.currentTarget.files;
            if (StandardUtils.listIsNotEmpty(files)) {
                var fullName = files[0].name;
                uploadForm.find('#file-remark').text(fullName);
                var extension = '';
                var pointIdx = fullName.lastIndexOf(".");
                var fileName = fullName;
                if (!StandardUtils.equals(pointIdx, -1)) {
                    fileName = fullName.substr(fullName.lastIndexOf('\\') + 1, pointIdx);
                    extension = '.' + fullName.substr(pointIdx + 1);
                }
                if (StandardUtils.isBlank(fileName)){
                    uploadForm.find('#name').val(extension);
                    uploadForm.find('#file-extension').text('');
                } else {
                    uploadForm.find('#name').val(fileName);
                    uploadForm.find('#file-extension').text(extension);
                }
            } else {
                if (editFile) {
                    setDefaultRemark(editFile);
                } else {
                    uploadForm.find('#file-remark').text(defaultFileRemark);
                    uploadForm.find('#name').val('');
                    uploadForm.find('#file-extension').text('');
                }
            }
        });
    }

    /**
     * 动态设置上传控件的值
     * @param item
     */
    function setDefaultRemark(item) {
        var extension = item.extension;
        var fileName = item.name;
        uploadForm.find('#name').val(fileName);
        if (StandardUtils.isNotEmpty(extension)){
            extension = '.' + extension;
            uploadForm.find('#file-extension').text(extension);
            fileName += extension
        }
        uploadForm.find('#file-remark').text(fileName);
    }
});
