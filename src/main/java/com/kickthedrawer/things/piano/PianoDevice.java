/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kickthedrawer.things.piano;

import com.amazonaws.services.iot.client.AWSIotDevice;
import com.amazonaws.services.iot.client.AWSIotMqttClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author warnet
 */
@Component
public class PianoDevice extends AWSIotDevice implements InitializingBean, DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(PianoDevice.class);

    @Autowired
    private AWSIotMqttClient awsClient;

    @Autowired
    private Piano piano;

    public PianoDevice() {
        super("piano");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info("Connecting piano device to AWS client");

        awsClient.attach(this);
    }

    @Override
    public void destroy() throws Exception {
        logger.info("Disconnecting piano device from AWS client");

        awsClient.detach(this);
    }

}
