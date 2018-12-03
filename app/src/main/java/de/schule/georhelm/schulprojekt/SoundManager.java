package de.schule.georhelm.schulprojekt;

import android.content.Context;
import android.media.MediaPlayer;

public class SoundManager {

    static MediaPlayer themeMusicIntroMP;
    static MediaPlayer themeMusicLoopMP;
    static boolean muted = false;

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

    public static boolean toggleMusic(Context context){
        if(muted){
            startThemeMusic(context);
        }else{
            stopThemeMusic();
        }
        return muted;
    }
}
