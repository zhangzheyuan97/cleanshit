package com.meritdata.dam.datapacket.plan.acquistion.vo;

/**
 * @Author fanpeng
 * @Date 2023/4/18
 * @Describe 非结构化数据对象
 */
public class PackGroupFileVO {

    //文件id
    private String fileId;
    //文件分类id
    private String fileTypeId;
    //文件类型
    private String fileType;
    //文件名称
    private String fileName;
    //文件路径
    private String filePath;
    //文件密级
    private String secretLevel;

    public String getSecretLevel() {
        return secretLevel;
    }

    public void setSecretLevel(String secretLevel) {
        this.secretLevel = secretLevel;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileTypeId() {
        return fileTypeId;
    }

    public void setFileTypeId(String fileTypeId) {
        this.fileTypeId = fileTypeId;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
