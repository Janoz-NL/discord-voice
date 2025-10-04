package com.janoz.discord.tasks;

import com.janoz.discord.discord.VoiceConnection;
import com.janoz.discord.discord.VoiceConnectionService;
import lombok.extern.slf4j.Slf4j;

import java.util.Timer;
import java.util.TimerTask;

@Slf4j
public class DisconnectingTask extends TimerTask{

    private static final long MAX_IDLE_TIME = 15*60*1000L;
    private static final long CHECK_INTERVAL = 5*60*1000L;

    private final VoiceConnectionService voiceConnectionService;

    public static void startRunning(VoiceConnectionService voiceConnectionService) {
        new Timer(true).schedule(new DisconnectingTask(voiceConnectionService), CHECK_INTERVAL, CHECK_INTERVAL);
    }

    DisconnectingTask(VoiceConnectionService voiceConnectionService) {
        this.voiceConnectionService = voiceConnectionService;
    }

    @Override
    public void run() {
        final long current = System.currentTimeMillis();
        voiceConnectionService.getAllConnections().stream()
                .filter(VoiceConnection::isConnected)
                .filter(vc -> vc.getLastInteraction() + MAX_IDLE_TIME < current)
                .forEach(VoiceConnection::disconnect);
    }
}
