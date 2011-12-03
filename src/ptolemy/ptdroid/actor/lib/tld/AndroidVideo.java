package ptolemy.ptdroid.actor.lib.tld;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

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
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
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
    private ConcurrentLinkedQueue<byte[]> images = new ConcurrentLinkedQueue<byte[]>();
    private boolean recording = false;
    private boolean training = false;
    private static int MAX_TRAINING = 500;
    private Button btnRecord;
    private Button btnStop;
    private Button btnTrain;

    public void init(Video video) {
        this.video = video;
    }

    public void place(PortableContainer container) {
        ViewGroup viewGroup = (ViewGroup) (container.getPlatformContainer());
        if (view == null) {
            LinearLayout linearLayout = new LinearLayout(viewGroup.getContext());
            FrameLayout frameLayout = new FrameLayout(viewGroup.getContext());
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            viewGroup.addView(linearLayout);
            linearLayout.addView(frameLayout, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.FILL_PARENT, 500));
            LinearLayout buttons = new LinearLayout(viewGroup.getContext());
            buttons.setOrientation(LinearLayout.HORIZONTAL);

            linearLayout.addView(buttons, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.FILL_PARENT, 50));

            btnRecord = new Button(viewGroup.getContext());
            btnStop = new Button(viewGroup.getContext());
            btnTrain = new Button(viewGroup.getContext());

            btnRecord.setText("Record");
            buttons.addView(btnRecord, new LinearLayout.LayoutParams(100, 50));
            btnRecord.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    recording = true;
                    training = false;
                    images.clear();
                    btnRecord.setEnabled(false);
                    btnStop.setEnabled(true);
                    btnTrain.setEnabled(false);
                }
            });

            btnStop.setEnabled(false);
            btnStop.setText("Stop");
            btnStop.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    recording = false;
                    btnRecord.setEnabled(true);
                    btnStop.setEnabled(false);
                    btnTrain.setEnabled(true);
                }
            });
            buttons.addView(btnStop, new LinearLayout.LayoutParams(100, 50));

            btnTrain.setText("Train");
            btnTrain.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    training = true;
                    btnTrain.setEnabled(false);
                }
            });
            btnTrain.setEnabled(false);
            buttons.addView(btnTrain, new LinearLayout.LayoutParams(100, 50));

            view = new SurfaceView(viewGroup.getContext());
            box = new Box(viewGroup.getContext());
            int lHeight = LinearLayout.LayoutParams.FILL_PARENT;
            int lWidth = LinearLayout.LayoutParams.FILL_PARENT;
            frameLayout.addView(view, lHeight, lWidth);
            frameLayout.addView(box, lHeight, lWidth);
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
                        parameters.setPreviewFrameRate(15);
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
                                    sent = false;
                                    AndroidVideo.this.data = data;
                                    AndroidVideo.this.notifyAll();
                                    if (recording) {
                                        images.add(data);
                                        if (images.size() > MAX_TRAINING) {
                                            recording = false;
                                            btnRecord.setEnabled(true);
                                            btnStop.setEnabled(false);
                                            btnTrain.setEnabled(true);
                                        }
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
        training = false;
    }

    public void fire() throws IllegalActionException {
        byte[] d = null;
        if (training) {
            byte[] image = images.poll();
            if (image != null) {
                d = image;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            } else {
                training = false;
                btnTrain.setEnabled(false);
            }
        }
        if (!training) {
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
        }
        byte[] result = toJpeg(d);
        try {
            video._output.send(0, new ByteArrayToken(result));
        } catch (NoRoomException e) {
            e.printStackTrace();
        } catch (IllegalActionException e) {
            e.printStackTrace();
        }
    }

    private byte[] toJpeg(byte[] data) {
        YuvImage im = new YuvImage(data, ImageFormat.NV21,
                parameters.getPreviewSize().width,
                parameters.getPreviewSize().height, null);
        Rect r = new Rect(0, 0, parameters.getPreviewSize().width,
                parameters.getPreviewSize().height);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        im.compressToJpeg(r, parameters.getJpegQuality(), baos);

        byte[] result = baos.toByteArray();
        return result;
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
            if (!training) {
                Paint paint = new Paint();
                paint.setStyle(Style.STROKE);
                paint.setStrokeWidth(5);
                paint.setARGB(255, 255, 0, 0);
                RectF rect = new RectF(x1, y1, x2, y2);
                canvas.drawRect(rect, paint);
            }
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
                sendBoundingBoxCoordinates();
            }

            return true;
        }
    }

    private void sendBoundingBoxCoordinates() {
        CompositeEntity container = (CompositeEntity) video.getContainer();
        StringAttribute bb = (StringAttribute) container
                .getAttribute("bb.expression");

        float hratio = (videoWidth / (float) view.getWidth());
        float vratio = (videoHeight / (float) view.getHeight());
        try {
            bb.setExpression(String.format("[%f, %f, %f, %f]", box.x1 * hratio,
                    box.y1 * hratio, box.x2 * vratio, box.y2 * vratio));
            bb.validate();
        } catch (IllegalActionException e) {
            throw new IllegalStateException(e);
        }
    }
}
