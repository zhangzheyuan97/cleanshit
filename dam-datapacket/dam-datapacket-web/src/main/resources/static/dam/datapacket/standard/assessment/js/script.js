$m('dam/metamanage/standard/assessment', function () {

    /**
     * 初始化选择对象
     */
    $(function () {
    });


    this.assessTabSelected = function (title, index) {
        switch (index) {
            case 0:
                setTimeout(function () {
                    $m('dam/metamanage/standard/assessment/task').initTaskGrid();
                }, 100);
                break;
            case 1:
                $m('dam/metamanage/standard/assessment/report').initReportGrid();
                break;
            case 2:
                $m('dam/metamanage/standard/assessment/log').initLogGrid();
                break;
            default:
                break;
        }

    };

});
