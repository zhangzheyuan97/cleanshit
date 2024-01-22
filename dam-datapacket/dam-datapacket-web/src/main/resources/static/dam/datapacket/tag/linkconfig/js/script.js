$m('dam/metamanage/linktagconfig', function () {
    $(function () {
        loadPageData();
    });

    function loadPageData() {
        var html = '';
        Api.getTagManageGroup(function (result) {
            result = result || [];
            for (var i = 0; i < result.length; i++) {
                $('#outerDiv').removeClass("basic-img");
                var tagManageGroup = result[i];

                var tags = tagManageGroup.linkTagConfigDTOS || [];
                html += '<div class="ds"><span class="standard-pic-style">▋</span><span class="standard-title-style">' + tagManageGroup.name + ' </span></div>' +
                    '        <div class="big_2_div" style="">' +
                    '            <div class="big_2_diva" >' +
                    '               <span class="big_2_sp">' +
                    '                   <span class="big_2_sp2">' +
                    '                      i\n' +
                    '                   </span>\n' +
                    '                   <span style="margin-left: 10px">' +
                    tagManageGroup.toolTip +
                    '                   </span>\n' +
                    '               </span>\n' +
                    '            </div>';
                var tagHtml = ' <div class="big_2_divb" >' +
                    '<table>';
                for (var tagIdx = 0; tagIdx < tags.length; tagIdx++) {
                    var tag = tags[tagIdx];

                    var tag = tags[tagIdx];
                    if (tagIdx % 3 == 0) {
                        tagHtml += '<tr>'
                    }
                    tagHtml += '<td>'
                    if (tag.selected === false) {
                        tagHtml += '<input type="checkbox" applyType="' + tag.applyType + '" catalogFieldId="' + tag.catalogFieldId + '" name="linktagconfig">'
                    } else {
                        tagHtml += '<input  type="checkbox" applyType="' + tag.applyType + '" catalogFieldId="' + tag.catalogFieldId + '" name="linktagconfig" checked>'
                    }
                    let showBusiname=tag.busiName;
                    if(showBusiname.length>7){
                        showBusiname=showBusiname.substring(0,7)+'...';
                    }
                    tagHtml += '<span class="in_sp" title="'+tag.busiName+'">\n' +
                        showBusiname +
                        '</span>\n' +
                        '</td>'
                    if (tagIdx % 3 == 2) {
                        tagHtml += '</tr>'
                    }
                }
                tagHtml += '</table>';
                tagHtml += '</div>';
                html += tagHtml;
                html += '</div>';
            }
            $("#big_2").html(html);
            //添加复选框的点击事件
            $("input[name='linktagconfig']").change(function () {
                let data = {};
                data.applyType = this.getAttribute('applyType');
                data.catalogFieldId = this.getAttribute('catalogFieldId');
                if (this.checked) {
                    Api.saveLinktagConfig(JSON.stringify(data),function (result) {
                        console.log(result);
                    })
                }else{
                    Api.delLinktagConfig(JSON.stringify(data),function (result) {
                        console.log(result);
                    })
                }

            })
        });
    }


});
