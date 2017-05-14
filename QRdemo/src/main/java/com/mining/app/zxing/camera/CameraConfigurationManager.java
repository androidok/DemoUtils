/*
 * Copyright (C) 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mining.app.zxing.camera;

import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.lang.reflect.Method;
import java.util.regex.Pattern;
//摄像头配置管理类，设置相机的参数
final class CameraConfigurationManager {

  private static final String TAG = CameraConfigurationManager.class.getSimpleName();
    //变焦值
  private static final int TEN_DESIRED_ZOOM = 27;
  private static final int DESIRED_SHARPNESS = 30;
  //compile()：将给定的正则表达式编译并赋予给Pattern类
  private static final Pattern COMMA_PATTERN = Pattern.compile(",");

  private final Context context;
  private Point screenResolution;
  private Point cameraResolution;
  private int previewFormat;
  private String previewFormatString;

  CameraConfigurationManager(Context context) {
    this.context = context;
  }

  /**
   * Reads, one time, values from the camera that are needed by the app.
   * 一段时间，应用程序需要的摄像头的值。
   */
  void initFromCameraParameters(Camera camera) {
    //获取摄像头的参数对象parameters
    Camera.Parameters parameters = camera.getParameters();
    //获取图片格式对应的数值
    previewFormat = parameters.getPreviewFormat();
    //获取预览图的格式yuv420sp
    previewFormatString = parameters.get("preview-format");
    Log.d(TAG, "Default preview format: " + previewFormat + '/' + previewFormatString);
    //// 获取当前屏幕管理器对象
    WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    // 获取默认的显示对象
    Display display = manager.getDefaultDisplay();
    //screenResolution：代表的是屏幕的分辨路，用于存储屏幕的高度和宽度
    screenResolution = new Point(display.getWidth(), display.getHeight());
    Log.d(TAG, "Screen resolution: " + screenResolution);
    cameraResolution = getCameraResolution(parameters, screenResolution);
    Log.d(TAG, "Camera resolution: " + screenResolution);
  }

  /**
   * Sets the camera up to take preview images which are used for both preview and decoding.
   * We detect the preview format here so that buildLuminanceSource() can build an appropriate
   * LuminanceSource subclass. In the future we may want to force YUV420SP as it's the smallest,
   * and the planar Y can be used for barcode scanning without a copy in some cases.
   * 设置自己想要的相机参数。Desired：想要的
   */
  void setDesiredCameraParameters(Camera camera) {
    Camera.Parameters parameters = camera.getParameters();
    Log.d(TAG, "Setting preview size: " + cameraResolution);
    //// 设置预览照片的大小
    parameters.setPreviewSize(cameraResolution.x, cameraResolution.y);
    setFlash(parameters);
    setZoom(parameters);
    //setSharpness(parameters);
    //modify here

//    camera.setDisplayOrientation(90);
    //兼容2.1
    setDisplayOrientation(camera, 90);
    camera.setParameters(parameters);
  }

  Point getCameraResolution() {
    return cameraResolution;
  }
  //获取屏幕的分辨率
  Point getScreenResolution() {
    return screenResolution;
  }
  //获取图片格式对应的数值
  int getPreviewFormat() {
    return previewFormat;
  }
  //获取预览的格式
  String getPreviewFormatString() {
    return previewFormatString;
  }
  //获取相机的分辨路
  private static Point getCameraResolution(Camera.Parameters parameters, Point screenResolution) {
  //preview-size-values:当前支持的拍照预览尺寸1920x1080,1440x1080,1280x720,864x480,800x480,
    // 768x432,720x480,640x480,576x432,480x320,384x288,352x288,320x240,240x160,176x144
    String previewSizeValueString = parameters.get("preview-size-values");
    // saw this on Xperia
    if (previewSizeValueString == null) {
      previewSizeValueString = parameters.get("preview-size-value");
    }
  //cameraResolution:代表的是相机的分辨率
    Point cameraResolution = null;

    if (previewSizeValueString != null) {
      Log.d(TAG, "preview-size-values parameter: " + previewSizeValueString);
      //设置相机的分辨率
      cameraResolution = findBestPreviewSizeValue(previewSizeValueString, screenResolution);
    }

    if (cameraResolution == null) {
      // Ensure that the camera resolution is a multiple of 8, as the screen may not be.：确保相机的分辨率是8的倍数,屏幕可能不是。
      cameraResolution = new Point(
              (screenResolution.x >> 3) << 3,
              (screenResolution.y >> 3) << 3);
    }

    return cameraResolution;
  }
  //获取和设置相机的分辨路，让相机的分辨率等于手机的分辨率
  private static Point findBestPreviewSizeValue(CharSequence previewSizeValueString, Point screenResolution) {
    int bestX = 0;
    int bestY = 0;
    int diff = Integer.MAX_VALUE;
//     COMMA_PATTERN.split：将目标字符串按照Pattern里所包含的正则表达式为模进行分割。分割之后是一个数组
    for (String previewSize : COMMA_PATTERN.split(previewSizeValueString)) {
      previewSize = previewSize.trim();
      //查询"1920x1080"中x额索引
      int dimPosition = previewSize.indexOf('x');
      if (dimPosition < 0) {
        Log.w(TAG, "Bad preview-size: " + previewSize);
        continue;
      }

      int newX;
      int newY;
      try {
        //"获取1920x1080"中1920
        newX = Integer.parseInt(previewSize.substring(0, dimPosition));
        //"获取1920x1080"中1080
        newY = Integer.parseInt(previewSize.substring(dimPosition + 1));
      } catch (NumberFormatException nfe) {
        Log.w(TAG, "Bad preview-size: " + previewSize);
        continue;
      }
  //abs():取绝对值
      int newDiff = Math.abs(newX - screenResolution.x) + Math.abs(newY - screenResolution.y);
      if (newDiff == 0) {
        bestX = newX;
        bestY = newY;
        break;
      } else if (newDiff < diff) {
        bestX = newX;
        bestY = newY;
        diff = newDiff;
      }

    }

    if (bestX > 0 && bestY > 0) {
      return new Point(bestX, bestY);
    }
    return null;
  }

  private static int findBestMotZoomValue(CharSequence stringValues, int tenDesiredZoom) {
    int tenBestValue = 0;
    //COMMA_PATTERN是pattern
    for (String stringValue : COMMA_PATTERN.split(stringValues)) {
      stringValue = stringValue.trim();
      double value;
      try {
        value = Double.parseDouble(stringValue);
      } catch (NumberFormatException nfe) {
        return tenDesiredZoom;
      }
      int tenValue = (int) (10.0 * value);
      if (Math.abs(tenDesiredZoom - value) < Math.abs(tenDesiredZoom - tenBestValue)) {
        tenBestValue = tenValue;
      }
    }
    return tenBestValue;
  }
  //设置闪关灯的方式
  private void setFlash(Camera.Parameters parameters) {
    // FIXME: This is a hack to turn the flash off on the Samsung Galaxy.
    // And this is a hack-hack to work around a different value on the Behold II
    // Restrict Behold II check to Cupcake, per Samsung's advice
    //if (Build.MODEL.contains("Behold II") &&
    //    CameraManager.SDK_INT == Build.VERSION_CODES.CUPCAKE) {
    if (Build.MODEL.contains("Behold II") && CameraManager.SDK_INT == 3) { // 3 = Cupcake
      parameters.set("flash-value", 1);
    } else {
      parameters.set("flash-value", 2);
    }
    // This is the standard setting to turn the flash off that all devices should honor.：这是设置所有设备都应该遵守的标准设置
    parameters.set("flash-mode", "off");
  }
  //获取变焦值
  private void setZoom(Camera.Parameters parameters) {
  ////zoom-supported：是否支持变焦
    String zoomSupportedString = parameters.get("zoom-supported");
    if (zoomSupportedString != null && !Boolean.parseBoolean(zoomSupportedString)) {
      return;
    }
    //Desired:想要的   Zoom：焦点
    int tenDesiredZoom = TEN_DESIRED_ZOOM;
  //"max-zoom"：最大变焦倍数
    String maxZoomString = parameters.get("max-zoom");
    if (maxZoomString != null) {
      try {
        int tenMaxZoom = (int) (10.0 * Double.parseDouble(maxZoomString));

        if (tenDesiredZoom > tenMaxZoom) {
          tenDesiredZoom = tenMaxZoom;
        }
      } catch (NumberFormatException nfe) {
        Log.w(TAG, "Bad max-zoom: " + maxZoomString);
      }
    }
    //获取图片变焦的最大值
    String takingPictureZoomMaxString = parameters.get("taking-picture-zoom-max");
    //这时候取出来的值是null
    if (takingPictureZoomMaxString != null) {
      try {
        int tenMaxZoom = Integer.parseInt(takingPictureZoomMaxString);
        if (tenDesiredZoom > tenMaxZoom) {
          tenDesiredZoom = tenMaxZoom;
        }
      } catch (NumberFormatException nfe) {
        Log.w(TAG, "Bad taking-picture-zoom-max: " + takingPictureZoomMaxString);
      }
    }

    String motZoomValuesString = parameters.get("mot-zoom-values");
    if (motZoomValuesString != null) {
      tenDesiredZoom = findBestMotZoomValue(motZoomValuesString, tenDesiredZoom);
    }

    String motZoomStepString = parameters.get("mot-zoom-step");
    if (motZoomStepString != null) {
      try {
        double motZoomStep = Double.parseDouble(motZoomStepString.trim());
        int tenZoomStep = (int) (10.0 * motZoomStep);
        if (tenZoomStep > 1) {
          tenDesiredZoom -= tenDesiredZoom % tenZoomStep;
        }
      } catch (NumberFormatException nfe) {
        // continue
      }
    }

    // Set zoom. This helps encourage the user to pull back.:设置缩放有助于帮助用户恢复到原来的地方
    // Some devices like the Behold have a zoom parameter：一些设备,如三星Behold 有一个缩放参数
    if (maxZoomString != null || motZoomValuesString != null) {
      parameters.set("zoom", String.valueOf(tenDesiredZoom / 10.0));
    }

    // Most devices, like the Hero, appear to expose this zoom parameter.：大多数设备，比如Hero，似乎都暴露了这个变焦参数
    // It takes on values like "27" which appears to mean 2.7x zoom：他需要27最大变焦，似乎意味着2.7倍的光学变焦
    if (takingPictureZoomMaxString != null) {
      parameters.set("taking-picture-zoom", tenDesiredZoom);
    }
  }

  public static int getDesiredSharpness() {
    return DESIRED_SHARPNESS;
  }

  /**设置摄像头旋转的角度
   * compatible  1.6
   * @param camera
   * @param angle  旋转的角度
   */
  protected void setDisplayOrientation(Camera camera, int angle){
    Method downPolymorphic;
    try
    {
      downPolymorphic = camera.getClass().getMethod("setDisplayOrientation", new Class[] { int.class });
      if (downPolymorphic != null)
        downPolymorphic.invoke(camera, new Object[] { angle });
    }
    catch (Exception e1)
    {
    }
  }

}
