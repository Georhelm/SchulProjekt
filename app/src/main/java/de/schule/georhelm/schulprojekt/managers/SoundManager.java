package de.schule.georhelm.schulprojekt.managers;

import android.content.Context;
import android.media.MediaPlayer;

import de.schule.georhelm.schulprojekt.R;

public class SoundManager {

    //#region properties
    private static MediaPlayer themeMusicIntroMP;
    private static MediaPlayer themeMusicLoopMP;
    private static MediaPlayer crashPlayer;
    private static boolean muted = false;
    private static boolean playedCrashsound = false;
    //#endregion properties

    //#region public static methods
    /**

     * Starts the theme music and loops it.
     * @param context Context from which this method is called.
     */
    public static void startThemeMusic(Context context){
        muted = false;
        themeMusicIntroMP = MediaPlayer.create(context, R.raw.medievalsongintro);
        themeMusicLoopMP = MediaPlayer.create(context, R.raw.medievalsongloop);

        themeMusicLoopMP.setLooping(true);
        themeMusicIntroMP.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                themeMusicLoopMP.start();
                themeMusicIntroMP.stop();
            }
        });
        themeMusicIntroMP.start();
    }
    /**
     * Stops music from playing.
     */
    public static void stopThemeMusic(){
        muted = true;
        if(themeMusicIntroMP != null && themeMusicLoopMP != null){
            themeMusicLoopMP.stop();
            themeMusicIntroMP.stop();
            themeMusicLoopMP.release();
            themeMusicIntroMP.release();
            themeMusicIntroMP = null;
            themeMusicLoopMP = null;
        }

    }
    /**
     * Toggles music on and off and returns wheter it is then muted or not.
     * @param context Context from which this method is called.
     * @return Returns the state after toggling.
     */
    public static boolean toggleMusic(Context context){
        if(muted){
            startThemeMusic(context);
        }else{
            stopThemeMusic();
        }
        return muted;
    }

    public static void playCrashSound(Context context){
        if(!muted && !playedCrashsound){
            playedCrashsound = true;
            crashPlayer =  MediaPlayer.create(context, R.raw.metalcrash);
            crashPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    crashPlayer.stop();
                    crashPlayer.release();
                    crashPlayer = null;
                }
            });
            crashPlayer.start();
        }
    }

    public static void resetCrashSound(){
        playedCrashsound = false;
    }
    //#endregion public static methods
}
