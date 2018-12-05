package de.schule.georhelm.schulprojekt;

import android.content.Context;
import android.media.MediaPlayer;

public class SoundManager {

    //#region properties
    static MediaPlayer themeMusicIntroMP;
    static MediaPlayer themeMusicLoopMP;
    static boolean muted = false;
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
    //#endregion public static methods
}
