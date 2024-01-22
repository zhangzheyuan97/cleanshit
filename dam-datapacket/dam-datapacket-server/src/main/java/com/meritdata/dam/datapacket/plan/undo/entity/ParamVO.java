package com.meritdata.dam.datapacket.plan.undo.entity;

/**
 * @Author fanpeng
 * @Date 2023/5/10
 * @Describe 发送待办参数实体
 */
public class ParamVO {

    /**
     * 异构系统标识
     */
    private String syscode;
    /**
     *流程实例id ，其中0-flowid非涉密,  1-flowid涉密
     */
    private String flowid;
    /**
     *标题，格式为：异构系统名称+待办内容
     */
    private String requestname;
    /**
     * 流程类型名称
     */
    private String workflowname;
    /**
     *步骤名称（节点名称）
     */
    private String nodename;
    /**
     *PC地址  回调到第三方系统到地址，不能为空！
     */
    private String pcurl;
    /**
     * APP地址  等于PC地址
     */
    private String appurl;
    /**
     *流程处理状态  用于流程流转的核心字段
     * 0：待办
     * 2：已办
     * 4：办结
     * 8：抄送（待阅）
     */
    private String isremark;

    /**
     * 流程查看状态
     * 0：未读
     * 1：已读;
     */
    private String viewtype;
    /**
     * 创建人（原值）--采用主数据编码18位
     */
    private String creator;
    /**
     * 创建日期时间
     */
    private String createdatetime;
    /**
     *接收人（原值）--采用主数据编码18位
     */
    private String receiver;
    /**
     * 接收日期时间  格式为 yyyy-MM-dd HH:mm:ss
     */
    private String receivedatetime;
    /**
     * 时间戳字段，客户端使用线程调用接口的时候，根据此字段判断是否需要更新数据，防止后发的请求数据被之前的覆盖
     * 例如"1602817491990"(毫秒级时间戳)
     */
    private String receivets;

    public String getSyscode() {
        return syscode;
    }

    public void setSyscode(String syscode) {
        this.syscode = syscode;
    }

    public String getFlowid() {
        return flowid;
    }

    public void setFlowid(String flowid) {
        this.flowid = flowid;
    }

    public String getRequestname() {
        return requestname;
    }

    public void setRequestname(String requestname) {
        this.requestname = requestname;
    }

    public String getWorkflowname() {
        return workflowname;
    }

    public void setWorkflowname(String workflowname) {
        this.workflowname = workflowname;
    }

    public String getNodename() {
        return nodename;
    }

    public void setNodename(String nodename) {
        this.nodename = nodename;
    }

    public String getPcurl() {
        return pcurl;
    }

    public void setPcurl(String pcurl) {
        this.pcurl = pcurl;
    }

    public String getAppurl() {
        return appurl;
    }

    public void setAppurl(String appurl) {
        this.appurl = appurl;
    }

    public String getIsremark() {
        return isremark;
    }

    public void setIsremark(String isremark) {
        this.isremark = isremark;
    }

    public String getViewtype() {
        return viewtype;
    }

    public void setViewtype(String viewtype) {
        this.viewtype = viewtype;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getCreatedatetime() {
        return createdatetime;
    }

    public void setCreatedatetime(String createdatetime) {
        this.createdatetime = createdatetime;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getReceivedatetime() {
        return receivedatetime;
    }

    public void setReceivedatetime(String receivedatetime) {
        this.receivedatetime = receivedatetime;
    }

    public String getReceivets() {
        return receivets;
    }

    public void setReceivets(String receivets) {
        this.receivets = receivets;
    }
}
