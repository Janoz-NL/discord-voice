package com.janoz.discord;

import com.janoz.discord.domain.Activity;
import com.janoz.discord.domain.Guild;
import com.janoz.discord.domain.Sample;
import com.janoz.discord.domain.VoiceChannel;
import net.dv8tion.jda.api.JDA;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DiscordServiceTest {

    DiscordServiceImpl cut = new DiscordServiceImpl();

    @BeforeEach
    void setUp() {
        cut.clear();
    }

    @Test
    void testConnect() {
        assertThat(cut.connect(getVoiceChannel(getGuild()))).isTrue();
        assertThat(cut.guildId).isEqualTo(1L);
        assertThat(cut.voiceChannelId).isEqualTo(2L);
        assertThat(cut.sampleId).isNull();
        assertThat(cut.method).isEqualTo("connect");
    }

    @Test
    void testDisconnect() {
        cut.disconnect(getGuild());
        assertThat(cut.guildId).isEqualTo(1L);
        assertThat(cut.voiceChannelId).isNull();
        assertThat(cut.sampleId).isNull();
        assertThat(cut.method).isEqualTo("disconnect");
    }

    @Test
    void testPlay() {
        cut.play(getSample(),getGuild());
        assertThat(cut.guildId).isEqualTo(1L);
        assertThat(cut.voiceChannelId).isNull();
        assertThat(cut.sampleId).isEqualTo("ID");
        assertThat(cut.method).isEqualTo("play1");
    }

    @Test
    void testConnectAndPlay() {
        cut.play(getSample(),getVoiceChannel(getGuild()));
        assertThat(cut.guildId).isEqualTo(1L);
        assertThat(cut.voiceChannelId).isEqualTo(2L);
        assertThat(cut.sampleId).isEqualTo("ID");
        assertThat(cut.method).isEqualTo("play2");
    }


    private Guild getGuild() {
        return Guild.builder()
                .id(1L)
                .build();
    }

    private VoiceChannel getVoiceChannel(Guild guild) {
        return VoiceChannel.builder()
                .id(2L)
                .guild(guild)
                .build();
    }

    private Sample getSample() {
        return new Sample() {
            @Override
            public String getId() {
                return "ID";
            }

            @Override
            public String getName() {
                return "";
            }
        };
    }


    static class DiscordServiceImpl implements DiscordService {

        Long guildId;
        Long voiceChannelId;
        String sampleId;
        String method;

        void clear() {
            guildId = null;
            voiceChannelId = null;
            sampleId = null;
            method = null;
        }

        @Override
        public void setBotActivity(Activity activity) {

        }

        @Override
        public Activity getBotActivity() {
            return null;
        }

        @Override
        public Collection<Guild> getGuilds() {
            return List.of();
        }

        @Override
        public boolean connect(long guildId, long voiceChannelId) {
            method = "connect";
            this.guildId = guildId;
            this.voiceChannelId = voiceChannelId;
            return true;
        }

        @Override
        public void disconnect(long guildId) {
            method = "disconnect";
            this.guildId = guildId;
        }

        @Override
        public void play(String sampleId, long guildId) {
            method = "play1";
            this.sampleId = sampleId;
            this.guildId = guildId;
        }

        @Override
        public void play(String sampleId, long guildId, long voiceChannelId) {
            method = "play2";
            this.sampleId = sampleId;
            this.guildId = guildId;
            this.voiceChannelId = voiceChannelId;
        }

        @Override
        public JDA getJda() {
            return null;
        }
    }
}