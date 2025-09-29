package com.janoz.discord;

import com.janoz.discord.impl.Voice;
import com.janoz.discord.impl.VoiceMock;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.JDA;

@SuppressWarnings("unused")
@UtilityClass
public class VoiceFactory {

    public static VoiceContext createVoiceContext(String token) {
        return new Voice(token);
    }

    public static VoiceContext createVoiceContext(JDA jda) {
        return new Voice(jda);
    }

    public static VoiceContext createVoiceContextMock() {
        return new VoiceMock();
    }
}
