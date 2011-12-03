package ptolemy.ptdroid.actor.lib.tld;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import ptolemy.actor.NoRoomException;
import ptolemy.actor.injection.PortableContainer;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.StringAttribute;
import ptserver.actor.lib.tld.Video;
import ptserver.actor.lib.tld.VideoInterface;
import ptserver.data.ByteArrayToken;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class AndroidVideo implements VideoInterface {

    private SurfaceView view;
    private Camera camera;
    private Video video;
    private Box box;
    private boolean sent;
    private byte[] data;
    private Parameters parameters;
    private int videoWidth = 320;
    private int videoHeight = 240;

    public void init(Video video) {
        this.video = video;
    }

    public void place(PortableContainer container) {
        ViewGroup viewGroup = (ViewGroup) (container.getPlatformContainer());
        if (view == null) {
            view = new SurfaceView(viewGroup.getContext());
            box = new Box(viewGroup.getContext());

            int lHeight = LinearLayout.LayoutParams.FILL_PARENT;
            int lWidth = LinearLayout.LayoutParams.FILL_PARENT;
            viewGroup.addView(view, lHeight, lWidth);
            viewGroup.addView(box);
            view.setOnTouchListener(box);
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
                        parameters.setPreviewSize(videoWidth, videoHeight);
                        parameters.setPreviewFrameRate(10);
                        parameters.setJpegQuality(25);
                        camera.setParameters(parameters);
                        camera.setPreviewDisplay(holder);
                        camera.startPreview();
                        camera.setPreviewCallback(new PreviewCallback() {

                            @Override
                            public void onPreviewFrame(byte[] data,
                                    Camera camera) {
                                box.invalidate();
                                synchronized (AndroidVideo.this) {
                                    //if (c % 10 == 0) {
                                    sent = false;
                                    AndroidVideo.this.data = data;
                                    AndroidVideo.this.notifyAll();
                                    //}
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

        try {
            video._output.send(0, new ByteArrayToken(result));
        } catch (NoRoomException e) {
            e.printStackTrace();
        } catch (IllegalActionException e) {
            e.printStackTrace();
        }
    }

    public void updateBoundingBox(float x1, float y1, float x2, float y2) {
        float hratio = ((float) view.getWidth()) / videoWidth;
        float vratio = ((float) view.getHeight()) / videoHeight;
        box.x1 = x1 * hratio;
        box.x2 = x2 * hratio;
        box.y1 = y1 * vratio;
        box.y2 = y2 * vratio;
    }

    class Box extends View implements OnTouchListener {
        int k;
        float x1, y1, x2, y2;

        public Box(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            Paint paint = new Paint();
            paint.setStyle(Style.STROKE);
            paint.setStrokeWidth(5);
            paint.setARGB(255, 255, 0, 0);
            RectF rect = new RectF(x1, y1, x2, y2);
            canvas.drawRect(rect, paint);
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getPointerCount() > 1) {
                x1 = event.getX(0);
                y1 = event.getY(0);
                x2 = event.getX(1);
                y2 = event.getY(1);
                for (int i = 0; i < event.getPointerCount(); i++) {
                    if (x1 > event.getX(i))
                        x1 = event.getX(i);
                    if (y1 > event.getY(i))
                        y1 = event.getY(i);
                    if (x2 < event.getX(i))
                        x2 = event.getX(i);
                    if (y2 < event.getY(i))
                        y2 = event.getY(i);
                }
            }
            CompositeEntity container = (CompositeEntity) video.getContainer();
            StringAttribute bb = (StringAttribute) container
                    .getAttribute("bb.expression");

            float hratio = (videoWidth / (float) view.getWidth());
            float vratio = (videoHeight / (float) view.getHeight());
            try {
                bb.setExpression(String.format("[%f, %f, %f, %f]", x1 * hratio,
                        y1 * hratio, x2 * vratio, y2 * vratio));
                bb.validate();
            } catch (IllegalActionException e) {
                throw new IllegalStateException(e);
            }
            return true;
        }
    }
}
