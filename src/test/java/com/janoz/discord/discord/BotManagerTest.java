package com.janoz.discord.discord;

import com.janoz.discord.domain.Activity;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.managers.Presence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BotManagerTest {

    BotManager cut;
    @Mock
    JDA jda;
    @Mock
    Presence presence;
    @Captor
    ArgumentCaptor<net.dv8tion.jda.api.entities.Activity> activityCaptor;

    @BeforeEach
    void setUp() {
        cut = new BotManager(jda);
    }

    @Test
    void testSetActivityNull() {
        when(jda.getPresence()).thenReturn(presence);
        doNothing().when(presence).setActivity(activityCaptor.capture());

        cut.setActivity(null);

        assertThat(activityCaptor.getValue()).isNull();
        verifyNoMoreInteractions(presence,jda);
    }

    @Test
    void testSetActivity() {
        when(jda.getPresence()).thenReturn(presence);
        doNothing().when(presence).setActivity(activityCaptor.capture());

        cut.setActivity(Activity.builder()
                .type(Activity.ActivityType.WATCHING)
                .name("JUnitest run")
                .build());

        assertThat(activityCaptor.getValue()).satisfies( a -> {
                    assertThat(a.getType()).isEqualTo(net.dv8tion.jda.api.entities.Activity.ActivityType.WATCHING);
                    assertThat(a.getName()).isEqualTo("JUnitest run");
                });
        verifyNoMoreInteractions(presence,jda);
    }

    @Test
    void testGetActivity() {
        when(jda.getPresence()).thenReturn(presence);
        when(presence.getActivity()).thenReturn(net.dv8tion.jda.api.entities.Activity.playing("Playing JUnitest"));

        Activity actual = cut.getActivity();

        assertThat(actual).satisfies(a -> {
            assertThat(a.getType()).isEqualTo(Activity.ActivityType.PLAYING);
            assertThat(a.getName()).isEqualTo("Playing JUnitest");
        });
    }

    @Test
    void testGetActivityNull() {
        when(jda.getPresence()).thenReturn(presence);
        when(presence.getActivity()).thenReturn(null);

        Activity actual = cut.getActivity();

        assertThat(actual).isNull();
    }
}