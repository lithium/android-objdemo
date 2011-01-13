package com.hlidskialf.android.objdemo;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.view.MotionEvent;
import android.view.GestureDetector;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import java.nio.FloatBuffer;

import com.hlidskialf.android.game.models.ObjModel;
import com.hlidskialf.android.game.util.Point3;
import com.hlidskialf.android.game.util.MathHelper;


public class DemoRendererView extends GLSurfaceView
{
    public static final int GRID_SIZE=8;

    Context mContext;
    DemoRenderer mRenderer;
    TouchHelper mTouchHelper;

    float mViewWidth, mViewHeight;

    ObjModel mModel;
    Point3 mOrigin;
    Point3 mRotate;
    Point3 mCamera;


    FloatBuffer mGrid;
    int mGridSize;




    public DemoRendererView(Context context) { this(context,null); }
    public DemoRendererView(Context context, AttributeSet attrs) 
    {
        super(context,attrs);
        mContext = context;

        try{
            mModel = ObjModel.loadFromStream(context.getResources().openRawResource(R.raw.dice));
        } catch (java.io.IOException e) {
            Log.v("DemoRendererView", "loading model: "+e);
        }

        mOrigin = new Point3(0f,0f,0f);
        mRotate = new Point3(0f,0f,0f);
        mCamera = new Point3(0f,3f,3.9f);

        mTouchHelper = new TouchHelper(context);

    }
    public void start()
    {
        mRenderer = new DemoRenderer();
        setRenderer(mRenderer);
    }






    @Override
    public boolean onTouchEvent(MotionEvent ev)
    {
        return mTouchHelper.onTouchEvent(ev);
    }

    private void drag_xy(float dx, float dy)
    {

        mOrigin.x += (dx/mViewWidth)*mGridSize;
        mOrigin.y -= (dy/mViewHeight)*mGridSize;

        float half = mGridSize/2;
        mOrigin.minmax(-half+.5f,-half+.5f,-half+.5f,half-.5f,half-.5f,half-.5f);
    }

    private void drag_xz(float dx, float dy)
    {
        mOrigin.x += (dx/mViewWidth)*mGridSize;
        mOrigin.z += (dy/mViewHeight)*mGridSize;

        float half = mGridSize/2;
        mOrigin.minmax(-half+.5f,-half+.5f,-half+.5f,half-.5f,half-.5f,half-.5f);
    }





    private class TouchHelper extends GestureDetector.SimpleOnGestureListener
    {
        GestureDetector gesture;
        int ptr1_id=-1, ptr2_id=-1;
        float last_x=-1, last_y=-1;

        public TouchHelper(Context context)
        {
            gesture = new GestureDetector(context, this);
        }

        @Override
        public boolean onDoubleTap(MotionEvent ev)
        {
            if (mRotationFinal != -1) 
                return true;

            int index = ev.getActionIndex();
            float x = ev.getX(index);
            float y = ev.getY(index);

            int col = (int)(x / mViewWidth * 3);
            int row = (int)(y / mViewHeight * 3);

            Log.v("ObjDem", "col,row:"+col+","+row);


            if (col == 0 && row == 0) {
                mRotationAxis = Z_AXIS;
                mRotationFinal = mRotate.z + 90;
                mRotationDelta = 1;
            }
            else
            if (col == 0 && row == 1) {
                mRotationAxis = Y_AXIS;
                mRotationFinal = mRotate.y - 90;
                mRotationDelta = -1;
            }
            else
            if (col == 1 && row == 0) {
                mRotationAxis = X_AXIS;
                mRotationFinal = mRotate.x - 90;
                mRotationDelta = -1;
            }
            else
            if (col == 1 && row == 2) {
                mRotationAxis = X_AXIS;
                mRotationFinal = mRotate.x + 90;
                mRotationDelta = 1;
            }
            else
            if (col == 2 && row == 1) {
                mRotationAxis = Y_AXIS;
                mRotationFinal = mRotate.y + 90;
                mRotationDelta = 1;
            }
            else
            if (col == 2 && row == 2) {
                mRotationAxis = Z_AXIS;
                mRotationFinal = mRotate.z - 90;
                mRotationDelta = -1;
            }





            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float vel_x, float vel_y)
        {
            float angle = MathHelper.angle_of_vector(vel_x, vel_y);

            Log.v("ObjDemo", "vel: "+vel_x+","+vel_y+" = "+angle);


            return true;
        }

        public boolean onTouchEvent(MotionEvent ev)
        {
            if (gesture.onTouchEvent(ev)) {
                ptr1_id = -1;
                ptr2_id = -1;
                last_x = -1f;
                last_y = -1f;
                return true;
            }


            int action = ev.getActionMasked();
            int index = ev.getActionIndex();
            int id = ev.getPointerId(index);
            int count = ev.getPointerCount();

            switch (action)
            {
                case MotionEvent.ACTION_DOWN:
                    ptr1_id = id;
                    break;

                case MotionEvent.ACTION_POINTER_DOWN:
                    ptr2_id = id;
                    break;

                case MotionEvent.ACTION_POINTER_UP:
                    ptr2_id = -1;
                    break;

                case MotionEvent.ACTION_UP:
                    ptr1_id = -1;
                    last_x = -1;
                    last_y = -1;
                    break;

                case MotionEvent.ACTION_MOVE:

                    float x,y;

                    if (count >= 2) {
                        x = ev.getX(index);
                        y = ev.getY(index);

                        if (last_x != -1) 
                            drag_xy(x - last_x, y - last_y);
                    }
                    else {
                        x = ev.getX(index);
                        y = ev.getY(index);

                        if (last_x != -1) 
                            drag_xz(x - last_x, y - last_y);
                    }

                    last_x = x;
                    last_y = y;


                    break;
            }
            return true;
        }

    }



    public final static int NONE=-1;
    public final static int X_AXIS=0;
    public final static int Y_AXIS=1;
    public final static int Z_AXIS=2;

    float mRotationFinal=-1f;
    float mRotationDelta=0f;
    int mRotationAxis=-1;



    private void tick()
    {
        if (mRotationFinal != -1) 
        {
            float close = Math.abs(mRotationDelta*2);
            switch (mRotationAxis)
            {
                case X_AXIS:
                    mRotate.x += mRotationDelta;
                    if (Math.abs(mRotationFinal - mRotate.x) <= close) {
                        mRotate.x = mRotationFinal;
                        mRotationFinal=-1;
                    }
                    break;
                case Y_AXIS:
                    mRotate.y += mRotationDelta;
                    if (Math.abs(mRotationFinal - mRotate.y) <= close) {
                        mRotate.y = mRotationFinal;
                        mRotationFinal=-1;
                    }
                    break;
                case Z_AXIS:
                    mRotate.z += mRotationDelta;
                    if (Math.abs(mRotationFinal - mRotate.z) <= close) {
                        mRotate.z = mRotationFinal;
                        mRotationFinal=-1;
                    }
                    break;
            }
        }

    }

    private class DemoRenderer implements GLSurfaceView.Renderer
    {
        public void onSurfaceCreated(GL10 gl, EGLConfig config)
        {
            gl.glClearColor(0f,0f,0f, 0.5f);
            gl.glEnable(GL10.GL_DEPTH_TEST);
            gl.glDepthFunc(GL10.GL_LEQUAL);
            gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);

            gl.glEnable(GL10.GL_TEXTURE_2D);
            gl.glShadeModel(GL10.GL_SMOOTH);


/*
            gl.glEnable(GL10.GL_LIGHTING);
            gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, new float[] {0.3f,0.3f,0.3f,1f}, 0);
            gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, new float[] {1f,1f,1f,1f}, 0);
            gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_SPECULAR, new float[] {1f,1f,1f,1f}, 0);
            gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, new float[] {-1f,10f,5f,1f}, 0);
            gl.glEnable(GL10.GL_LIGHT0);
*/


            build_grid(GRID_SIZE);
            mModel.bindTextures(mContext, gl);
        }
        public void onSurfaceChanged(GL10 gl, int w, int h)
        {
            mViewWidth = (float)w;
            mViewHeight = (float)h;
            gl.glViewport(0,0,w,h);

            gl.glMatrixMode(GL10.GL_PROJECTION);
            gl.glLoadIdentity();
            GLU.gluPerspective(gl, 60.0f, mViewWidth/mViewHeight, 0.1f, 100f);

            gl.glMatrixMode(GL10.GL_MODELVIEW);
            gl.glLoadIdentity();


        }
        public void onDrawFrame(GL10 gl)
        {
            tick();

            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
            gl.glPushMatrix();


            //position camera
            GLU.gluLookAt(gl, mCamera.x,mCamera.y,mCamera.z, mOrigin.x,mOrigin.y,mOrigin.z, 0f,1f,0f);


            //draw_grid
            draw_grid(gl);


            //draw_model
            gl.glPushMatrix();

                gl.glTranslatef(mOrigin.x, mOrigin.y, mOrigin.z);
                gl.glRotatef(mRotate.x, 1f, 0f, 0f);
                gl.glRotatef(mRotate.y, 0f, 1f, 0f);
                gl.glRotatef(mRotate.z, 0f, 0f, 1f);

                mModel.draw(gl);

                //draw_lights
                //gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, new float[] {-1f,10f,5f,1f}, 0);

            gl.glPopMatrix();


            gl.glPopMatrix();
        }


        private void build_grid(int GRID_SIZE)
        {
            float x,y,z;

            float i;

            float half = GRID_SIZE/2f;

            mGridSize = GRID_SIZE;
            mGrid = FloatBuffer.allocate((GRID_SIZE+1)*12*3);

            for (i=-half; i <= +half; i++)
            {
                mGrid.put(i); mGrid.put(-half); mGrid.put(+half);
                mGrid.put(i); mGrid.put(-half); mGrid.put(-half);
                mGrid.put(i); mGrid.put(+half); mGrid.put(-half);
                mGrid.put(i); mGrid.put(+half); mGrid.put(+half);

                mGrid.put(-half); mGrid.put(i); mGrid.put(+half);
                mGrid.put(-half); mGrid.put(i); mGrid.put(-half);
                mGrid.put(+half); mGrid.put(i); mGrid.put(-half);
                mGrid.put(+half); mGrid.put(i); mGrid.put(+half);

                mGrid.put(-half); mGrid.put(+half); mGrid.put(i);
                mGrid.put(-half); mGrid.put(-half); mGrid.put(i);
                mGrid.put(+half); mGrid.put(-half); mGrid.put(i);
                mGrid.put(+half); mGrid.put(+half); mGrid.put(i);
            }
            mGrid.rewind();


        }
        public void draw_grid(GL10 gl)
        {
            gl.glPushMatrix();

            int ofs=0;
            int i;

            gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mGrid);
            for (i=0; i <= mGridSize*3; i++)
            {
                gl.glDrawArrays(GL10.GL_LINE_LOOP, ofs, 4);
                ofs+=4;
            }

            gl.glPopMatrix();
        }


    }

}
