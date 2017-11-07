package com.plugin;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.WindowManager;

public interface IProxyPluginAct {

   void setContentView(int layoutResID);

   void startActivity(String className);
   void onCreate(Bundle var1);

   void onStart();

   void attach(Activity var1);

   void onRestart();

   void onActivityResult(int var1, int var2, Intent var3);

   void onResume();

   void onPause();

   void onStop();

   void onDestroy();

   void onSaveInstanceState(Bundle var1);

   void onNewIntent(Intent var1);

   void onRestoreInstanceState(Bundle var1);

   boolean onTouchEvent(MotionEvent var1);

   void onBackPressed();

   boolean onCreateOptionsMenu(Menu var1);

   boolean onOptionsItemSelected(MenuItem var1);

   void onConfigurationChanged(Configuration var1);

   boolean onKeyUp(int var1, KeyEvent var2);

   boolean onKeyDown(int var1, KeyEvent var2);

   void onWindowAttributesChanged(WindowManager.LayoutParams var1);

   void onWindowFocusChanged(boolean var1);

   boolean dispatchKeyEvent(KeyEvent var1);

   @TargetApi(23)
   void onRequestPermissionsResult(int var1, String[] var2, int[] var3);


   AssetManager getAssets();


   LayoutInflater getLayoutInflater();

   MenuInflater getMenuInflater();
}
