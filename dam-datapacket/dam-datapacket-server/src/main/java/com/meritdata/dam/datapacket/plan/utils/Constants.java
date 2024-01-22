package com.meritdata.dam.datapacket.plan.utils;

/**
 * @Author fanpeng
 * @Date 2023/4/18
 * @Describe 常量类
 */
public class Constants {

    // ============================ 数据包组包相关常量 ================================
    //汇总
    public static final String IS_POOL = "1";

    //不汇总
    public static final String NO_POOL = "0";

    //成包包含
    public static final String IS_PACKAGE = "1";

    //成包不包含
    public static final String NO_PACKAGE = "0";

    //默认分页参数
    public static final int PAGE = 1;
    public static final int ROWS = 1000;

    //单机批次节点层级
    public static final String DJ_PC_LEVEL = "4";
    //单机实物节点层级
    public static final String DJ_SW_LEVEL = "5";
    //分系统发次节点层级
    public static final String FXT_FC_LEVEL = "4";

    //单机名称
    public static final String SINGLE_NAME = "单机";
    //分系统
    public static final String SYSTEM_NAME = "分系统";
    //模块
    public static final String MODEL_NAME = "模块";

    //向上汇总标识 为1 则为本级
    public static final String THIS_LEVEL = "1";
    //向上汇总标识 为2 则为总装直属件或单机，并列处理
    public static final String OTHER_LEVEL = "2";

    //固定的表头
    public static final String[] TABLE_HEADS_FIX = {"分类", "型号/图号", "发次/批次", "实物"};

    //流程审批通过的状态
    public static final String PROCESS_STATUS_PASS = "pass";

    //文件标识
    public static final int IS_FILE = 1;

    //校验模板名字的正则
    public static final String REG = "[^0-9a-zA-Z\u4e00-\u9fa5]";

    //无权限查看固定值
    public static final String NO_PERMISSION_PROMPT_FIX = "存在无权限查看的数据：";
    //组包线程最大数量
    public static final int THREAD_MAX_NUM = 10;

    public static final String ORDER_FIELD = "序号";

    public static final String GROUP_FIELD = "产品代号";

    //树排序的四种情况
    public static final String TREE_ONE                 = "DJ";
    public static final String TREE_TWO                 = "FXT_MK";
    public static final String TREE_THREE               = "FXT_MK_DJ";
    public static final String TREE_FOUR                = "FXT_MK_DJ_ZSJ";


    //数据增加的密级名称
    public static final String SECRET_LEVEL_NAME = "密级";
    //默认密级 3为内部
//    public static final String DEFAULT_SECRET_LEVEL = "3";

    //nacos配置文件 默认密级 配置
    public static final String NACOS_DATA_ID = "common-dam.properties";
    //nacos分组
    public static final String NACOS_GROUP = "DEFAULT_GROUP";
    //nacos配置项 默认密级配置项
    public static final String NACOS_DEFAULT_SECRET = "datapacket.default.secret";

    // ============================ 数据包组包相关常量end ================================

    // ============================ 页面配置枚举 =========================================
    public enum PageFlagEnum {
        // TODO: 2023/4/23 其他页面往前加
        //数据包版本
        PACKAGE_VERSION("package_version"),
        //我的审批
        MY_APPROVE("my_approve"),
        //数据采集页面单机
        MAINTAIN_SINGLE("maintain_single"),
        //数据采集页面分系统
        MAINTAIN_SYSTEM("maintain_system"),
        //发次实物
        LITY_MATTER("lity_matter"),
        //型号策划
        MODULE_PLAN("module_plan"),
        //数据维护
        DATA_MAINTAIN("data_maintain_view"),
        //数据包组包
        PACKAGE_GROUP("package_group_view"),
        //数据包展示
        PACKAGE_SHOW("package_show_view");
        /**
         * 编码
         */
        private String code;

        /**
         * 描述信息
         */
        private String value;

        PageFlagEnum(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    // ============================ 页面配置枚举 end =========================================


    // ============================ 待办常量  =========================================
    //待办
    public final static String UNDO_TYPE = "0";
    //已办
    public final static String COMPLETE_TYPE = "2";
    // ============================ 待办常量end =======================================


    // ============================ 批次号功能 ========================================
    public final static String BATCH_NO_FIELD = "F_BatchNo";

    public static final String OWN_ISSUE = "发次本级";

    // ============================ 批次号功能end  ========================================
}
