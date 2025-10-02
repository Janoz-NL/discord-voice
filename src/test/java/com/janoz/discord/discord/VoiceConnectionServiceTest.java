package com.janoz.discord.discord;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.managers.AudioManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VoiceConnectionServiceTest {

    VoiceConnectionService cut;
    @Mock
    JDA jda;
    @Mock
    AudioPlayerManager audioPlayerManager;


    @BeforeEach
    void setUp() {
        cut = new VoiceConnectionService(jda, audioPlayerManager);
    }

    @Test
    void testGetConnections() {
        Guild guild1 = mock(Guild.class);
        when(guild1.getAudioManager()).thenReturn(mock(AudioManager.class));
        when(jda.getGuildById(1L)).thenReturn(guild1);

        Guild guild2 = mock(Guild.class);
        when(guild2.getAudioManager()).thenReturn(mock(AudioManager.class));
        when(jda.getGuildById(2L)).thenReturn(guild2);

        VoiceConnection actual1 = cut.getConnection(1L);
        VoiceConnection actual2 = cut.getConnection(2L);
        VoiceConnection actual3 = cut.getConnection(1L);

        assertThat(actual1).isSameAs(actual3);

        assertThat(cut.getAllConnections()).containsExactlyInAnyOrder(actual1, actual2);
    }

}