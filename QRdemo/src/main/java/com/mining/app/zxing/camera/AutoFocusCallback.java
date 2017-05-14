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

import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
//Camera.AutoFocusCallback：回调接口用于通知完成相机自动对焦
final class AutoFocusCallback implements Camera.AutoFocusCallback {
  //获取AutoFocusCallback值
  private static final String TAG = AutoFocusCallback.class.getSimpleName();
//延时发送消息的时间
  private static final long AUTOFOCUS_INTERVAL_MS = 1500L;

  private Handler autoFocusHandler;
  private int autoFocusMessage;

  void setHandler(Handler autoFocusHandler, int autoFocusMessage) {
    this.autoFocusHandler = autoFocusHandler;
    this.autoFocusMessage = autoFocusMessage;
  }
//回调方法
  public void onAutoFocus(boolean success, Camera camera) {
    if (autoFocusHandler != null) {
//      通过handler创建一个消息对象，并且指定消息id号（what）,传递的数据(obj)
      Message message = autoFocusHandler.obtainMessage(autoFocusMessage, success);
      //延时AUTOFOCUS_INTERVAL_MS秒发送消息
      autoFocusHandler.sendMessageDelayed(message, AUTOFOCUS_INTERVAL_MS);
      autoFocusHandler = null;
    } else {
      Log.d(TAG, "Got auto-focus callback, but no handler for it");
    }
  }

}
