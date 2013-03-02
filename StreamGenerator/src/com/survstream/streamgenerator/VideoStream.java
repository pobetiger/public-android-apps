package com.survstream.streamgenerator;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.EditText;
//import android.widget.MediaController;
import android.widget.VideoView;

public class VideoStream extends Activity 
	implements OnCompletionListener, OnPreparedListener, OnErrorListener, OnInfoListener, OnTouchListener
{

	boolean bDebug = false;
	boolean bStreamLoaded = false;
	WifiLock _wifilock; 
	
//	String sTestURL = "rtsp://v5.cache1.c.youtube.com/CjYLENy73wIaLQnhycnrJQ8qmRMYESARFEIJbXYtZ29vZ2xlSARSBXdhdGNoYPj_hYjnq6uUTQw=/0/0/0/video.3gp";
	String sTestURL = "rtsp://192.168.1.106:8554/bensing3_small_bitrate_adj.H.264";
//	private final String sTestURL = "rtsp://192.168.2.181:8554/bensing.264";
//	private final String sTestURL = "http://www.youtube.com/watch?v=2Ld6xhJuGBk";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video_streamer);
		
		writeLog("Creating activity view ...");
		setWakeLock(true);
		setWifiLock(true);
		
		// do some initialization
		if (!bDebug)
		{
			EditText txtLog = (EditText) findViewById(R.id.editTextLog);
			txtLog.setVisibility(View.GONE);
		}
		
		EditText t = (EditText) findViewById(R.id.editText1);
		t.setText(sTestURL);
		
		VideoView vw = (VideoView) findViewById(R.id.videoView1);
		
		vw.setOnTouchListener(this);
		vw.setOnPreparedListener(this);
		vw.setOnCompletionListener(this);
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		stopStreaming();
		
		setWifiLock(false);
		setWakeLock(false);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.video_streamer, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		case R.id.action_settings:
			if (item.isChecked())
			{
				item.setChecked(false);
				item.setTitle(R.string.action_menu_logging_on);
			}
			else
			{
				item.setChecked(true);
				item.setTitle(R.string.action_menu_logging_off);
			}
			
			writeLog("debug: " + item.isChecked());
			
			bDebug = item.isChecked();
			EditText txtLog = (EditText) findViewById(R.id.editTextLog);
			
			if (bDebug)
			{
				txtLog.setVisibility(View.VISIBLE);
			}
			else
			{
				txtLog.setVisibility(View.GONE);
			}
			
			return true;
			
		case R.id.action_exit:
			VideoView vw = (VideoView) findViewById(R.id.videoView1);
			writeLog("Stopping playback");
			if (vw.isPlaying())
				vw.stopPlayback();
			stopStreaming();
			
			finish();
			System.exit(0);

			return true;
			
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void writeLog(String sMsg)
	{
		if (bDebug)
		{
			EditText theLog = (EditText) findViewById(R.id.editTextLog);
			theLog.setText(theLog.getText() + sMsg + "\n");
			Log.d("VideoStreamer_Debug", sMsg);
		}
	}
	
	private void setWakeLock(boolean bEnable)
	{
		// keep screen on for the duration
		writeLog("Getting wake lock: " + bEnable);
		((VideoView) findViewById(R.id.videoView1)).setKeepScreenOn(bEnable);
	}
	private void setWifiLock(boolean bEnable)
	{
		
		if (bEnable == true)
		{
			// get wifi lock
			writeLog("Getting wifi lock");
			_wifilock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
				    .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
			_wifilock.acquire();
		}
		else
		{
			if (_wifilock != null)
			{
				writeLog("Releasing wifi lock");
				_wifilock.release();
			}
		}
		
	}
	
	public void startStreaming()
	{
		writeLog("Setting up video view ...");
		
		// commented out here and move to onCreate() for now.
		// come back to better battery usage later:
//		setWakeLock(true);
//		setWifiLock(true);
//				
		// grab the string from the text box		
		String sURL = ((EditText)findViewById(R.id.editText1)).getText().toString();
		
		writeLog("Attempting to create video stream ...");
		VideoView vw = (VideoView) findViewById(R.id.videoView1);
		try
		{
			vw.setVideoURI(Uri.parse(sURL));
		}
		catch(Exception ex)
		{
			writeLog("Argument Exception, message: " + ex.getMessage());
		}
		
//		writeLog("Setup media controller ...");
//		vw.setMediaController(new MediaController(this));
		
		vw.requestFocus();
		vw.start();
	}
	private void stopStreaming() 
	{
		writeLog("Video play finished");
		
		VideoView vw = (VideoView) findViewById(R.id.videoView1);
		if (vw.isPlaying())
		{
			vw.stopPlayback();
		}
		
		bStreamLoaded = false;
		
		// commented out for stability:
//		setWifiLock(false);
//		setWakeLock(false);
	}
	
	@Override
	public void onPrepared(MediaPlayer mp)
	{
		writeLog("Video Prepared, ready to play");
				
		bStreamLoaded = true;
		try
		{
			writeLog("Video playback started");
			mp.start();
		}
		catch(Exception ex)
		{
			writeLog("Video play error, Exception: " + ex.getMessage());
		}
		finally
		{
			writeLog("Video should be started (to buffer, etc.)");
		}
	}
	@Override
	public void onCompletion(MediaPlayer mp)
	{
		stopStreaming();
		
		// also need to remove media controller
		if (mp != null)
			mp.release();
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		writeLog("MediaPlayer error: " + what + ", extra: " + extra);
		return false;
	}

	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		writeLog("MediaPlayer error: " + what + ", extra: " + extra);
		return false;
	}

	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		writeLog("User touched video player, getting ready to play ...");
		if (bStreamLoaded)
		{
			VideoView vw = (VideoView) findViewById(R.id.videoView1);
			if (vw.isPlaying())
			{
				writeLog("Pausing playback");
				vw.pause();
			}
			else
			{
				writeLog("Resume playback");
				vw.resume();
			}
		}
		else
		{
			// not playing we can start streaming
			writeLog("No stream loaded, starting new one ...");
			startStreaming();
			writeLog("Waiting on stream to be ready");
		}
		return false;
	}


}











