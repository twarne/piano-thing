/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kickthedrawer.things.piano;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import static java.lang.String.format;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Transmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 *
 * @author warnet
 */
@Component
@ConfigurationProperties("midi")
public class Piano implements InitializingBean, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(Piano.class);

    @Value("#{deviceName}")
    private String deviceName;

    private MidiDevice receiverDevice;
    private Receiver receiver;
    private Sequencer sequencer;
    private Transmitter transmitter;

    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info(format("Found device name %s", deviceName));

        Info[] midiInfo = MidiSystem.getMidiDeviceInfo();
        logger.info(format("Found %d devices", midiInfo.length));
        for (Info info : midiInfo) {
            logger.info(format("Found midi device: %s (%s)",
                    info.getName(), info.getDescription()));
            if (info.getName().startsWith("CH345")) {
                MidiDevice device = MidiSystem.getMidiDevice(info);
                logger.info(format("Device class: %s", device.getClass().getName()));
                try {
                    this.receiver = device.getReceiver();
                    this.receiverDevice = device;
                    this.receiverDevice.open();
                    logger.info("Found a receiver");
                } catch (Exception e) {
                    logger.info("No receiver found");
                }
            }
        }
        if (receiver == null) {
            throw new Exception("No CH345 receiver found");
        }
        logger.info("Opening sequencer");
        this.sequencer = MidiSystem.getSequencer();
        this.sequencer.open();
        this.transmitter = sequencer.getTransmitter();
        this.transmitter.setReceiver(this.receiver);
    }

    public void playMozart() throws Exception {
        logger.info("Playing Mozart");
        if (new File("/home/chip/mz_311_1.mid").exists()) {
            if (receiver != null) {
                InputStream is = new BufferedInputStream(new FileInputStream("/home/chip/mz_311_1.mid"));
                sequencer.setSequence(is);
                logger.info("Staring sequencer");
                sequencer.start();
            }
        }
    }

    @Override
    public void destroy() throws Exception {
        logger.info("Closing midi devices");
        sequencer.close();
        receiver.close();
    }

}
