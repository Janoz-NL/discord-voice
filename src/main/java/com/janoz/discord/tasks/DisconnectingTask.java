package com.janoz.discord.tasks;

import com.janoz.discord.services.VoiceConnection;
import com.janoz.discord.services.VoiceConnectionService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DisconnectingTask {

    private static final long MAX_IDLE_TIME = 15*60*1000L;
    private static final long CHECK_INTERVAL = 5*60*1000L;

    public DisconnectingTask(VoiceConnectionService voiceConnectionService) {
        this.voiceConnectionService = voiceConnectionService;
    }

    private final VoiceConnectionService voiceConnectionService;

    public void run() {
        Thread runner = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(CHECK_INTERVAL);
                } catch (InterruptedException e) {
                    return;
                }
                disconnectIdleVoicechannels();
            }
        });
        runner.setDaemon(true);
        runner.start();
    }

    private void disconnectIdleVoicechannels() {
        final long current = System.currentTimeMillis();
        voiceConnectionService.getAllConnections().stream()
                .filter(VoiceConnection::isConnected)
                .filter(vc -> vc.getLastInteraction() + MAX_IDLE_TIME < current)
                .peek(vc -> log.info("Disconnecting from {}", vc.getActiveConnection().getName()))
                .forEach(VoiceConnection::disconnect);
    }
}
