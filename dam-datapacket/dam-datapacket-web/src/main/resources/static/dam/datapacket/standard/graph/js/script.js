//echarts图像自适应
var charts = [];
window.onresize = function () {
    for (var i = 0; i < charts.length; i++) {
        charts[i].resize();
    }
};
$m('dam/metamanage/standard/graph', function () {
    /**
     * 初始化选择对象
     */
    $(function () {
        // Api.health({msg: 'graph loading...'}, function (result) {
        //     top$.messager.promptInfo({
        //         msg: JSON.stringify(result),
        //         icon: 'successs',
        //     });
        // });
        // 各标准统计分析
        standardStatistic();
        // 数据元标准状态
        elementStatus();
        // 标准下发
        standardDown();
        // 标准评分TOP10
        reportTop();
        // 评分分布
        reportRange();
    })

    /**
     * 各标准统计分析
     */
    function standardStatistic () {
        Api.statistic(function (result) {
            result = result || {};
            if (StandardUtils.isNotEmpty(result)) {
                var elementTotal = StandardUtils.isNotEmpty(result.elementTotal) ? result.elementTotal + "个" : "0个";
                $('#element_num').html(elementTotal);
                var enumTotal = StandardUtils.isNotEmpty(result.enumTotal) ? result.enumTotal + "个" : "0个";
                $('#enum_num').html(enumTotal);
                var fileTotal = StandardUtils.isNotEmpty(result.fileTotal) ? result.fileTotal + "个" : "0个";
                $('#file_num').html(fileTotal);
                var standardDownTotal = StandardUtils.isNotEmpty(result.standardDownTotal) ? result.standardDownTotal + "个" : "0个";
                $('#down_num').html(standardDownTotal);
                var reportScore = StandardUtils.isNotEmpty(result.reportScore) ? result.reportScore : "0";
                $('#score_num').html(reportScore);
            }
        })
    }

    /**
     * 数据元标准状态
     */
    function elementStatus() {
        var element_status_map = echarts.init(document.getElementById("element_status"));
        element_status_map.showLoading({
            text: "正在努力读取数据中..."
        });
        Api.element(function (result) {
            result = result || [];
            if (result.length === 0) {
                if (element_status_map) {
                    element_status_map.dispose();
                }
                element_status_map.hideLoading();
                $('#element_status').addClass('comment_no_data_style');
                return;
            } else {
                $('#element_status').removeClass('comment_no_data_style');
            }
            var color = ['#60DBAA', '#FAD06E', '#9AA6B3'];
            var m2R2Data = [];
            var legendArray = [];
            var data = result;
            var allNum = 0;
            for (var i = 0; i < data.length; i++) {
                allNum += data[i].value;
                var item = {
                    value: data[i].value,
                    name: data[i].label, itemStyle: {
                        color: color[i]
                    }
                };
                legendArray.push(data[i].label);
                m2R2Data.push(item);
            }
            var option_pie = {
                title: {
                    text: '数据元标准总数',
                    subtext: allNum,
                    textStyle: {
                        color: '#9a9a9a',
                        fontWeight: 400,
                        fontSize: 18,
                        align: 'center'
                    },
                    subtextStyle: {
                        fontSize: 16,
                        color: '#9a9a9a'
                    },
                    x: 'center',
                    y: '45%',
                },
                legend: {
                    show: true,
                    orient: 'vertical',
                    x: '30px',
                    y: 'bottom',
                    icon: 'circle',
                    data: legendArray
                },
                tooltip: {
                    trigger: 'item',
                    formatter: function (params) {
                        var str = params.marker + "" + params.data.name + "</br>" +
                            "数量：" + params.data.value + "</br>" +
                            "占比：" + params.percent + "%";
                        return str;
                    }
                },
                series: [
                    {
                        name: '标题',
                        type: 'pie',
                        radius: ['48%', '75%'],
                        center: ['50%', '50%'],
                        clockwise: true, //饼图的扇区是否是顺时针排布
                        avoidLabelOverlap: false,
                        label: {
                            normal: {
                                show: true,
                                fontSize: 12,
                                position: 'inside',
                                formatter: function (params) {
                                    return params.data.value;
                                }
                            }
                        },
                        labelLine: {
                            length: 1,
                            length2: 55,
                            show: true
                        },
                        data: m2R2Data
                    }
                ]
            };
            element_status_map.setOption(option_pie);
            element_status_map.hideLoading();
            charts.push(element_status_map);
        })
    }

    /**
     * 标准下发
     */
    function standardDown() {
        var standard_down_map = echarts.init(document.getElementById("standard_down"));
        standard_down_map.showLoading({
            text: "正在努力读取数据中..."
        });
        Api.down(10,function (result) {
            result = result || [];
            if (result.length === 0) {
                if (standard_down_map) {
                    standard_down_map.dispose();
                }
                standard_down_map.hideLoading();
                $('#standard_down').addClass('comment_no_data_style');
                return;
            } else {
                $('#standard_down').removeClass('comment_no_data_style');
            }
            var plantCap = result;
            if (StandardUtils.isNotEmpty(plantCap)) {
                //echarts图形绘制
                var colorList = [
                    "rgba(250, 150, 233,1)", "rgba(83, 198,255,1)", 'rgba(250, 110, 143,1)', "rgba(175,126,252,1)",
                    "rgba(96,219,170,1)", "rgba(250,208,110,1)", "rgba(165,230,101,1)", "rgba(250,154,35,1)",
                    "rgba(126, 156, 242,1)", "rgba(50, 289, 182,1)", "rgba(249, 115, 72,1)", "rgba(69,236,246,1)",
                    "rgba(73,181,235,1)",
                ];
                var colorList4d = [
                    "rgba(250, 150, 233,0.7)", "rgba(83, 198,255,0.7)", 'rgba(250, 110, 143,0.7)', "rgba(175,126,252,0.7)",
                    "rgba(96,219,170,0.7)", "rgba(250,208,110,0.7)", "rgba(165,230,101,0.7)", "rgba(250,154,35,0.7)",
                    "rgba(126, 156, 242,0.7)", "rgba(50, 289, 182,0.7)", "rgba(249, 115, 72,0.7)", "rgba(69,236,246,0.7)",
                    "rgba(73,181,235,0.7)",
                ];
                var datalist = [];
                var index = 0;
                for (var i = 0; i < plantCap.length; i++) {
                    if (index === colorList.length) {
                        index = 0;
                    }
                    var mapObject = {
                        offset: [Math.floor(Math.random() * (90 - 10)) + 10, Math.floor(Math.random() * (80 - 10) + 10)],//设置图形的位置
                        symbolSize: getSymbolSize(plantCap[i].value),
                        opacity: .95,
                        color: new echarts.graphic.RadialGradient(0.3, 0.5, 0.7, [{
                            offset: 0,
                            color: colorList4d[index]
                        },
                            {
                                offset: 1,
                                color: colorList[index]
                            }
                        ])
                    };
                    datalist.push(mapObject);
                    index ++;
                }
                var datas = [];
                for (var i = 0; i < plantCap.length; i++) {
                    var item = plantCap[i];
                    var itemToStyle = datalist[i];
                    datas.push({
                        name: item.label + '\n' + item.value + "个",
                        value: itemToStyle.offset,
                        symbolSize: itemToStyle.symbolSize,
                        num: item.value,
                        label: {
                            normal: {
                                textStyle: {
                                    fontSize: 12
                                }
                            }
                        },
                        children: item.children,
                        itemStyle: {
                            normal: {
                                color: itemToStyle.color,
                                opacity: itemToStyle.opacity
                            }
                        },
                    })
                }
                // reSize(plantCap,datalist);
                var option_scatter = {
                    grid: {
                        left: '8%',
                        top: '5%',
                        right: '0%',
                        bottom: '5%',
                        containLabel: true
                    },
                    tooltip: {
                        show : true,
                        enterable : true,
                        position : function (point, params, dom, rect, size) {
                            var x = 0;
                            var y = 0;
                            var pointX = point[0];
                            var pointY = point[1];
                            var boxWidth = size.contentSize[0];
                            var boxHeight = size.contentSize[1];
                            if (boxWidth > pointX) {
                                x = 50;
                            } else {
                                x = pointX - boxWidth;
                            }
                            if (boxHeight > pointY) {
                                y = 20;
                            } else {
                                y = pointY - boxHeight;
                            }
                            return [x, y];
                        },
                        extraCssText : 'overflow:auto;',
                        formatter: function (params) {
                            if (StandardUtils.isNotEmpty(params)) {
                                var num = params.data.num;
                                var tooltipStyle = num > 12 ? 'width:500px;height:300px;' : '';
                                var html = "<div style='" + tooltipStyle +"'>";
                                html += "<span>" + params.data.name.replace('\n', ' : ') + "</span></br>" ;
                                if (StandardUtils.listIsNotEmpty(params.data.children)) {
                                   var children = params.data.children;
                                    children.forEach(function (item) {
                                        html += "<span>" + item.label + "</span></br>";
                                    })
                                }
                                return html + "</div>";
                            }
                        },
                        triggerOn: 'click'
                    },
                    xAxis: [{
                        gridIndex: 0,
                        type: 'value',
                        show: false,
                        min: 0,
                        max: 100,
                        nameLocation: 'middle',
                        nameGap: 5
                    }],
                    yAxis: [{
                        gridIndex: 0,
                        min: 0,
                        show: false,
                        max: 100,
                        nameLocation: 'middle',
                        nameGap: 30
                    }],
                    series: [{
                        type: 'scatter',
                        symbol: 'circle',
                        symbolSize: 120,
                        label: {
                            normal: {
                                show: true,
                                formatter: '{b}',
                                color: '#fff',
                                textStyle: {
                                    fontSize: '20'
                                }
                            },
                        },
                        itemStyle: {
                            normal: {
                                color: '#00acea'
                            }
                        },
                        data: datas
                    }]
                };
                standard_down_map.setOption(option_scatter);
                standard_down_map.hideLoading();
                charts.push(standard_down_map);
            }
        });
    }

    /**
     * 气泡大小
     * @param number
     * @returns {number}
     */
    function getSymbolSize(number) {
        var num = Math.floor(number/5);
        var size = 0;
        switch (num) {
            case 0 :
                size = number * 10 + 50;
                break;
            case 1 :
                size = number * 9 + 50;
                break;
            case 2 :
                size = number * 8 + 50;
                break;
            case 3 :
                size = number * 7 + 50;
                break;
            case 4 :
                size = number * 6 + 50;
                break;
            default :
                size = number * 5 + 50;
                break;
        }
        return size;
    }
    /**
     * 通过值来改变球的大小
     * @param plantCap
     * @param dataList
     */
    function reSize(plantCap,dataList){
        var maxArr = [];
        plantCap.forEach(function(item){
            maxArr.push(item.value)
        });
        var strMax = maxArr.sort(function(a,b){
            return b-a;
        });
        var maxNum = strMax[0];//获取最大值
        dataList.forEach(function(item,index){
            item.symbolSize = plantCap[index].value/maxNum*100;
        })
    }
    /**
     * 标准评分TOP10
     */
    function reportTop() {
        var report_top10_map = echarts.init(document.getElementById("report_top10"));
        report_top10_map.showLoading({
            text: "正在努力读取数据中..."
        });
        Api.top(10, function (result) {
            result = result || [];
            if (result.length === 0) {
                if (report_top10_map) {
                    report_top10_map.dispose();
                }
                report_top10_map.hideLoading();
                $('#report_top10').addClass('comment_no_data_style');
                return;
            } else {
                $('#report_top10').removeClass('comment_no_data_style');
            }
            var catalog = [];
            var data = [];
            result.sort((a,b)=>{
                return Number(b.value) - Number(a.value)  ;
            });
            $.each(result, function (i, item) {
                catalog.push(item.label);
                data.push(item.value);
            });

            var option_bar = {
                color: [new echarts.graphic.LinearGradient(0, 0, 1, 0, [{
                    offset: 0,
                    color: "#53C6FF"
                }, {
                    offset: 1,
                    color: "#53C6FF"
                }], false)],
                grid: {
                    left: '23%',
                    right: '10%',
                    top: '5%',
                    bottom: '5%'
                },
                tooltip: {
                    show: "true",
                    trigger: 'axis',
                    axisPointer: {
                        type: 'shadow'
                    }
                },
                yAxis: {
                    data: catalog,
                    axisTick: {
                        show: false
                    },
                    axisLine: {
                        show: false
                    },
                    inverse: true,
                    axisLabel: {
                        show: true,
                        color: "#585F66",
                        fontSize: 12,
                        margin: 120,
                        textStyle:{
                            align: 'left'
                        },
                        formatter: function (params){
                            if(params.length > 10){
                                return params.substr(0, 10)+"...";
                            }else{
                                return params;
                            }
                        },
                        position: 'left'
                    },
                },
                xAxis: [{
                    axisTick: {
                        show: false
                    },
                    type: 'value',
                    axisLine: {
                        show: false
                    },
                    axisLabel: {
                        show: false
                    },
                    splitLine: {
                        show: false
                    }
                }],
                series: [{
                    name: '评分',
                    type: 'bar',
                    barWidth: 16,
                    barGap : 10,
                    label: {
                        normal: {
                            show: true,
                            position: 'right',
                            textStyle: {
                                color: "#585F66",
                                fontSize: 12
                            }
                        }
                    },
                    data: data,
                    itemStyle: {
                        emphasis : {
                            barBorderRadius: 30
                        },
                        normal: {
                            barBorderRadius:[8,8,8,8]
                        }
                    }
                }]
            };
            report_top10_map.setOption(option_bar);
            report_top10_map.hideLoading();
            charts.push(report_top10_map);
        })
    }

    /**
     * 评分分布
     */
    function reportRange() {
        var element_range_map = echarts.init(document.getElementById("element_range"));
        element_range_map.showLoading({
            text: "正在努力读取数据中..."
        });
        Api.range('ELEMENT', function (result) {
            result = result || [];
            if (result.length === 0) {
                if (element_range_map) {
                    element_range_map.dispose();
                }
                element_range_map.hideLoading();
                $('#element_range').addClass('comment_no_data_style');
                return;
            } else {
                $('#element_range').removeClass('comment_no_data_style');
            }
            var catalog = [];
            var data = [];
            var total = 0;
            $.each(result, function (i, item) {
                catalog.push(item.label);
                data.push(item.value);
                total += item.value;
            });
            var option_bar = {
                color: [new echarts.graphic.LinearGradient(0, 0, 1, 0, [{
                    offset: 0,
                    color: "#60DBAA"
                }, {
                    offset: 1,
                    color: "#60DBAA"
                }], false)],
                grid: {
                    left: '-7%',
                    right: '18%',
                    top: '6%',
                    bottom: '6%',
                    containLabel: true
                },
                tooltip: {
                    show: "true",
                    trigger: 'item',
                    axisPointer: {
                        type: 'shadow'
                    },
                    formatter: function (params) {
                        var percent = 0;
                        if(StandardUtils.isNotEmpty(params.value) && params.value !== 0) {
                            percent = ((params.value / total) * 100).toFixed(0);
                        }
                        var str =  params.name + "</br>" +
                            "占比：" + percent + "%" + "</br>" +
                            "数量：" + params.value;
                        return str;
                    }
                },
                yAxis: {
                    data: catalog,
                    axisTick: {
                        show: false
                    },
                    axisLine: {
                        show: false
                    },
                    inverse: true,

                    axisLabel: {
                        show: true,
                        color: "#585F66",
                        fontSize: 12,
                        margin: 70,
                        textStyle:{
                            align: 'left'
                        },
                        formatter: function (params) {
                            if(params.length > 10){
                                return params.substr(0, 10)+"...";
                            }else{
                                return params;
                            }
                        },
                        position: 'left'
                    },
                },
                xAxis: [{
                    axisTick: {
                        show: false
                    },
                    type: 'value',
                    axisLine: {
                        show: false
                    },
                    axisLabel: {
                        show: false
                    },
                    splitLine: {
                        show: false
                    }
                }],
                series: [{
                    name: '数据量',
                    type: 'bar',
                    barWidth: 24,
                    barGap : 10,
                    data: data,
                    itemStyle: {
                        emphasis : {
                            barBorderRadius: 30
                        },
                        normal: {
                            barBorderRadius:[10,10,10,10],
                        }
                    },
                    label: {
                        normal: {
                            show: true,
                            position: 'right',
                            textStyle: {
                                color: "#585F66",
                                fontSize: 12
                            },
                            formatter: function (params) {
                                if (StandardUtils.isNotEmpty(params.name)) {
                                    var percent = 0;
                                    if(StandardUtils.isNotEmpty(params.value) && params.value !== 0){
                                        percent = ((params.value / total) * 100).toFixed(0);
                                        return percent + '% (' + params.value + ')';
                                    }else{
                                        return '';
                                    }
                                } else {
                                    return '';
                                }
                            }
                        }
                    }
                }]
            };
            element_range_map.setOption(option_bar);
            element_range_map.hideLoading();
            charts.push(element_range_map);
        });
        var enum_range_map = echarts.init(document.getElementById("enum_range"));
        enum_range_map.showLoading({
            text: "正在努力读取数据中..."
        });
        Api.range('ENUM', function (result) {
            result = result || [];
            if (result.length === 0) {
                if (enum_range_map) {
                    enum_range_map.dispose();
                }
                enum_range_map.hideLoading();
                $('#enum_range').addClass('comment_no_data_style');
                return;
            } else {
                $('#enum_range').removeClass('comment_no_data_style');
            }
            var catalog = [];
            var data = [];
            var total = 0;
            $.each(result, function (i, item){
                catalog.push(item.label);
                data.push(item.value);
                total += item.value;
            });
            var option_bar = {
                color: [new echarts.graphic.LinearGradient(0, 0, 1, 0, [{
                    offset: 0,
                    color: "#53C6FF"
                }, {
                    offset: 1,
                    color: "#53C6FF"
                }], false)],
                grid: {
                    left: '-7%',
                    right: '18%',
                    top: '6%',
                    bottom: '6%',
                    containLabel: true
                },
                tooltip: {
                    show: "true",
                    trigger: 'item',
                    axisPointer: {
                        type: 'shadow'
                    },
                    formatter: function (params){
                        var percent = 0;
                        if(StandardUtils.isNotEmpty(params.value) && params.value !== 0) {
                            percent = ((params.value / total) * 100).toFixed(0);
                        }
                        var str =  params.name + "</br>" +
                            "占比：" + percent + "%" + "</br>" +
                            "数量：" + params.value;
                        return str;
                    }
                },
                yAxis: {
                    data: catalog,
                    axisTick: {
                        show: false
                    },
                    axisLine: {
                        show: false
                    },
                    inverse: true,
                    axisLabel: {
                        show: true,
                        color: "#585F66",
                        fontSize: 12,
                        margin: 70,
                        textStyle:{
                            align: 'left'
                        },
                        formatter: function (params) {
                            if(params.length > 5){
                                return params.substr(0, 8)+"...";
                            }else{
                                return params;
                            }
                        },
                        position: 'left'
                    },
                },
                xAxis: [{
                    axisTick: {
                        show: false
                    },
                    type: 'value',
                    axisLine: {
                        show: false
                    },
                    axisLabel: {
                        show: false
                    },
                    splitLine: {
                        show: false
                    }
                }],
                series: [{
                    name: '数据量',
                    type: 'bar',
                    barWidth: 24,
                    barGap : 10,
                    label: {
                        normal: {
                            show: true,
                            position: 'right',
                            textStyle: {
                                color: "#585F66",
                                fontSize: 12
                            },
                            formatter: function (params) {
                                if (StandardUtils.isNotEmpty(params.name)){
                                    var percent = 0;
                                    if(StandardUtils.isNotEmpty(params.value) && params.value !== 0){
                                        percent = ((params.value / total) * 100).toFixed(0);
                                        return percent + '% (' + params.value + ')';
                                    }else{
                                        return '';
                                    }
                                } else {
                                    return '';
                                }
                            }
                        }
                    },
                    data: data,
                    itemStyle: {
                        emphasis : {
                            barBorderRadius: 30
                        },
                        normal: {
                            barBorderRadius:[10,10,10,10],
                        }
                    }
                }]
            };
            enum_range_map.setOption(option_bar);
            enum_range_map.hideLoading();
            charts.push(enum_range_map);
        });
    }
});
