package com.meritdata.dam.datapacket.plan.utils;

import com.meritdata.dam.base.constants.ResponseStatus;
import org.springframework.util.StringUtils;

/**
 * 接口返回值状态码<p/>
 * 错误码设计原则：<p/>
 * 1，状态码为6位整数，xx_xx_xx。微服务编号_模块编号_业务异常编号。<p/>
 * 2，数据管理微服务编号为 66。<p/>
 * 3，模块编号：01数据管理、02文件管理、03资源目录、04主题数据、05行权限控制、06数据访问日志
 * 4，业务异常编号01-99依次流水。<p/>
 *
 * @author weijh
 * @date 2023-02-24 11:28
 */
public enum EnumDamDatamanageResultStatus implements ResponseStatus {
    /**
     * 数据管理模块异常定义
     */
    DATA_MODEL_TREE_ERROR(660101, "获取模型树失败！"),
    DATA_DATATYPE_MODEL_ERROR(660102, "获取数据分类下的模型失败！"),
    DATA_DATATYPE_FAVORITE_ERROR(660103, "获取数据分类下收藏的模型失败！"),
    DATA_DATATYPE_FAVORITE_EXISTENCE_ERROR(660104, "判断数据分类下是否存在收藏过的模型失败！"),
    DATA_FAVORITE_ERROR(660105, "收藏模型失败！"),
    DATA_FAVORITE_REMOVE_ERROR(660106, "取消模型收藏失败！"),
    DATA_GET_SHOW_CONFIG_ERROR(660107, "获取模型显示配置失败！"),
    DATA_TREE_LIST_ERROR(660108, "判断是否为树列表方式失败！"),
    DATA_MODEL_FIELD_LIST_ERROR(660109, "获取模型字段列表失败！"),
    DATA_LIST_ERROR(660110, "获取模型数据列表失败！"),
    DATA_MODEL_BUTTON_PRIVILEGE_ERROR(660111, "获取模型按钮权限失败！"),
    DATA_GET_AVAI_STATUS_MODEL_ERROR(660112, "获取模型生效版本信息失败！"),
    DATA_USER_CUSTOM_FILTER_ERROR(660113, "记录用户自定义查询信息失败！"),
    DATA_GET_FIELD_INFO_ERROR(660114, "获取模型字段信息失败！"),
    DATA_MODEL_FORM_ERROR(660115, "查询模型属性表单失败"),
    DATA_IMPORT_NO_DATA_ERROR(660116, "没有可导入的数据"),
    DATA_IMPORT_REPEAT_FIELD_ERROR(660117, "字段%s重复"),
    DATA_IMPORT_UNEDITABLE_ERROR(660118, "字段%s不可编辑"),
    DATA_IMPORT_FILE_FIELD_ERROR(660119, "字段%s为文件字段，不可编辑"),
    DATA_IMPORT_NON_NULL_ERROR(660120, "字段%s不能为空"),
    DATA_IMPORT_MATCH_AUDIT_ERROR(660121, "第%n行与审核中的数据重复，不能导入!"),
    DATA_IMPORT_MATCH_STOP_ERROR(660122, "第%n行与停用的数据重复，不能导入!"),
    DATA_GET_USER_FILTER_ERROR(660123, "获取用自定义查询条件失败！"),
    DATA_LIST_SQL_ERROR(660124, "获取模型数据列表组装SQL失败！"),
    DATA_GET_QUALITY_RULE_ERROR(660125, "获取质量规则失败！"),
    DATA_GET_MODEL_FIELD_ASSIGN_ERROR(660126, "获取模型赋值字段出错！"),
    DATA_IMPORT_MATCH_EXISTS_ERROR(660126, "该条数据已存在！"),
    DATA_ADD_MODEL_INFO_ERROR(660127, "新增模型数据失败！"),
    DATA_UPDATE_MODEL_INFO_ERROR(660128, "更新模型数据失败！"),
    DATA_REVISE_MODEL_INFO_ERROR(660129, "修订模型数据失败！"),
    DATA_IMPORT_NOTNULL_ERROR(660130, "%s数据不可为空！"),
    DATA_GET_DIC_ERROR(660131, "查询字典值失败！"),
    DATA_CREATE_QUALITY_TABLE_ERROR(660132, "创建合格数据表失败！"),
    DATA_CREATE_DIRTY_TABLE_ERROR(660133, "创建脏数据表失败！"),
    DATA_GET_CONDITION_INFO_ERROR(660134, "获取条件规则信息失败！"),
    DATA_GET_COMBINE_INFO_ERROR(660135, "获取组合规则信息失败！"),
    DATA_GET_COMBINE_EXP_ERROR(660136, "获取组合规则表达式失败！"),
    PRIMARYKEY_VAL_NOT_NULL(660137, "唯一字段属性【{0}】值不能为空！"),
    DATA_GET_COMBINE_MERGE_OPERATOR_ERROR(660138, "根据组合表达式动态添加合并算子失败！"),
    DATA_INSTALL_SINGLE_OPERATOR_ERROR(660139, "根据规则信息组装单个算子对象失败！"),
    DATA_GET_CORRELATION_MODEL_ID_ERROR(660140, "获取对比模型ID失败！"),
    DATA_GET_CORRELATION_FIELD_ID_ERROR(660141, "获取对比模型字段ID失败！"),
    DATA_SUBMIT_QUALITY_PROCESS_ERROR(660142, "提交质量流程失败！"),
    DATA_GET_CLOUMN_FAMILY_ERROR(660143, "获取Hbase列簇信息失败！"),
    DATA_GET_HDFS_FILE_LIST_ERROR(660144, "获取HDFS文件列表失败！"),
    DATA_IMPORT_MATCH_EXIST_ERROR(660145, "第%s行该条数据已存在，不能导入!"),
    DATA_IMPORT_DATA_TYPE_ERROR(660146, "%s数据类型错误！"),
    DATA_IMPORT_DATA_FORMAT_ERROR(660147, "%s数据格式错误！"),
    DATA_IMPORT_NOT_INT_ERROR(660148, "%s数据不是整型！"),
    DATA_IMPORT_LENGTH_DEFINITION_ERROR(660149, "%s大于为此列指定的长度或精度范围！"),
    GET_USER_ERROR(660150, "查询用户信息失败"),
    GET_DEPT_ERROR(660151, "查询部门信息失败"),
    GET_DB_ERROR(660152, "查询数据源信息失败"),
    GET_MODEL_DATA_ERROR(660153, "查询模型数据失败！"),
    GET_MODEL_FIELD_CONF_ERROR(660154, "查询模型属性配置失败！"),
    GET_DATABASE_INFO_ERROR(660155, "获取数据源信息失败！"),
    GET_MODEL_VERSION_ERROR(660156, "获取模型版本数据失败！"),
    DATA_IMPORT_FILE_ANALYSIS_ERROR(660157, "文件解析异常！"),
    DATA_IMPORT_SECRET_ERROR(660158, "第%s行密级大于当前用户最大密级，不能导入!"),
    DATA_IMPORT_SECRET_CLOSE_ERROR(666699, "第%s行数据密级为停用状态，不能导入!"),
    DATA_FILE_SECRET_LESS_ERROR(777777, "文件中数据的最高密级高于文件的密级，无法上传"),
    GET_MODEL_FIELD_PRIVILEGE_ERROR(660159, "获取模型字段权限失败！"),
    GET_HADOOP_CONFIG_ERROR(660160, "获取HADOOP配置信息失败！"),
    DATA_IMPORT_DATA_REP_ERROR(660161, "第%s行匹配字段数据重复，不能导入!"),
    DATA_EXPORT_HBASE_COLUMN_EMPTY(660162, "要导出的列为空，请选择要导出的列！"),
    DATA_LIST_CONFIG_FILE_OR_CONFIG_DIR_ERROR(660163, "配置文件或配置路径不存在！"),
    DATA_IMPORT_FORMULA_LENGTH_ERROR(660164, "%s内容超过公式字段设置长度，请检查！"),
    DATA_IMPORT_FORMULA_EXP_ERROR(660165, "%s数值类型公式计算出错！"),
    DATA_IMPORT_SAVE_DATA_ERROR(660166, "导入数据入库失败！"),
    JOB_TRANSFORM_ERROR(660167, "任务流程转换失败！"),
    JOB_SUBMIT_ERROR(660168, "提交任务失败，请检查服务是否可用！"),
    CREATE_IMPORT_JOB_ERROR(660169, "创建导入任务失败"),
    CREATE_EXPORT_JOB_ERROR(660170, "创建导出任务失败"),
    DATA_GET_DIC_INFO_ERROR(660171, "获取数据字典信息失败！"),
    DATA_IMPORT_REF_FIELD_ERROR(660172, "%s关联到多条数据！"),
    DATA_VALIDATE_ERROR(660173, "入库数据质量校验失败"),
    GET_HBASE_ROWKEY_RULE_ERROR(660174, "获取Rowkey规则失败"),
    DATA_IMPORT_DATA_SPECIAL_ERROR(660175, "%s存在特殊字符[']！"),
    GET_UNMAX_DATA_LIST_ERROR(660176, "获取模型非最高版本数据列表失败！"),
    GET_PARTITION_LIST_ERROR(660177, "获取分区列表失败！"),
    DELETE_PARTITION_ERROR(660178, "分区数据删除失败！"),
    DELETE_DATA_ERROR(660179, "删除数据失败！"),
    MODEL_DATA_EXPORT_ERROR(660180, "模型数据导出失败！"),
    MODEL_NOT_FOUNT(660181, "模型不存在"),
    DATA_IMPORT_REF_STOP_DATA_ERROR(660182, "%s存在停用或错误数据！"),
    DATA_IMPORT_UNSIGNED_ERROR(660183, "%s为负数！"),
    DATA_GET_QUALITY_INFO_ERROR(660184, "获取质量规则信息失败！"),
    OFF_DATA_ERROR(660185, "停用数据失败！"),
    OPEN_DATA_ERROR(660186, "启用数据失败！"),
    QU_DATA_ERROR(660187, "未查询到期望的数据"),
    VERIFY_DATA_ERROR(660188, "校验数据异常"),
    DATA_OPERATE_UNSUPPORT_ERROR(660189, "不支持的操作：【s%】!"),
    DATA_IMPORT_MATCH_ERROR(660190, "第%s行匹配到多条数据，不能导入!"),
    EXIST_NOT_SUPPORT_SECRET(660191, "查询数据密级不符合平台数据密级规范，请联系数据管理员进行确认处理!"),
    DATA_OTHER_ERROR(660199, "其他异常"),

    /**
     * 文件关联模块异常定义
     */
    GET_FILE_TYPE_TREE_ERROR(660201, "获取文件类型左侧模型树失败!"),
    GET_HDFS_CONFIG_ERROR(660202, "获取hdfs配置信息失败"),
    UPLOAD_CONFIG_FILE_ERROR(660203, "上传配置文件【s%】出错"),
    CONNECT_SERVER_ERROR(660204, "上传文件配置失败，无法连接文件服务器"),
    HADOOP_CONFIG_FILE_FAIL(660205, "获取大数据配置文件流失败"),
    UPLOAD_FILE_SUFFIX_FAIL(660206, "文件格式不符合规则！"),
    UPLOAD_FILe_FAIL(660207, "文件【s%】上传失败!"),
    DOWNLOAD_FILe_FAIL(660208, "文件【s%】下载失败!"),
    /**
     * 资源目录模块异常定义
     */
    CATALOG_SAVE_ERROR(660301, "保存资源信息失败！"),
    CATALOG_FIELD_DELETE_ERROR(660302, "删除目录属性失败！"),
    CATALOG_LIST_SQL_ERROR(660303, "获取资源目录数据列表组装SQL失败！"),
    CATALOG_DATA_LIST_ERROR(660304, "获取资源目录数据列表失败！"),
    CATALOG_IMPORT_NOT_RESOURCE_ERROR(990305, "未找到导入sheet页:%s 信息，请检查！"),
    CATALOG_IMPORT_NAME_ERROR(990306, "导入%s名称为空，请检查！"),
    CATALOG_IMPORT_NOT_DATA_ERROR(990307, "导入%s无数据，请检查！"),
    CATALOG_IMPORT_TYPE_ERROR(990308, "导入%s资源类型为空，请检查！"),
    CATALOG_IMPORT_RESOURCE_NULL_ERROR(990309, "导入%s资源不存在，请检查！"),
    CATALOG_IMPORT_NOT_UPDATE_ERROR(990310, "导入%s无可更新的资源或数据，请检查！"),
    CATALOG_IMPORT_DATE_FORMAT_ERROR(990311, "导入%s不符合日期格式，请检查！"),
    CATALOG_IMPORT_TIME_FORMAT_ERROR(990312, "导入%s不符合时间格式，请检查！"),
    CATALOG_IMPORT_REF_DIC_ERROR(990312, "导入%s未关联到数据字典值，请检查！"),
    CATALOG_IMPORT_DATABASE_ERROR(990313, "导入数据入库失败，请联系管理员！"),
    CATALOG_IMPORT_FILE_EXTENSION_ERROR(990314, "导入文件类型错误，请下载扩展名为xls的模板导入！"),
    CATALOG_INIT_CATEGORY_INFO_ERROR(990315, "获取所属分类信息失败！"),
    CATALOG_INIT_STORAGE_INFO_ERROR(990316, "获取存储位置信息失败！"),
    CATALOG_DATA_LENGTH_OUT_OF_RANGE_ERROR(990317, "数据内容超长，请检查！"),
    GET_THEME_TREE_ERROR(990318, "获取主题树失败！"),
    CATALOG_IMPORT_FILE_OLD_ERROR(990319, "导入excel版本太旧，请下载2003版本excel导入！"),
    CATALOG_IMPORT_FILE_ERROR(990320, "导入excel文件类型不合法！"),
    CATALOG_INIT_DATATAG_INFO_ERROR(990321, "获取数据标签信息失败！"),
    CATALOG_INIT_INFO_ERROR(990322, "获取分类查询信息失败！"),
    CATALOG_IMPORT_OPEN_ERROR(990323, "导入%s是否公开为空，请检查！"),
    EXIST_XSS_ERROR(990324, "请求数据可能存在安全漏洞，请检查！"),
    /**
     * 行权限控制模块异常定义
     */
    GET_MODEL_TREE_ERROR(660501, "查询行权限左侧模型树出错!"),
    GET_ROLE_TREE_ERROR(660502, "查询非系统角色树出错!"),
    GET_MODEL_REL_FIELD_ERROR(660503, "获取模型引用属性列表失败!"),
    ROW_PRIVILEGE_GET_SYSTEM_FIELD_ERROR(660504, "获取制定系统字段列表失败!"),
    ROW_PRIVILEGE_SAVE_ERROR(660505, "系统属性行权限信息保存失败!"),
    ROW_PRIVILEGE_ORG_DEPT_TREE_ERROR(660506, "获取组织机构树出错!"),
    ROW_PRIVILEGE_CONFIG_LIST_ERROR(660507, "引用属性权限分配列表失败!"),

    /**
     * 数据访问日志模块异常定义
     */
    DATALOG_SAVE_ERROR(660601, "保存数据操作日志失败！"),

    /**
     * 任务管理
     */
    GET_TASK_LIST_ERROE(660702, "获取任务列表失败！"),
    GET_TASK_BY_ID_ERROE(660701, "根据任务id获取任务信息失败！"),
    UPDATE_STATUS_ERROE(660703, "数据导入节点任务状态更新失败！"),
    UPDATE_MESSAGE_ERROE(660704, "数据导入日志更新失败！"),

    /**
     * 元数据管理
     */
    GET_USER_INFO_LIST_ERROR(660801, "批量获取用户信息失败！"),
    GET_ORG_INFO_LIST_ERROR(660801, "批量获取部门信息失败！");


    private Integer status;
    private String message;

    private EnumDamDatamanageResultStatus(Integer status, String message) {
        this.status = status;
        this.message = message;
    }

    @Override
    public Integer status() {
        return this.status;
    }

    @Override
    public String message() {
        return this.message;
    }

    public static String getMessage(Integer status) {
        String message = "";
        EnumDamDatamanageResultStatus[] values = values();
        for (EnumDamDatamanageResultStatus item : values) {
            if (item.status.equals(status)) {
                message = item.message;
                break;
            }
        }
        return message;
    }

    public static EnumDamDatamanageResultStatus of(Integer status) {
        if (!StringUtils.isEmpty(status)) {
            EnumDamDatamanageResultStatus[] types = EnumDamDatamanageResultStatus.values();
            for (EnumDamDatamanageResultStatus type : types) {
                if (type.status.intValue() == status.intValue()) {
                    return type;
                }
            }
        }
        return null;
    }
}
