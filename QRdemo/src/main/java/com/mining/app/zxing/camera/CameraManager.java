/*
 * Copyright (C) 2008 ZXing authors
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
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;

/**
 * This object wraps the Camera service object and expects to be the only one talking to it. The
 * implementation encapsulates the steps needed to take preview-sized images, which are used for
 * both preview and decoding.
 *
 */
public final class CameraManager {

  private static final String TAG = CameraManager.class.getSimpleName();

  private static final int MIN_FRAME_WIDTH = 240;
  private static final int MIN_FRAME_HEIGHT = 240;
  private static final int MAX_FRAME_WIDTH = 480;
  private static final int MAX_FRAME_HEIGHT = 360;

  private static CameraManager cameraManager;

  static final int SDK_INT; // Later we can use Build.VERSION.SDK_INT
  static {
    int sdkInt;
    try {
      //获取系统的版本号
      sdkInt = Integer.parseInt(Build.VERSION.SDK);
    } catch (NumberFormatException nfe) {
      // Just to be safe
      sdkInt = 10000;
    }
    SDK_INT = sdkInt;
  }

  private final Context context;
  private final CameraConfigurationManager configManager;
  private Camera camera;
  private Rect framingRect;
  private Rect framingRectInPreview;
  private boolean initialized;
  private boolean previewing;
  //代表的是sdk的版本是否大于3，如果大于3，是true，代表的含义是是否可以调用OneShotPreviewCallback方法
  private final boolean useOneShotPreviewCallback;
  /**
   * Preview frames are delivered here, which we pass on to the registered handler. Make sure to
   * clear the handler so it will only receive one message.
   */
  private final PreviewCallback previewCallback;
  /** Autofocus callbacks arrive here, and are dispatched to the Handler which requested them. */
  private final AutoFocusCallback autoFocusCallback;

  /**
   * Initializes this static object with the Context of the calling Activity.
   *cameraManager初始化单例对象
   * @param context The Activity which wants to use the camera.
   */
  public static void init(Context context) {
    if (cameraManager == null) {
      cameraManager = new CameraManager(context);
    }
  }

  /**
   * Gets the CameraManager singleton instance.
   *返回对象
   * @return A reference to the CameraManager singleton.
   */
  public static CameraManager get() {
    return cameraManager;
  }
//私有构造方法
  private CameraManager(Context context) {

    this.context = context;
    this.configManager = new CameraConfigurationManager(context);

    // Camera.setOneShotPreviewCallback() has a race condition in Cupcake,:Camera.setOneShotPreviewCallback() 的调用在Android 1.5 中有特殊的条件，所以这个方法只能在android 1.5之后调用
    // so we use the older  Camera.setPreviewCallback() on 1.5 and earlier.： Android 1.5 之前或者更早的系统我们调用 Camera.setPreviewCallback()
    // For Donut and later, we need to use  the more efficient one shot callback, ：对于android 1.6及以后，我们需要更有效的回调
    // as the older one can swamp the system and cause it to run out of memory：.因为旧的系统会使系统陷入困境，导致内存耗尽。
    // We can't use SDK_INT because it was introduced in the Donut SDK.：我们不能使用SDK_INT 因为在android 1.6中没有对sdk做介绍
    //useOneShotPreviewCallback = Integer.parseInt(Build.VERSION.SDK) > Build.VERSION_CODES.CUPCAKE;：我们使用这个方法判断
    useOneShotPreviewCallback = Integer.parseInt(Build.VERSION.SDK) > 3; // 3 = Cupcake
    previewCallback = new PreviewCallback(configManager, useOneShotPreviewCallback);
    //自动对焦实例对象
    autoFocusCallback = new AutoFocusCallback();
  }

  /**
   * Opens the camera driver and initializes the hardware parameters.
   *
   * @param holder The surface object which the camera will draw preview frames into.
   * @throws IOException Indicates the camera driver failed to open.
   * 打开驱动和初始化硬件参数
   */
  public void openDriver(SurfaceHolder holder) throws IOException {
    if (camera == null) {
      //// 1. 获取手机背面的摄像头
      camera = Camera.open();
      if (camera == null) {
        throw new IOException();
      }
      //// 2. 设置摄像头预览view
      camera.setPreviewDisplay(holder);

      if (!initialized) {
        //记录相机的参数是否被初始化过
        initialized = true;
        //初始化相机的参数
        configManager.initFromCameraParameters(camera);
      }
      // 3. 读取配置并设置自己想要相机参数
      configManager.setDesiredCameraParameters(camera);

      //FIXME
      //     SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
      //是否使用前灯
//      if (prefs.getBoolean(PreferencesActivity.KEY_FRONT_LIGHT, false)) {
//        FlashlightManager.enableFlashlight();
//      }
      FlashlightManager.enableFlashlight();
    }
  }

  /**
   * Closes the camera driver if still in use.
   */
  public void closeDriver() {
    if (camera != null) {
      FlashlightManager.disableFlashlight();
      //释放camera
      camera.release();
      camera = null;
    }
  }

  /**
   * Asks the camera hardware to begin drawing preview frames to the screen.
   */
  public void startPreview() {
    if (camera != null && !previewing) {
      // 开始预览
      camera.startPreview();
      Log.i("ssssss","startPreview"+"正在运行");
      previewing = true;
    }
  }

  /**
   * Tells the camera to stop drawing preview frames.
   * 停止预览
   */
  public void stopPreview() {
    if (camera != null && previewing) {
      if (!useOneShotPreviewCallback) {
        camera.setPreviewCallback(null);
      }
      // 停止预览
      camera.stopPreview();
      //是否handler
      previewCallback.setHandler(null, 0);
      autoFocusCallback.setHandler(null, 0);
      previewing = false;
    }
  }

  /**
   * A single preview frame will be returned to the handler supplied. The data will arrive as byte[]
   * in the message.obj field, with width and height encoded as message.arg1 and message.arg2,
   * respectively.
   * 1：将handler与preview回调函数绑定；<br/>
   * 2：注册preview回调函数<br/>
   * 综上，该函数的作用是当相机的预览界面准备就绪后就会调用hander向其发送传入的message
   * @param handler The handler to send the message to.
   * @param message The what field of the message to be sent.
   *                请求预览的框架
   */
  public void requestPreviewFrame(Handler handler, int message) {
    if (camera != null && previewing) {
      previewCallback.setHandler(handler, message);
      Log.i("ssssss","requestPreviewFrame"+"正在运行");
      if (useOneShotPreviewCallback) {
         // 绑定相机回调函数，当预览界面准备就绪后会回调Camera.PreviewCallback.onPreviewFrame
        camera.setOneShotPreviewCallback(previewCallback);
      } else {
        camera.setPreviewCallback(previewCallback);
      }
    }
  }

  /**
   * Asks the camera hardware to perform an autofocus.
   *
   * @param handler The Handler to notify when the autofocus completes.
   * @param message The message to deliver.
   *                请求自动对焦
   */
  public void requestAutoFocus(Handler handler, int message) {
    if (camera != null && previewing) {
      autoFocusCallback.setHandler(handler, message);
      //Log.d(TAG, "Requesting auto-focus callback");
      camera.autoFocus(autoFocusCallback);
    }
  }

  /**
   * Calculates the framing rect which the UI should draw to show the user where to place the
   * barcode. This target helps with alignment as well as forces the user to hold the device
   * far enough away to ensure the image will be in focus.
   *
   * @return The rectangle to draw on screen in window coordinates.：画在屏幕上的矩形窗口坐标
   */
  public Rect getFramingRect() {
    Point screenResolution = configManager.getScreenResolution();
    if (framingRect == null) {
      if (camera == null) {
        return null;
      }
      int width = screenResolution.x * 3 / 4;
      if (width < MIN_FRAME_WIDTH) {
        width = MIN_FRAME_WIDTH;
      } else if (width > MAX_FRAME_WIDTH) {
        width = MAX_FRAME_WIDTH;
      }
      int height = screenResolution.y * 3 / 4;
      if (height < MIN_FRAME_HEIGHT) {
        height = MIN_FRAME_HEIGHT;
      } else if (height > MAX_FRAME_HEIGHT) {
        height = MAX_FRAME_HEIGHT;
      }
      //矩形框的左上角x轴的位置
      int leftOffset = (screenResolution.x - width) / 2;
      //矩形框的左上角y轴的位置
      int topOffset = (screenResolution.y - height) / 2;
      //创建矩形，参数分别是前两个（x，y）确定左上角，后面两个确定右下角(x,y)
      framingRect = new Rect(leftOffset, topOffset, leftOffset + width, topOffset + height);
      Log.d(TAG, "Calculated framing rect: " + framingRect);
    }
    return framingRect;
  }

  /**
   * Like {@link #getFramingRect} but coordinates are in terms of the preview frame,
   * not UI / screen.
   * 对上面的矩形进行一个反转，如果是横屏的话，通过下面的方法转化成竖屏的
   * 就是修改矩形的横竖屏的显示
   */
  public Rect getFramingRectInPreview() {
    if (framingRectInPreview == null) {
      Rect rect = new Rect(getFramingRect());
      Point cameraResolution = configManager.getCameraResolution();
      Point screenResolution = configManager.getScreenResolution();
      //modify here，支持横屏模式
//      rect.left = rect.left * cameraResolution.x / screenResolution.x;
//      rect.right = rect.right * cameraResolution.x / screenResolution.x;
//      rect.top = rect.top * cameraResolution.y / screenResolution.y;
//      rect.bottom = rect.bottom * cameraResolution.y / screenResolution.y;
      /**
       * left ： 矩形左边的X坐标
       top:    矩形顶部的Y坐标
       right :  矩形右边的X坐标
       bottom： 矩形底部的Y坐标
       x和y值的对调，支持竖屏模式
       */
      rect.left = rect.left * cameraResolution.y / screenResolution.x;
      rect.right = rect.right * cameraResolution.y / screenResolution.x;
      rect.top = rect.top * cameraResolution.x / screenResolution.y;
      rect.bottom = rect.bottom * cameraResolution.x / screenResolution.y;
      framingRectInPreview = rect;
    }
    return framingRectInPreview;
  }

  /**
   * Converts the result points from still resolution coordinates to screen coordinates.
   *
   * @param points The points returned by the Reader subclass through Result.getResultPoints().
   * @return An array of Points scaled to the size of the framing rect and offset appropriately
   *         so they can be drawn in screen coordinates.
   */
  /*
  public Point[] convertResultPoints(ResultPoint[] points) {
    Rect frame = getFramingRectInPreview();
    int count = points.length;
    Point[] output = new Point[count];
    for (int x = 0; x < count; x++) {
      output[x] = new Point();
      output[x].x = frame.left + (int) (points[x].getX() + 0.5f);
      output[x].y = frame.top + (int) (points[x].getY() + 0.5f);
    }
    return output;
  }
   */

  /**
   * A factory method to build the appropriate LuminanceSource object based on the format
   * of the preview buffers, as described by Camera.Parameters.
   *
   * @param data A preview frame.
   * @param width The width of the image.
   * @param height The height of the image.
   * @return A PlanarYUVLuminanceSource instance.
   */
  /**
   *
   * @param data  扫描矩形中的数据源
   * @param width  扫描框中图像的宽度
   * @param height   扫描框中图像的度
   * @return
   */
  public PlanarYUVLuminanceSource buildLuminanceSource(byte[] data, int width, int height) {
    //获取矩形对象实例
    Rect rect = getFramingRectInPreview();
    //获取预览格式对应的数值（Integer值）
    int previewFormat = configManager.getPreviewFormat();
    //获取预览的格式
    String previewFormatString = configManager.getPreviewFormatString();
    switch (previewFormat) {
      // This is the standard Android format which all devices are REQUIRED to support.
      // In theory, it's the only one we should ever care about.
      case PixelFormat.YCbCr_420_SP:
        // This format has never been seen in the wild, but is compatible as we only care
        // about the Y channel, so allow it.
      case PixelFormat.YCbCr_422_SP:
        return new PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top,
                rect.width(), rect.height());
      default:
        // The Samsung Moment incorrectly uses this variant instead of the 'sp' version.
        // Fortunately, it too has all the Y data up front, so we can read it.
        if ("yuv420p".equals(previewFormatString)) {
          return new PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top,
                  rect.width(), rect.height());
        }
    }
    throw new IllegalArgumentException("Unsupported picture format: " +
            previewFormat + '/' + previewFormatString);
  }

  public Context getContext() {
    return context;
  }

}
