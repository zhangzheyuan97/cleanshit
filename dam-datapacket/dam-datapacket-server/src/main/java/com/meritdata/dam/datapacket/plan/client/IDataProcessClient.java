package com.meritdata.dam.datapacket.plan.client;

import com.meritdata.cloud.config.CloudFeignConfiguration;
import com.meritdata.cloud.resultmodel.ResultBody;
import com.meritdata.dam.dataprocess.model.dto.BatchDataOperationDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;


/**
 * 远程调用client
 */
@FeignClient(value = "dam-dataprocess", configuration = CloudFeignConfiguration.class, contextId = "IDataProcessClient")
public interface IDataProcessClient {

    /**
     * 批量修改数据
     * @param batchUpdateParam
     * @return
     */
    @PostMapping(value = "/api/strucdata/data/batch-update", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    ResultBody<Boolean> batchUpdate(BatchDataOperationDTO batchUpdateParam);

}
