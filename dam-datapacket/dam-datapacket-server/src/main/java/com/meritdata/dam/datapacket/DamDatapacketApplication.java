package com.meritdata.dam.datapacket;


import com.meritdata.cloud.base.MeritDataCloudApplication;
import com.meritdata.cloud.config.SwaggerConfig;
import com.meritdata.dam.datapacket.plan.client.IDatamationsClient;
import com.meritdata.dam.datapacket.plan.utils.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.HashMap;
import java.util.Map;

/**
 * @author merit
 */
@MeritDataCloudApplication
@EnableSwagger2
@EnableAutoConfiguration(exclude = SwaggerConfig.class)
public class DamDatapacketApplication {

    public static void main(String[] args) {
        SpringApplication.run(DamDatapacketApplication.class, args);

    }

}
