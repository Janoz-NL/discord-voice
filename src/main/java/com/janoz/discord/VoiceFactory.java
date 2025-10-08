package com.janoz.discord;

import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.JDA;

/**
 * @deprecated use builder instead
 */
@SuppressWarnings("unused")
@UtilityClass
@Deprecated(since = "0.3.2", forRemoval = true)
public class VoiceFactory {

    public static VoiceContext createVoiceContext(String token) {
        return VoiceContext.builder().token(token).build();
    }

    public static VoiceContext createVoiceContext(JDA jda) {
        return VoiceContext.builder().jda(jda).build();
    }

    public static VoiceContext createVoiceContextMock() {
        return VoiceContext.builder().asMock().build();
    }
}
