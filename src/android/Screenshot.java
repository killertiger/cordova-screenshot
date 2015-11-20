/**
 * Copyright (C) 2012 30ideas (http://30ide.as)
 * MIT licensed
 * 
 * @author Josemando Sobral
 * @created Jul 2nd, 2012.
 * improved by Hongbo LU
 */
package com.darktalker.cordova.screenshot;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Environment;
import android.util.Base64;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import android.graphics.Color;
import android.util.Log;

public class Screenshot extends CordovaPlugin {
	
	private TextureView findXWalkTextureView(ViewGroup group) {

		int childCount = group.getChildCount();
		for(int i=0;i<childCount;i++) {
			View child = group.getChildAt(i);
			if(child instanceof TextureView) {
				String parentClassName = child.getParent().getClass().toString();
				boolean isRightKindOfParent = (parentClassName.contains("XWalk"));
				if(isRightKindOfParent) {
					return (TextureView) child;
				}
			} else if(child instanceof ViewGroup) {
				TextureView textureView = findXWalkTextureView((ViewGroup) child);
				if(textureView != null) {
					return textureView;
				}
			}
		}
		
		return null;
	}
	
	private Bitmap getBitmap() {
		Bitmap bitmap = null;
		
		boolean isCrosswalk = false;
		try {
			Class.forName("org.crosswalk.engine.XWalkWebViewEngine");
			isCrosswalk = true;
		} catch (Exception e) {
		}
		
		if(isCrosswalk) {
			try {
				
				TextureView textureView = findXWalkTextureView((ViewGroup)webView.getView());
                                if (textureView != null) {
				    bitmap = textureView.getBitmap();
                                    return bitmap;
                                }
			} catch(Exception e) {
			}
		} 

	        View view = webView.getView().getRootView();
		view.setDrawingCacheEnabled(true);
		bitmap = Bitmap.createBitmap(view.getDrawingCache());
		view.setDrawingCacheEnabled(false);

		
		return bitmap;
	}

    private void scanPhoto(String imageFileName)
    {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(imageFileName);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.cordova.getActivity().sendBroadcast(mediaScanIntent);
    }
	
	private static Bitmap resize(Bitmap image, int maxWidth, int maxHeight) {
	    if (maxHeight > 0 && maxWidth > 0) {
	        int width = image.getWidth();
	        int height = image.getHeight();
	        
	        float scaleFactor = (width < height) ? maxWidth / width : maxHeight / height;
	        int finalWidth = (int)(width * scaleFactor);
	        int finalHeight = (int)(height * scaleFactor);
	        
	        /*float ratioBitmap = (float) width / (float) height;
	        float ratioMax = (float) maxWidth / (float) maxHeight;
	
	        int finalWidth = maxWidth;
	        int finalHeight = maxHeight;
	        if (ratioMax > 1) {
	            finalWidth = (int) ((float)maxHeight * ratioBitmap);
	        } else {
	            finalHeight = (int) ((float)maxWidth / ratioBitmap);
	        }*/
	   	String result = finalWidth + " - " + finalHeight;
	        Log.d("autocrop", result);
	        image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
	        return image;
	    } else {
	        return image;
	    }
	}
	
	private Bitmap tentativa3(Bitmap source)
	{
		int baseColor = Color.WHITE;
		int width = source.getWidth();
		int height = source.getHeight();
		int topX = 0;
		int topY = 0;
		int x = 0;
		for(int y=0; y < height/2; y++)
		{
			if (source.getPixel(x, y) == Color.BLACK) {
				topY = y;
			}
			else
			{
				break;
			}
		}
		for(int y=height-1; y > 0; y--)
		{
			if (source.getPixel(x, y) == Color.WHITE) {
				height = y;	
			}
			else
			{
				break;
			}
		}
		
		Bitmap destination = Bitmap.createBitmap(source, topX, topY, width - topX, height - topY);//source.getHeight() - upperBorder);
	   	return resize(destination, 300, 300);
	}
	
	@Override
	public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
	 	// starting on ICS, some WebView methods
		// can only be called on UI threads

		if (action.equals("saveScreenshot")) {
			final String format = (String) args.get(0);
			final Integer quality = (Integer) args.get(1);
			final String fileName = (String)args.get(2);

			super.cordova.getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					
					try {
						if(format.equals("png") || format.equals("jpg")){
							Bitmap bitmap = getBitmap();
							File folder = new File(Environment.getExternalStorageDirectory(), "Pictures");
							if (!folder.exists()) {
								folder.mkdirs();
							}

							//ALTERAÇÕES *************************
							//dasateasteasseasjo
							//jiojij
							bitmap = tentativa3(bitmap);
							
							/*Bitmap source = bitmap;

							boolean flag = false ;
							int upperBorder = -1 ; 
							do{
								upperBorder ++ ;
								for (int c1 =0 ; c1 < source.getWidth() ; c1++){
									if(source.getPixel(c1, upperBorder) != Color.WHITE ){
										flag = true;
										break ;
									}
								}

								if (upperBorder >= source.getHeight())
									flag = true ;
							}while(!flag) ;

						//	BufferedImage destination = new BufferedImage(source.getWidth(), source.getHeight() - upperBorder, BufferedImage.TYPE_INT_ARGB) ;
						//	destination.getGraphics().drawImage(source, 0, upperBorder*-1, null) ;
							Bitmap destination = Bitmap.createBitmap(source, 0, 0, source.getWidth(), 50);//source.getHeight() - upperBorder);
							bitmap = destination;*/

							//ALTERAÇÕES *************************

							File f = new File(folder, fileName + "."+format);

							FileOutputStream fos = new FileOutputStream(f);
							if(format.equals("png")){
								bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
							}
							if(format.equals("jpg")){
								bitmap.compress(Bitmap.CompressFormat.JPEG, quality == null?100:quality, fos);
							}
							JSONObject jsonRes = new JSONObject();
							jsonRes.put("filePath",f.getAbsolutePath());
				                        PluginResult result = new PluginResult(PluginResult.Status.OK, jsonRes);
				                        callbackContext.sendPluginResult(result);

                            				scanPhoto(f.getAbsolutePath());
						}else{
							callbackContext.error("format "+format+" not found");

						}

					} catch (JSONException e) {
						callbackContext.error(e.getMessage());
						
					} catch (IOException e) {
						callbackContext.error(e.getMessage());
						
					}
				}
			});
			return true;
		}else if(action.equals("getScreenshotAsURI")){
			final Integer quality = (Integer) args.get(0);
			
			super.cordova.getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					try {
						Bitmap bitmap = getBitmap();

						ByteArrayOutputStream jpeg_data = new ByteArrayOutputStream();
						
						if (bitmap.compress(CompressFormat.JPEG, quality, jpeg_data)) {
						   byte[] code = jpeg_data.toByteArray();
						   byte[] output = Base64.encode(code, Base64.NO_WRAP);
						   String js_out = new String(output);
						   js_out = "data:image/jpeg;base64," + js_out;
						   JSONObject jsonRes = new JSONObject();
						   jsonRes.put("URI", js_out);
				                   PluginResult result = new PluginResult(PluginResult.Status.OK, jsonRes);
				                   callbackContext.sendPluginResult(result);
							
						   js_out = null;
						   output = null;
						   code = null;
						}
						
						jpeg_data = null;

					} catch (JSONException e) {
						callbackContext.error(e.getMessage());
						
					} catch (Exception e) {
						callbackContext.error(e.getMessage());
						
					}
				}
			});

			return true;		
		}
		callbackContext.error("action not found");
		return false;
	}
}
