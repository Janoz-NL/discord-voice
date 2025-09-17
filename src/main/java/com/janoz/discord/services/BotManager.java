package com.janoz.discord.services;

import com.janoz.discord.domain.Activity;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;

@RequiredArgsConstructor
public class BotManager {

    private final JDA jda;

    public void setActivity(Activity activity) {
        if (activity == null) {
            jda.getPresence().setActivity(null);
        } else {
            jda.getPresence().setActivity(
                    net.dv8tion.jda.api.entities.Activity.of(
                            net.dv8tion.jda.api.entities.Activity.ActivityType.fromKey(activity.getType().ordinal()),
                            activity.getName()));
        }
    }

    public Activity getActivity() {
        net.dv8tion.jda.api.entities.Activity activity = jda.getPresence().getActivity();
        if (activity == null) return null;
        return Activity.builder()
                .name(activity.getName())
                .type(Activity.ActivityType.values()[activity.getType().getKey()])
                .build();
    }

}
