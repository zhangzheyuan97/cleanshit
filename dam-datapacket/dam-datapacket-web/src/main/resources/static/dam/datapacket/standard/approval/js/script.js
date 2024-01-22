$m('dam/metamanage/standard/approval', function () {
    /**
     * 初始化选择对象
     */
    $(function () {

        top$.messager.promptInfo({
            msg: '删除数据失败',
            icon: 'error',
        });
        Api.health({msg: 'approval loading...'}, function (result) {
            top$.messager.promptInfo({
                msg: JSON.stringify(result),
                icon: 'successs',
            });
        });
    });
});
