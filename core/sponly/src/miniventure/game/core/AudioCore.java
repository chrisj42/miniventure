package miniventure.game.core;

import miniventure.game.util.MyUtils;
import miniventure.game.world.WorldObject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class AudioCore {
	
	public static final float SOUND_RADIUS = 10; // 10 tiles
	// private static final HashMap<String, SoundEffect> soundEffects = new HashMap<>();
	// public static boolean PLAY_MUSIC = false;
	private static Music song;
	
	private AudioCore() {}
	
	public enum SoundEffect {
		
		PLAYER_HURT(false),
		PLAYER_PICKUP(false),
		PLAYER_SWING(false),
		ENTITY_HURT,
		TILE_BREAK,
		TILE_HIT;
		
		// private final String soundName;
		private final Sound sound;
		private final boolean checkDist;
		
		SoundEffect() { this(true); }
		SoundEffect(boolean checkDist) {
			this.checkDist = checkDist;
			final String soundName = "audio/effects/" + name().toLowerCase().replace("_", "/") + ".wav";
			Sound s = null;
			try {
				s = Gdx.audio.newSound(Gdx.files.internal(soundName));
			} catch(GdxRuntimeException e) {
				MyUtils.error("error loading sound '"+soundName+"'; not playing.", false);
			}
			this.sound = s;
		}
		
		public void play(WorldObject source) {
			if(checkDist && source.distance(source.getLevel().getPlayer()) > SOUND_RADIUS)
				return;
			
			if(sound != null)
				sound.play();
		}
	}
	
	public enum MusicTrack {
		
		GAME, TITLE;
		
		private final Music song;
		
		MusicTrack() {
			final String songName = "audio/music/"+name().toLowerCase()+".mp3";
			Music s = null;
			try {
				s = Gdx.audio.newMusic(Gdx.files.internal(songName));
			} catch(GdxRuntimeException e) {
				MyUtils.error("failed to load music track "+songName+"; will not play.");
			}
			this.song = s;
		}
		
		// TODO not sure how music is going to be used in the game yet, so for now this won't be very functional. But when music is added proper, I'll look into this again.
		public void play() {/* play(null); }
		public void play(OnCompletionListener completionListener) {*/
			stopMusic();
			AudioCore.song = song;
			song.stop(); // not sure if songs auto reset after ending, so this should reset them
			song.play();
		}
	}
	
	public static void stopMusic() {
		if(song != null) {
			song.stop();
			song = null;
		}
	}
}
