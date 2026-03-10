package net.neoforged.neoforge.client.event;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.Music;
import org.jetbrains.annotations.Nullable;

/**
 * Fired to allow mods to change the background music selection.
 */
public class SelectMusicEvent extends net.neoforged.bus.api.Event {
    private final Music originalMusic;
    @Nullable
    private Music music;
    @Nullable
    private final SoundInstance playingMusic;

    public SelectMusicEvent(Music music, @Nullable SoundInstance playingMusic) {
        this.originalMusic = music;
        this.music = music;
        this.playingMusic = playingMusic;
    }

    public Music getOriginalMusic() { return originalMusic; }
    @Nullable
    public Music getMusic() { return music; }
    public void setMusic(@Nullable Music music) { this.music = music; }
    @Nullable
    public SoundInstance getPlayingMusic() { return playingMusic; }
}
