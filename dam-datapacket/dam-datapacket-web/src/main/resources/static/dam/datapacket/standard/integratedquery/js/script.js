$m('dam/metamanage/standard/integratedquery', function () {

    var searchForm;
    var queryKeyword;

    $(function () {
        searchForm = $('#dam_matemanage_integratedquery_query_form');
        searchForm.find('input[name="keyword"]').on('keyup', function (e) {
            if (e.keyCode == "13") {
                resetQuery();
            }
        });
        $('input[type="checkbox"]').on('click', function (e) {
            resetQuery();
        });
        resetQuery();
    });

    this.integratedQuery = function () {
        resetQuery();
    };

    /**
     * 重置查询结构集
     */
    var resetQuery = this.resetQuery = function () {
        var types = getCheckedValues();
        var keyword = searchForm.form('getRecord').keyword;
        queryKeyword = keyword;
        $('#integrated-query-content').html('');
        top$.messager.progress({
            text: '正在加载，请稍候....',
            interval: 500,
            width: 500
        });
        $m('dam/metamanage/standard/integratedquery/pagination').resetPageQuery({keyword: keyword}, types);
    };

    this.showPageResult = function (result) {
        $('#integrated-query-content')[0].style.maxHeight = $('#integrated-query-content').parent().height() + 'px';
        var html = getQueryResultHtml(result);
        $('#integrated-query-content').html(html);
    };

    /**
     * 构建查询结构展示
     * @returns {[]}
     */
    function getQueryResultHtml(result) {
        result = result || {};
        var rows = result.rows || [];
        if (StandardUtils.listIsEmpty(rows)) {
            return getTableZeroImgHtml();
        }
        var html = '';
        for (let i = 0; i < rows.length; i++) {
            var item = rows[i];
            var btnHtml = '<a id="exportTemplate" href="javascript:void(0)" style="color: #2E8AE6" class="table_td_opetation l-btn-text" ' +
                'onclick="$m(\'dam/metamanage/standard/integratedquery/show\').showInfo(' + "\'" + item.type + "','" + item.standardId + "\'" + ')">查看</a>';
            var itemInfoHtml = getItemInfo(item);
            var itemRemarkHtml = getItemRemark(item);
            var title = dyeKeyword(item.title);
            var type = getTypeLabel(item.type);
            html += '<div class="standard-item-content">' +
                '	<div class="item-content-left">' +
                '		<div class="s-item-banner">' +
                '           <span class="s-i-title">' + title + '</span><span class="s-i-tag">' + type + '</span>' +
                '		</div>' +
                '		<div class="s-item-title">'
                + itemInfoHtml +
                '		</div>' +
                '		<div class="s-item-content">'
                + itemRemarkHtml +
                '		</div>' +
                '	</div>    ' +
                '	<div class="item-content-right">'
                + btnHtml +
                '	</div>' +
                '</div>';
        }
        return html;
    }

    function getItemRemark(item) {
        var type = item.type;
        var titleHtml = '';
        var row = item.standardData;
        if (StandardUtils.equals(type, 'ELEMENT')) {
            return getSpanTag('业务含义说明', row.buisRemark, "span-online-show span-rm-margin-top");
        }
        if (StandardUtils.equals(type, 'ENUM')) {
            var itemHtml = getEnumItemNames(row.itemNames);
            var rmkHtml = getSpanTag('枚举项说明', row.remark, "span-online-show span-rm-margin-top");
            if (StandardUtils.isNotEmpty(itemHtml)) {
                rmkHtml += '</br><span class="span-online-show">';
                rmkHtml += ('<span class="span-mn-rt">枚举值：</span>' + itemHtml + '</span>');
            }
            return rmkHtml;
        }
        if (StandardUtils.equals(type, 'FILE')) {
            return getSpanTag('文件说明', row.remark, "span-online-show span-rm-margin-top");
        }
        return titleHtml;
    }

    function getTypeLabel(type) {
        var labelObj = {
            ELEMENT: '数据元标准',
            ENUM: '枚举项标准',
            FILE: '标准文件',
        };
        var label = labelObj[type];
        return StandardUtils.isBlank(label) ? '' : label;
    }

    function getItemInfo(item) {
        var type = item.type;
        var titleHtml = '';
        var row = item.standardData;
        //数据元编码，数据元分类，中文名称、英文名称
        if (StandardUtils.equals(type, 'ELEMENT')) {
            titleHtml += getSpanTag('数据元编码', row.code, '');
            titleHtml += getSpanTag('中文名称', row.buisName, '');
            titleHtml += getSpanTag('英文名称', row.publishName, '');
            titleHtml += getSpanTag('数据元分类', row.elementTypeName, '');
        }
        //1）展示内容包括枚举项名称，枚举项编码，枚举项说明；枚举值名称；
        if (StandardUtils.equals(type, 'ENUM')) {
            titleHtml += getSpanTag('枚举项编码', row.code, '');
            titleHtml += getSpanTag('枚举项名称', row.name, "span-online-enum-name")
        }
        // 文件编号、文件名称、文件说明
        if (StandardUtils.equals(type, 'FILE')) {
            titleHtml += getSpanTag('文件编号', row.code, '');
            var fileName = fmtFileName(row);
            titleHtml += getSpanTag('文件名称', fileName, '');
        }
        return titleHtml;
    }

    /**
     * 枚举值数据展示 先默认10条
     * @param items
     * @returns {string}
     */
    function getEnumItemNames(items) {
        items = items || [];
        var titleVal = items.join(" ");
        var html = '<span title="' + titleVal + '">';
        for (var i = 0; i < items.length; i++) {
            var item = items[i];
            item = dyeKeyword(item);
            html += ('<span class="span-mn-rt">' + item + '</span>');
        }
        return html + '</span>';
    }


    function getSpanTag(label, value, classes) {
        if (StandardUtils.isBlank(classes)) {
            classes = 'span-mn-rt span-mn-rt-field';
        }
        var valTitle = value;
        if (StandardUtils.isNotEmpty(valTitle)) {
            valTitle = valTitle.replace(/\</g, "&lt;").replace(/\>/g, "&gt;");
            valTitle = valTitle.replace(/\"/g, "&quot;");
        }
        value = dyeKeyword(value);
        valTitle = valTitle ? valTitle : '';
        return '<span class="' + classes + '" title="' + valTitle + '">' + label + '：' + value + '</span>';
    }

    /**
     * 输出文件名称
     * @param row
     * @returns {*}
     */
    function fmtFileName(row) {
        var value = row.name;
        var extension = row.extension || '';
        if (StandardUtils.isNotEmpty(extension)) {
            extension = '.' + extension;
        }
        return value + extension;
    }

    /**
     * 获取已选择的标准类型
     * @returns {[]}
     */
    function getCheckedValues() {
        var checkboxes = $('input[type="checkbox"]') || [];
        var types = [];
        for (var i = 0; i < checkboxes.length; i++) {
            var checkbox = checkboxes[i];
            if (checkbox.checked) {
                types.push(checkbox.value);
            }
        }
        return types;
    }

    /**
     * 匹配值染色
     * @param v
     * @returns {string|*|void|string}
     */
    function dyeKeyword(v) {
        var vTemp = v;
        var keywordTemp = queryKeyword;
        var vTx = $("<span></span>");
        if (StandardUtils.isBlank(queryKeyword)) {
            vTx.text(vTemp);
            return parseDom(vTx);
        }
        if (StandardUtils.isBlank(vTemp)) {
            return '';
        }
        if (vTemp.indexOf(queryKeyword) < 0) {
            vTx.text(vTemp);
            return parseDom(vTx);
        }
        var time = new Date().getTime();
        var ltKey = "『" + time;//防止相同
        var gtKey = "』" + time;//防止相同
        keywordTemp = keywordTemp.replace(/\</g, ltKey).replace(/\>/g, gtKey);
        vTemp = vTemp.replace(/\</g, ltKey).replace(/\>/g, gtKey);
        var dyeV = '<span style="color: red;">' + keywordTemp + '</span>';
        if (StandardUtils.equals(queryKeyword, ".")) {
            keywordTemp = "\\.";
        }
        var regKeyword = new RegExp(keywordTemp, 'g').toString();
        var ltKeyExp = new RegExp(ltKey, 'g').toString();
        var gtKeyExp = new RegExp(gtKey, 'g').toString();
        vTemp = vTemp.replace(eval(regKeyword), dyeV);
        vTemp = vTemp.replace(eval(ltKeyExp), "&lt;").replace(eval(gtKeyExp), "&gt;");
        return vTemp;
    }

    function getTableZeroImgHtml() {
        var height = $('#integrated-query-content').parent().height();
        return '<div style="width:100%;height:' + height + 'px;" class="table-zero-img"></div>'
    }

    function parseDom(dom) {
        var content = $("<div></div>");
        content.append(dom);
        return content[0].innerHTML;
    }
});
