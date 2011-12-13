/*
 Android implementation of VideoInterface that shows a camera
 output in a view, allows selection of initial bounding box and 
 overlays bounding box on top of a tracked object.
 
 Copyright (c) 2011 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY
 */
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

//////////////////////////////////////////////////////////////////////////
//// AndroidVideo
/**
 * Android implementation of VideoInterface that shows a camera
 * output in a view, allows selection of initial bounding box and 
 * overlays bounding box on top of a tracked object.
 * @author Anar Huseynov
 * @version $Id: AndroidVideo.java 179 2011-12-09 04:52:42Z ahuseyno $ 
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (ahuseyno)
 * @Pt.AcceptedRating Red (ahuseyno)
 */
public class AndroidVideo implements VideoInterface {

    /** 
     * Initialized the implementation of the interface.
     * @param video the video actor holding the implementation.
     * @see ptserver.actor.lib.tld.VideoInterface#init(ptserver.actor.lib.tld.Video)
     */
    public void init(Video video) {
        this._video = video;
    }

    /** 
     * Places the video into provided container along with needed controls.
     * @param container the container to place.
     * @see ptolemy.actor.injection.PortablePlaceable#place(ptolemy.actor.injection.PortableContainer)
     */
    public void place(PortableContainer container) {
        ViewGroup viewGroup = (ViewGroup) (container.getPlatformContainer());
        if (_view == null) {
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

            _btnRecord = new Button(viewGroup.getContext());
            _btnStop = new Button(viewGroup.getContext());
            _btnTrain = new Button(viewGroup.getContext());

            _btnRecord.setText("Record");
            buttons.addView(_btnRecord, new LinearLayout.LayoutParams(100, 50));
            _btnRecord.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    _recording = true;
                    _training = false;
                    _images.clear();
                    _btnRecord.setEnabled(false);
                    _btnStop.setEnabled(true);
                    _btnTrain.setEnabled(false);
                }
            });

            _btnStop.setEnabled(false);
            _btnStop.setText("Stop");
            _btnStop.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    _recording = false;
                    _btnRecord.setEnabled(true);
                    _btnStop.setEnabled(false);
                    _btnTrain.setEnabled(true);
                }
            });
            buttons.addView(_btnStop, new LinearLayout.LayoutParams(100, 50));

            _btnTrain.setText("Train");
            _btnTrain.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    _training = true;
                    _btnTrain.setEnabled(false);
                }
            });
            _btnTrain.setEnabled(false);
            buttons.addView(_btnTrain, new LinearLayout.LayoutParams(100, 50));

            _view = new SurfaceView(viewGroup.getContext());
            _box = new Box(viewGroup.getContext());
            int lHeight = LinearLayout.LayoutParams.FILL_PARENT;
            int lWidth = LinearLayout.LayoutParams.FILL_PARENT;
            frameLayout.addView(_view, lHeight, lWidth);
            frameLayout.addView(_box, lHeight, lWidth);
            _view.setOnTouchListener(_box);
            //        view.setBackgroundColor(Color.WHITE);
            _view.getHolder().addCallback(new Callback() {

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    if (_camera != null) {
                        _camera.stopPreview();
                        _camera.setPreviewCallback(null);
                        _camera.release();
                        _camera = null;
                    }
                }

                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    try {
                        _camera = Camera.open();
                        _parameters = _camera.getParameters();
                        _parameters.setPreviewSize(_videoWidth, _videoHeight);
                        _parameters.setPreviewFrameRate(15);
                        _parameters.setJpegQuality(25);
                        _camera.setParameters(_parameters);
                        _camera.setPreviewDisplay(holder);
                        _camera.startPreview();
                        _camera.setPreviewCallback(new PreviewCallback() {

                            @Override
                            public void onPreviewFrame(byte[] data,
                                    Camera camera) {
                                _box.invalidate();
                                synchronized (AndroidVideo.this) {
                                    _sent = false;
                                    AndroidVideo.this._data = data;
                                    AndroidVideo.this.notifyAll();
                                    if (_recording) {
                                        _images.add(data);
                                        if (_images.size() > _MAX_TRAINING) {
                                            _recording = false;
                                            _btnRecord.setEnabled(true);
                                            _btnStop.setEnabled(false);
                                            _btnTrain.setEnabled(true);
                                        }
                                    }
                                }
                            }
                        });

                        updateBoundingBox((_videoWidth - _BOX_SIZE) / 2, (_videoHeight - _BOX_SIZE) / 2,
                                (_videoWidth - _BOX_SIZE) / 2 + _BOX_SIZE, (_videoHeight - _BOX_SIZE) / 2 + _BOX_SIZE);
                        _sendBoundingBoxCoordinates();
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

    /**
     * Callback for initializing the interface.
     * @see ptserver.actor.lib.tld.VideoInterface#initialize()
     */
    public void initialize() throws IllegalActionException {
    }

    /** 
     * Callback for stopping the interface.
     * @see ptserver.actor.lib.tld.VideoInterface#stop()
     */
    public void stop() {
        _training = false;
    }

    /** 
     * Callback after the actor has fired.
     * The interface will either write a new video frame or read 
     * it from the pre-recorded images.
     * @see ptserver.actor.lib.tld.VideoInterface#fire()
     */
    public void fire() throws IllegalActionException {
        byte[] d = null;
        if (_training) {
            byte[] image = _images.poll();
            if (image != null) {
                d = image;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            } else {
                _training = false;
                _btnTrain.setEnabled(false);
            }
        }
        if (!_training) {
            synchronized (this) {
                d = this._data;
                while (_sent) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                    }
                }
                _sent = true;
            }
        }
        byte[] result = _toJpeg(d);
        try {
            _video.output.send(0, new ByteArrayToken(result));
        } catch (NoRoomException e) {
            e.printStackTrace();
        } catch (IllegalActionException e) {
            e.printStackTrace();
        }
    }

    /**
     * Convert the byte stream to JPEG from NV21.
     * @param data
     * @return
     */
    private byte[] _toJpeg(byte[] data) {
        YuvImage im = new YuvImage(data, ImageFormat.NV21,
                _parameters.getPreviewSize().width,
                _parameters.getPreviewSize().height, null);
        Rect r = new Rect(0, 0, _parameters.getPreviewSize().width,
                _parameters.getPreviewSize().height);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        im.compressToJpeg(r, _parameters.getJpegQuality(), baos);

        byte[] result = baos.toByteArray();
        return result;
    }

    /** 
     * Update the bounding box coordinates.
     * @see ptserver.actor.lib.tld.VideoInterface#updateBoundingBox(float, float, float, float)
     */
    public void updateBoundingBox(float x1, float y1, float x2, float y2) {
        float hratio = ((float) _view.getWidth()) / _videoWidth;
        float vratio = ((float) _view.getHeight()) / _videoHeight;
        _box.x1 = x1 * hratio;
        _box.x2 = x2 * hratio;
        _box.y1 = y1 * vratio;
        _box.y2 = y2 * vratio;
    }

    /**
     * BoundingBox view that is overlayed on top of the video (surface view).
     */
    private class Box extends View implements OnTouchListener {
        public float x1, y1, x2, y2;

        /**
         * Creates new instance of the view.
         * @param context The context to use.
         */
        public Box(Context context) {
            super(context);
        }

        /* Callback that is called when the view is drawn.
         *  (non-Javadoc)
         * @see android.view.View#onDraw(android.graphics.Canvas)
         */
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (!_training) {
                Paint paint = new Paint();
                paint.setStyle(Style.STROKE);
                paint.setStrokeWidth(5);
                paint.setARGB(255, 255, 0, 0);
                RectF rect = new RectF(x1, y1, x2, y2);
                canvas.drawRect(rect, paint);
            }
        }

        /* Callback that is called when the view is touched.
         *  (non-Javadoc)
         * @see android.view.View.OnTouchListener#onTouch(android.view.View, android.view.MotionEvent)
         */
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
                _sendBoundingBoxCoordinates();
            }

            return true;
        }
    }

    /**
     * Send new bounding box coordinates by updating bb.expression parameter.
     */
    private void _sendBoundingBoxCoordinates() {
        CompositeEntity container = (CompositeEntity) _video.getContainer();
        StringAttribute bb = (StringAttribute) container
                .getAttribute("bb.expression");

        float hratio = (_videoWidth / (float) _view.getWidth());
        float vratio = (_videoHeight / (float) _view.getHeight());
        try {
            bb.setExpression(String.format("[%f, %f, %f, %f]", _box.x1 * hratio,
                    _box.y1 * hratio, _box.x2 * vratio, _box.y2 * vratio));
            bb.validate();
        } catch (IllegalActionException e) {
            throw new IllegalStateException(e);
        }
    }
    
    /**
     * The default box size.
     */
    private static final int _BOX_SIZE = 50;
    /**
     * Maximum frames to train on.
     */
    private static final int _MAX_TRAINING = 500;
    /**
     * The view holding the video.
     */
    private SurfaceView _view;
    /**
     * Reference to the camera object.
     */
    private Camera _camera;
    /**
     * The video actor that holds the interface.
     */
    private Video _video;
    /**
     * Reference to the box view displaying the bounding box.
     */
    private Box _box;
    /**
     * Flag indicating if the data was sent.
     */
    private boolean _sent;
    /**
     * Last video frame.
     */
    private byte[] _data;
    /**
     * Parameters of the camera.
     */
    private Parameters _parameters;
    /**
     * The video width.
     */
    private int _videoWidth = 320;
    /**
     * The video height.
     */
    private int _videoHeight = 240;
    /**
     * The video frames used for training.
     */
    private ConcurrentLinkedQueue<byte[]> _images = new ConcurrentLinkedQueue<byte[]>();
    /**
     * Flag indicating if video frames are being recorded now.
     */
    private boolean _recording = false;
    /**
     * Flag indicating if the training is performed now.
     */
    private boolean _training = false;
    /**
     * Reference to record button.
     */
    private Button _btnRecord;
    /**
     * Reference to stop button.
     */
    private Button _btnStop;
    /**
     * Reference to train button.
     */
    private Button _btnTrain;
}
