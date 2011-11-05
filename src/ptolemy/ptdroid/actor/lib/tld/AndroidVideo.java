package ptolemy.ptdroid.actor.lib.tld;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import ptolemy.actor.NoRoomException;
import ptolemy.actor.injection.PortableContainer;
import ptolemy.data.ArrayToken;
import ptolemy.data.UnsignedByteToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.IllegalActionException;
import ptserver.actor.lib.Video;
import ptserver.actor.lib.VideoInterface;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.ViewGroup;

public class AndroidVideo implements VideoInterface {

    private SurfaceView view;
    private Camera camera;
    private Video video;
    private boolean sent;
    private byte[] data;
    private Parameters parameters;

    @Override
    public void init(Video video) {
        this.video = video;
    }

    public void place(PortableContainer container) {
        ViewGroup viewGroup = (ViewGroup) (container.getPlatformContainer());
        if (view == null) {
            view = new SurfaceView(viewGroup.getContext());

            //        int lHeight = LinearLayout.LayoutParams.FILL_PARENT;
            //        int lWidth = LinearLayout.LayoutParams.FILL_PARENT;
            viewGroup.addView(view);
            //        view.setBackgroundColor(Color.WHITE);
            view.getHolder().addCallback(new Callback() {

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    if (camera != null) {
                        camera.stopPreview();
                        camera.setPreviewCallback(null);
                        camera.release();
                        camera = null;
                    }
                }

                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    try {
                        camera = Camera.open();
                        System.out.println(camera.getParameters()
                                .getSupportedPreviewFormats());
                        parameters = camera.getParameters();
                        //parameters.setPreviewSize(320, 240);
                        parameters.setPreviewFrameRate(5);
                        camera.setParameters(parameters);
                        camera.setPreviewDisplay(holder);
                        camera.startPreview();
                        camera.setPreviewCallback(new PreviewCallback() {

                            int c = 0;

                            @Override
                            public void onPreviewFrame(byte[] data,
                                    Camera camera) {
                                synchronized (AndroidVideo.this) {
                                    if (c % 5 < 3) {
                                        sent = false;
                                        AndroidVideo.this.data = data;
                                        AndroidVideo.this.notifyAll();
                                    }
                                }
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format,
                        int width, int height) {
                }
            });
        }
    }

    public void initialize() throws IllegalActionException {

    }

    public void stop() {
        if (camera != null) {
            camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
        }
    }

    @Override
    public void fire() throws IllegalActionException {
        byte[] d;
        synchronized (this) {
            d = this.data;
            while (sent) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                }
            }
            sent = true;
        }
        YuvImage im = new YuvImage(d, ImageFormat.NV21,
                parameters.getPreviewSize().width,
                parameters.getPreviewSize().height, null);
        Rect r = new Rect(0, 0, parameters.getPreviewSize().width,
                parameters.getPreviewSize().height);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        im.compressToJpeg(r, parameters.getJpegQuality(), baos);

        byte[] result = baos.toByteArray();
        UnsignedByteToken[] tokens = new UnsignedByteToken[result.length];
        for (int i = 0; i < result.length; i++) {
            tokens[i] = new UnsignedByteToken(result[i]);
        }

        try {
            video._output.send(0,
                    new ArrayToken(BaseType.UNSIGNED_BYTE, tokens));
        } catch (NoRoomException e) {
        } catch (IllegalActionException e) {
        }
    }

}
