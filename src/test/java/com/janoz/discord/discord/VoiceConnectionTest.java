package com.janoz.discord.discord;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.managers.AudioManager;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VoiceConnectionTest {


    VoiceConnection cut;

    @Mock
    AudioPlayer player;
    @Mock
    AudioManager audioManager;

    AudioPlayerSendHandler sendHandler;

    long previousInteraction;

    @BeforeEach
    void setup() throws InterruptedException{
        Guild guild = mock(Guild.class);
        when(guild.getAudioManager()).thenReturn(audioManager);
        AudioPlayerManager playerManager = mock(AudioPlayerManager.class);
        when(playerManager.createPlayer()).thenReturn(player);
        ArgumentCaptor<AudioPlayerSendHandler> sendHandlerCaptor = ArgumentCaptor.forClass(AudioPlayerSendHandler.class);
        doNothing().when(audioManager).setSendingHandler(sendHandlerCaptor.capture());
        cut = new VoiceConnection(guild, playerManager);
        sendHandler = sendHandlerCaptor.getValue();
        previousInteraction = cut.getLastInteraction();
        Thread.sleep(10);
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(player, audioManager);
    }

    @Test
    void testSetup() {
        assertThat(cut.getLastInteraction()).isCloseTo(System.currentTimeMillis(), Offset.offset(100L));
    }

    @Test
    void testStop() {
        cut.stop();
        verify(player).stopTrack();
    }

    @Test
    void testIsConnected() {
        when(audioManager.isConnected()).thenReturn(true);
        assertThat(cut.isConnected()).isTrue();
        when(audioManager.isConnected()).thenReturn(false);
        assertThat(cut.isConnected()).isFalse();
    }

    @Test
    void testDisconnect() {
        cut.disconnect();
        verify(player).stopTrack();
        verify(audioManager).closeAudioConnection();
    }

    @Test
    void testConnectedToThisChannel() {
        AudioChannelUnion currentVC = mock(AudioChannelUnion.class);
        when(currentVC.getIdLong()).thenReturn(1L);
        when(audioManager.getConnectedChannel()).thenReturn(currentVC);

        assertThat(cut.connectedToThisVoiceChannel(1L)).isTrue();
        assertThat(cut.connectedToThisVoiceChannel(2L)).isFalse();
    }

    @Test
    void testConnectedToOtherChannel() {
        AudioChannelUnion currentVC = mock(AudioChannelUnion.class);
        when(currentVC.getIdLong()).thenReturn(1L);
        when(audioManager.getConnectedChannel()).thenReturn(currentVC);

        assertThat(cut.connectedToOtherVoiceChannel(1L)).isFalse();
        assertThat(cut.connectedToOtherVoiceChannel(2L)).isTrue();
    }

    @Test
    void testConnectedToNoChannel() {
        when(audioManager.getConnectedChannel()).thenReturn(null);

        assertThat(cut.connectedToThisVoiceChannel(1L)).isFalse();
        assertThat(cut.connectedToOtherVoiceChannel(1L)).isFalse();
    }

    @Test
    void testConnectAlreadyConnected() {
        AudioChannelUnion currentVC = mock(AudioChannelUnion.class);
        when(currentVC.getIdLong()).thenReturn(1L);
        when(audioManager.getConnectedChannel()).thenReturn(currentVC);

        assertThat(cut.connect(1L)).isFalse();
    }

    @Test
    void testConnectNewChannel() {
        when(audioManager.getConnectedChannel()).thenReturn(null).thenReturn(null);

        VoiceChannel channel = mock(VoiceChannel.class);
        Guild guild = mock(Guild.class);
        when(audioManager.getGuild()).thenReturn(guild);
        when(guild.getVoiceChannelById(1L)).thenReturn(channel);
        ArgumentCaptor<VoiceChannel> channelCaptor = ArgumentCaptor.forClass(VoiceChannel.class);
        doNothing().when(audioManager).openAudioConnection(channelCaptor.capture());

        assertThat(cut.connect(1L)).isTrue();
        assertThat(channelCaptor.getValue()).isSameAs(channel);
    }

    @Test
    void testConnectAlreadyConnectedOtherChannel() {
        AudioChannelUnion currentVC = mock(AudioChannelUnion.class);
        when(currentVC.getIdLong())
                .thenReturn(2L)
                .thenReturn(2L);
        when(audioManager.getConnectedChannel())
                .thenReturn(currentVC)
                .thenReturn(currentVC);


        VoiceChannel channel = mock(VoiceChannel.class);
        Guild guild = mock(Guild.class);
        when(audioManager.getGuild()).thenReturn(guild);
        when(guild.getVoiceChannelById(1L)).thenReturn(channel);
        ArgumentCaptor<VoiceChannel> channelCaptor = ArgumentCaptor.forClass(VoiceChannel.class);
        doNothing().when(audioManager).openAudioConnection(channelCaptor.capture());


        assertThat(cut.connect(1L)).isTrue();

        assertThat(channelCaptor.getValue()).isSameAs(channel);
        verify(player).stopTrack();
        verify(audioManager).closeAudioConnection();
    }

    @Test
    void testGetActiveConnection() {
        AudioChannelUnion acu = mock(AudioChannelUnion.class);
        VoiceChannel vc = mock(VoiceChannel.class);
        when(acu.asVoiceChannel()).thenReturn(vc);
        when(audioManager.getConnectedChannel()).thenReturn(acu);

        assertThat(cut.getActiveConnection()).isSameAs(vc);
    }

    @Test
    void testGetActiveConnectionNull() {
        when(audioManager.getConnectedChannel()).thenReturn(null);

        assertThat(cut.getActiveConnection()).isNull();
    }

    @Test
    void testPlayNull() {
        cut.play(null);
        assertThat(cut.getLastInteraction()).isGreaterThan(previousInteraction);
    }

    @Test
    void testPlayInvalid() {
        DiscordSample sample = new DiscordSample();
        sample.setErrorMessage("I'm invalid");
        cut.play(sample);
        assertThat(cut.getLastInteraction()).isGreaterThan(previousInteraction);
    }

    @Test
    void testPlayValid() {
        AudioTrack src = mock(AudioTrack.class);
        AudioTrack clone = mock(AudioTrack.class);
        when(src.makeClone()).thenReturn(clone);
        doNothing().when(clone).setPosition(0);
        DiscordSample sample = new DiscordSample();
        sample.setSample(src);
        sample.setLength(0);
        previousInteraction = cut.getLastInteraction();
        cut.play(sample);
        verify(player).stopTrack();
        verify(player).startTrack(clone,true);
        assertThat(cut.getLastInteraction()).isGreaterThan(previousInteraction);
        verifyNoMoreInteractions(src, clone);
    }

}