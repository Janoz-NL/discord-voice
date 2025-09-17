package com.janoz.discord.services;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor
public class VoiceConnectionService {

    private final Map<Long, VoiceConnection> connections = new HashMap<>();

    private final JDA jda;

    private final AudioPlayerManager audioPlayerManager;

    public VoiceConnection getConnection(Long guildId) {
        if (!connections.containsKey(guildId)) {
            Guild guild = Objects.requireNonNull(jda.getGuildById(guildId));
            connections.put(guildId, new VoiceConnection(guild, audioPlayerManager));
        }
        return connections.get(guildId);
    }

    public Collection<VoiceConnection> getAllConnections() {
        return connections.values();
    }

}
