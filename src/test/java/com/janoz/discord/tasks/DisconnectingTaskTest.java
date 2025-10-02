package com.janoz.discord.tasks;

import com.janoz.discord.discord.VoiceConnection;
import com.janoz.discord.discord.VoiceConnectionService;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DisconnectingTaskTest {



    @Test
    void testRun() {
        VoiceConnection notConnected = mock(VoiceConnection.class);
        when(notConnected.isConnected()).thenReturn(false);
        VoiceConnection connected = mock(VoiceConnection.class);
        when(connected.isConnected()).thenReturn(true);
        when(connected.getLastInteraction()).thenReturn(System.currentTimeMillis()-1000L);
        VoiceConnection connectedButStale = mock(VoiceConnection.class);
        when(connectedButStale.isConnected()).thenReturn(true);
        when(connectedButStale.getLastInteraction()).thenReturn(System.currentTimeMillis()-(15*60*1000L) - 1L);
        when(connectedButStale.getActiveConnection()).thenReturn(mock(VoiceChannel.class)); //for logging

        VoiceConnectionService vcs = mock(VoiceConnectionService.class);
        when(vcs.getAllConnections()).thenReturn(List.of(notConnected, connected, connectedButStale));

        DisconnectingTask cut = new DisconnectingTask(vcs);
        cut.run();
        verify(connectedButStale).disconnect();

        verifyNoMoreInteractions(vcs,notConnected, connected, connectedButStale);

    }
}