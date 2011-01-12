package com.hlidskialf.android.objdemo;

import android.app.Activity;
import android.os.Bundle;
import android.opengl.GLSurfaceView;
import android.widget.TextView;

public class DemoActivity extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);


        mSurfaceView = (DemoRendererView)findViewById(R.id.renderer_view);
        mSurfaceView.start();

        mText = (TextView)findViewById(android.R.id.text1);

        update_text();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        mSurfaceView.onPause();
    }
    @Override
    public void onResume()
    {
        super.onResume();
        mSurfaceView.onResume();
    }


    private void update_text()
    {
        //mText.setText( "origin: "+mRenderer.mOrigin);
    }


    DemoRendererView mSurfaceView;
    TextView mText;
}
