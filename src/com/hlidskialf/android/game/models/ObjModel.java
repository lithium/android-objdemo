package com.hlidskialf.android.game.models;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.StringTokenizer;
import javax.microedition.khronos.opengles.GL10;

import com.hlidskialf.android.game.util.Point3;


public class ObjModel
{

    public void bindTextures(Context context, GL10 gl)
    {
        Bitmap bitmap;

        try {
            InputStream is = context.getAssets().open("textures/"+mTextureName);
            bitmap = BitmapFactory.decodeStream(is);
            if (bitmap == null) {
                Log.v("ObjModel", "err loading bitmap!");
            }
        } catch (java.io.IOException e) {
            Log.v("ObjModel", "err loading tex: "+e.toString());
            return;
        }

        mTextures = new int[1];
        gl.glGenTextures(1, mTextures, 0);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextures[0]);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);

        Log.v("ObjModel", "loaded texture: "+mTextureName+" = "+mTextures[0]);

        bitmap.recycle();
    }

    public void draw(GL10 gl)
    {

        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mV);
        if (mVt != null) {
            if (mTextures != null) {
                gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextures[0]);
            }
            gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mVt);
        }
        if (mVn != null)
            gl.glNormalPointer(GL10.GL_FLOAT, 0, mVn);
        gl.glDrawArrays(GL10.GL_TRIANGLES, 0, mVertexCount);

    }

    public static ObjModel loadFromStream(InputStream is) throws IOException
    {
        return ObjLoader.loadFromStream(is);
    }


    /* private */
    private FloatBuffer mV;
    private FloatBuffer mVt;
    private FloatBuffer mVn;
    private int mVertexCount;
    private int mTextures[];
    private String mTextureName;

    private static class ObjLoader
    {

        public static ObjModel loadFromStream(InputStream is) throws IOException
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            ObjModel obj = new ObjModel();
            ArrayList<Point3> v = new ArrayList<Point3>();
            ArrayList<Point3> vt = new ArrayList<Point3>();
            ArrayList<Point3> vn = new ArrayList<Point3>();
            ArrayList<Face> f = new ArrayList<Face>();

            while(reader.ready()) 
            {
                String line = reader.readLine();
                if (line == null) 
                    break;

                StringTokenizer tok = new StringTokenizer(line);
                String cmd = tok.nextToken();

                if (cmd.equals("v")) {
                    v.add(read_point(tok));
                }
                else
                if (cmd.equals("vn")) {
                    vn.add(read_point(tok));
                }
                else
                if (cmd.equals("vt")) {
                    vt.add(read_point(tok));
                }
                else
                if (cmd.equals("f")) {
                    if (tok.countTokens() != 3)
                        throw new UnsupportedOperationException("Only triangles supported");

                    Face face = new Face(3);
                    while (tok.hasMoreTokens()) {
                        StringTokenizer face_tok = new StringTokenizer(tok.nextToken(), "/");

                        int v_idx = -1;
                        int vt_idx = -1;
                        int vn_idx = -1;
                        v_idx = Integer.parseInt(face_tok.nextToken());
                        if (face_tok.hasMoreTokens()) vt_idx = Integer.parseInt(face_tok.nextToken());
                        if (face_tok.hasMoreTokens()) vn_idx = Integer.parseInt(face_tok.nextToken());


                        //Log.v("objmodel", "face: "+v_idx+"/"+vt_idx+"/"+vn_idx);

                        face.addVertex(
                            v.get(v_idx-1),
                            vt_idx == -1 ? null : vt.get(vt_idx-1),
                            vn_idx == -1 ? null : vn.get(vn_idx-1)
                        );
                    }
                    f.add(face);
                }
                else
                if (cmd.equals("usemtl")) {
                    // lets not bother parsing material file
                    // just use the name as an asset path
                    obj.mTextureName = tok.nextToken();
                }
            }


            int f_len = f.size();
            obj.mVertexCount = f_len * 3;
            obj.mV = FloatBuffer.allocate(obj.mVertexCount*3);

            if (vt.size() > 0) {
                obj.mVt = FloatBuffer.allocate(obj.mVertexCount*2);
            }

            if (vn.size() > 0) {
                obj.mVn = FloatBuffer.allocate(obj.mVertexCount*3);
            }

            int i;
            for (i=0; i < f_len; i++) {
                Face face = f.get(i);
                face.pushOnto(obj.mV, obj.mVt, obj.mVn);
            }
            obj.mV.rewind();
            if (obj.mVt != null)
                obj.mVt.rewind();
            if (obj.mVn != null)
                obj.mVn.rewind();

            return obj;
        }



        private static Point3 read_point(StringTokenizer tok)
        {
            Point3 ret = new Point3();
            if (tok.hasMoreTokens()) {
                ret.x = Float.parseFloat(tok.nextToken());
                if (tok.hasMoreTokens()) {
                    ret.y = Float.parseFloat(tok.nextToken());
                    if (tok.hasMoreTokens()) {
                        ret.z = Float.parseFloat(tok.nextToken());
                    }
                }
            }
            return ret;
        }

        public static class Face
        {
            Point3 v[];
            Point3 vt[];
            Point3 vn[];
            int size;
            int count;

            public Face(int size)
            {
                this.size = size;
                this.count = 0;
                this.v = new Point3[size];
                this.vt = new Point3[size];
                this.vn = new Point3[size];
            }
            public boolean addVertex(Point3 v, Point3 vt, Point3 vn)
            {
                if (count >= size)
                    return false;
                this.v[count] = v;
                this.vt[count] = vt;
                this.vn[count] = vn;
                count++;
                return true;
            }

            public void pushOnto(FloatBuffer v_buffer, FloatBuffer vt_buffer, FloatBuffer vn_buffer)
            {
                int i;
                for (i=0; i<size; i++) {
                    v_buffer.put(v[i].x); v_buffer.put(v[i].y); v_buffer.put(v[i].z);

                    if (vt_buffer != null && vt[i] != null) {
                        vt_buffer.put(vt[i].x); vt_buffer.put(vt[i].y);
                    }

                    if (vn_buffer != null && vn[i] != null) {
                        vn_buffer.put(vn[i].x); vn_buffer.put(vn[i].y); vn_buffer.put(vn[i].z);
                    }
                }
            }
        }
    }


}
