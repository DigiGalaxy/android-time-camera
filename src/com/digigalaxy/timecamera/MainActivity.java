package com.digigalaxy.timecamera;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

public class MainActivity extends Activity {
    private Camera camera;
    private cameraSurfaceView cameraSurfaceView;
    private MediaRecorder mediaRecorder;

    Button startButton;
    SurfaceHolder surfaceHolder;
    boolean recording;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        recording = false; //Not recording on first creation
        
        setContentView(R.layout.activity_main);
        
        //Get Camera for preview
        camera = getCameraInstance();
        if(camera == null){
            Toast.makeText(MainActivity.this, 
                    "Fail to get Camera", 
                    Toast.LENGTH_LONG).show();
        }

        cameraSurfaceView = new cameraSurfaceView(this, camera);
        FrameLayout cameraPreview = (FrameLayout)findViewById(R.id.videoview);
        cameraPreview.addView(cameraSurfaceView);
        
        startButton = (Button)findViewById(R.id.startButton);
        startButton.setOnClickListener(startButtonOnClickListener);
    }
    
    Button.OnClickListener startButtonOnClickListener
    = new Button.OnClickListener(){

        @Override
        public void onClick(View v) {
            if(recording){ //Already recording
                mediaRecorder.stop();  // stop the recording
                releaseMediaRecorder(); // release the MediaRecorder object
                //Exit application (finish) after released & saved
                finish();
            }else{ //Not recording
                
                //Release Camera before preparing and starting mediaRecording
                releaseCamera();
                
                if(!prepareMediaRecorder()){
                    Toast.makeText(MainActivity.this, 
                            "Fail in prepareMediaRecorder()!\n - Ended -", 
                            Toast.LENGTH_LONG).show();
                    finish();
                }
                
                mediaRecorder.start(); //Start media recorder
                recording = true; //Now recording
                startButton.setText("STOP"); 
            }
        }};
    
    //Method to get camera instance
    private Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }
    
    private boolean prepareMediaRecorder(){
        camera = getCameraInstance(); //set camera to camera instance
        mediaRecorder = new MediaRecorder(); //Initialise mediaRecorder

        //Unnecessary
        //CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        
        camera.unlock(); //unlock camera
        mediaRecorder.setCamera(camera); 

       
        
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_TIME_LAPSE_HIGH));
        /*mediaRecorder.setOutputFormat(profile.fileFormat);
        mediaRecorder.setVideoEncoder(profile.videoCodec);
        mediaRecorder.setVideoEncodingBitRate(profile.videoBitRate);
        mediaRecorder.setCaptureRate(30);
        mediaRecorder.setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);*/
        mediaRecorder.setCaptureRate(2);
        mediaRecorder.setOutputFile("/sdcard/avideo.mp4");
        mediaRecorder.setMaxDuration(60000); // Set max duration 60 sec.
        mediaRecorder.setMaxFileSize(50000000); // Set max file size 50M

        mediaRecorder.setPreviewDisplay(cameraSurfaceView.getHolder().getSurface());

        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            releaseMediaRecorder();
            return false;
        }
        return true;
        
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();       // Release media recorder
        releaseCamera();              // release the camera immediately on pause event
    }

    private void releaseMediaRecorder(){
        if (mediaRecorder != null) {
            mediaRecorder.reset();   // Reset recorder config
            mediaRecorder.release(); // Release recorder object
            mediaRecorder = null;
            camera.lock();           // lock camera for later.
        }
    }

    private void releaseCamera(){
        if (camera != null){
            camera.release();        // release the camera for other applications
            camera = null;
        }
    }
    
    public class cameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback{

        private SurfaceHolder mHolder;
        private Camera mCamera;
        
        public cameraSurfaceView(Context context, Camera camera) {
            super(context);
            mCamera = camera;

            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            mHolder = getHolder();
            mHolder.addCallback(this);
            // deprecated setting, but required on Android versions prior to 3.0
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int weight,
                int height) {
            // If your preview can change or rotate, take care of those events here.
            // Make sure to stop the preview before resizing or reformatting it.

            if (mHolder.getSurface() == null){
              // preview surface does not exist
              return;
            }

            // stop preview before making changes
            try {
                mCamera.stopPreview();
            } catch (Exception e){
              // ignore: tried to stop a non-existent preview
            }

            // make any resize, rotate or reformatting changes here

            // start preview with new settings
            try {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();

            } catch (Exception e){
            }
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            // TODO Auto-generated method stub
            // The Surface has been created, now tell the camera where to draw the preview.
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (IOException e) {
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // TODO Auto-generated method stub
            
        }
    }
}
