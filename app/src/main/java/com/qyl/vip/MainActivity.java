package com.qyl.vip;

import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.qyl.vip.crypt.Crypt;
import com.qyl.vip.crypt.DefaultCrypt;
import com.qyl.vip.crypt.DefaultProxyProcessor;
import com.qyl.vip.download.DownloadCallBack;
import com.qyl.vip.download.exception.DownloadException;
import com.qyl.vip.download.DownloadManager;
import com.qyl.vip.download.DownloadRequest;
import com.qyl.vip.crypt.DefaultProcessor;
import com.qyl.vip.proxy.VideoProxyServer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView statusTv;
    private Button downloadBtn;
    private Button playBtn;
    private SurfaceView surfaceView;

    private String testUrl = "http://download.3g.joy.cn/video/236/60236937/1451280942752_hd.mp4";
    private String testUrl2 = "http://download.3g.joy.cn/video/236/60236853/1450837945724_hd.mp4";


    private File mDownloadDir;

    private String videoPath = null;
    private long startTime = 0;

    private Crypt mCrypt;

    private Surface mSurface = null;
    private SurfaceHolder mSurfaceHolder;
    private MediaPlayer mediaPlayer;
    private final SurfaceHolder.Callback mSurfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Log.d(TAG, "surfaceChanged");
            try {
                if (mediaPlayer != null) {
                    final Surface newSurface = holder.getSurface();
                    mSurface = newSurface;
                    mediaPlayer.setDisplay(holder);
                    mediaPlayer.setScreenOnWhilePlaying(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Log.d(TAG, "surfaceCreated");
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.d(TAG, "surfaceDestroyed");
            if (mediaPlayer != null) {
                mediaPlayer.setDisplay(null);
                mSurface = null;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        statusTv = (TextView) findViewById(R.id.statusTv);
        downloadBtn = (Button) findViewById(R.id.downloadBtn);
        playBtn = (Button) findViewById(R.id.playBtn);
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);

        mCrypt = new DefaultCrypt();

        statusTv.setText("未下载");

        mDownloadDir = getExternalDir();

        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTime = SystemClock.uptimeMillis();
                DownloadRequest request = new DownloadRequest.Builder()
                        .setUri(testUrl)
                        .setFolder(mDownloadDir)
                        .setName(System.currentTimeMillis() + ".tmp")
                        .setProcessor(new DefaultProcessor(mCrypt))
                        .build();
                DownloadManager.getInstance().download(request, new DownloadCallBack() {
                    @Override
                    public void onStarted() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                statusTv.setText("onStarted...");
                            }
                        });
                    }

                    @Override
                    public void onConnecting() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                statusTv.setText("onConnecting...");
                            }
                        });
                    }

                    @Override
                    public void onConnected(final long total, final boolean isRangeSupport) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                statusTv.setText("onConnected...total=" + total + ",isRangeSupport=" + isRangeSupport );
                            }
                        });
                    }

                    @Override
                    public void onProgress(final long finished, final long total, final int progress) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                statusTv.setText("onConnected...finished=" + finished + ",total=" + total + ",progress=" + progress );
                            }
                        });
                    }

                    @Override
                    public void onCompleted(final String path) {
                        final long cost = (SystemClock.uptimeMillis() - startTime) / 1000;
                        videoPath = path;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                statusTv.setText("onCompleted..." + path + "花费时间:" + cost + "s");
                            }
                        });
                    }

                    @Override
                    public void onDownloadPaused() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                statusTv.setText("onDownloadPaused...");
                            }
                        });
                    }

                    @Override
                    public void onDownloadCanceled() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                statusTv.setText("onDownloadCanceled...");
                            }
                        });
                    }

                    @Override
                    public void onFailed(final DownloadException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                statusTv.setText("onFailed..." + e.toString());
                            }
                        });
                    }
                });
            }
        });

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(videoPath)) {
                    playVideo();
                }else {
                    Toast.makeText(MainActivity.this, "下载未完成", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mSurfaceHolder = surfaceView.getHolder();
        mSurfaceHolder.addCallback(mSurfaceCallback);

    }

    @Override
    protected void onStop() {
        super.onStop();
        pauseVideo();
    }

    private void playVideo() {
        videoPlayEnd();

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

            }
        });
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                //mediaPlayer.setLooping(true);
                mediaPlayer.start();

                scaleVideoView();
            }
        });
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, final int what, final int extra) {
                Log.e(TAG, "onError..." + what + "," + extra);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,
                                "播放出错啦!" + what +"," + extra, Toast.LENGTH_SHORT)
                                .show();
                    }
                });
                return false;
            }
        });
        mediaPlayer.setScreenOnWhilePlaying(true);

        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");

        try {
            if (mSurfaceHolder != null) {
                mediaPlayer.setDisplay(mSurfaceHolder);
                mediaPlayer.setScreenOnWhilePlaying(true);
            } else {
                return;
            }

            mediaPlayer.setDataSource(getProxyPath());
            mediaPlayer.prepareAsync();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void pauseVideo() {
        videoPlayEnd();
    }

    private void scaleVideoView() {
        if (mediaPlayer == null || mediaPlayer.getVideoHeight() <= 0 || surfaceView == null) {
            return;
        }

        WindowManager wm = getWindowManager();
        int sw = wm.getDefaultDisplay().getWidth();
        int sh = wm.getDefaultDisplay().getHeight();
        int videoWidth = mediaPlayer.getVideoWidth();
        int videoHeight = mediaPlayer.getVideoHeight();
        int visibleWidth = 0;
        int visibleHeight = 0;

        Log.i(TAG, "visibleWidth=" +visibleWidth +",visibleHeight=" + visibleHeight+ "videoWidth=" + videoWidth + ",videoHeight=" + videoHeight);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            visibleWidth = sw > sh ? sh : sw;
            visibleHeight = (int) Math.ceil(visibleWidth * videoHeight / videoWidth);
        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {

            if (videoHeight * sw > videoWidth * sh) {
                visibleHeight = sh;
                visibleWidth = (int) Math.ceil(videoWidth * visibleHeight / videoHeight);
            } else {
                visibleWidth = sw;
                visibleHeight = (int) Math.ceil(visibleWidth * videoHeight / videoWidth);
            }
        }

        ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();
        lp.width = visibleWidth;
        lp.height = visibleHeight;
        surfaceView.setLayoutParams(lp);

        surfaceView.invalidate();
    }

    private void videoPlayEnd() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private String getProxyPath() {
        if (!TextUtils.isEmpty(videoPath)) {
            VideoProxyServer proxy = VIPApplication.getProxy(this);
            if (proxy == null) {
                return videoPath;
            }

            proxy.registerProcessor(new DefaultProxyProcessor(mCrypt), videoPath);
            String result = proxy.getProxyUrl(videoPath);
            Log.i(TAG, "getProxyPath=" + result);
            return result;
        }
        return videoPath;
    }

    private static File getExternalDir() {
        String path = Environment.getExternalStorageDirectory().toString() + "/VipVideo/";

        File dirPath = new File(path);
        if (!dirPath.exists()) {
            dirPath.mkdirs();
        }
        return dirPath;
    }

}
