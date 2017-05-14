/*
 * Copyright 2009 ZXing authors
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

import android.graphics.Bitmap;
import android.util.Log;

import com.google.zxing.LuminanceSource;

/**
 * This object extends LuminanceSource around an array of YUV data returned from the camera driver,
 * with the option to crop to a rectangle within the full data. This can be used to exclude
 * superfluous pixels around the perimeter and speed up decoding.
 *
 * It works for any pixel format where the Y channel is planar and appears first, including
 * YCbCr_420_SP and YCbCr_422_SP.
 *zxing库的使用及图像亮度信息提取
 * LuminanceSource：亮度的源码，存放的是切割之后的图片的信息
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class PlanarYUVLuminanceSource extends LuminanceSource {
  private final byte[] yuvData;
  private final int dataWidth;
  private final int dataHeight;
  private final int left;
  private final int top;
    //取出指定范围的帧的数据

  /**
   *
   * @param  yuvData 整个屏幕中的图片的数据
   * @param dataWidth 整个屏幕中的图片的宽度
   * @param dataHeight 整个屏幕中的图片的高度
   * @param left  扫描框的x坐标的值
   * @param top   扫描框的y坐标的值
   * @param width  扫描框的宽度
   * @param height  扫描框的高度
   */
  public PlanarYUVLuminanceSource(byte[] yuvData, int dataWidth, int dataHeight, int left, int top,
      int width, int height) {
    super(width, height);

    if (left + width > dataWidth || top + height > dataHeight) {
      //Crop rectangle does not fit within image data.：切割矩形图片和图像的数据不符
      throw new IllegalArgumentException("Crop rectangle does not fit within image data.");
    }

    this.yuvData = yuvData;
    this.dataWidth = dataWidth;
    this.dataHeight = dataHeight;
    this.left = left;
    this.top = top;
  }
  // 这里要得到指定行的像素数据,GlobalHistogramBinarizer这个类会自己调用的
  @Override
  public byte[] getRow(int y, byte[] row) {
    Log.i("ssssss","getRow"+"正在执行");

    //getHeight()获取扫描框的高度
    if (y < 0 || y >= getHeight()) {
      throw new IllegalArgumentException("Requested row is outside the image: " + y);
    }
    //获取扫描框的宽度
    int width = getWidth();
    if (row == null || row.length < width) {
      row = new byte[width];
    }
    //因为图像的数据是数组的形式存在的
    /**
     * 1 2 3 4 5 56 6 6 6 6  6
     * 1 2 3 4 5 56 6 6 6 6  6
     * 1 2 3 4 5 56 6 6 6 6  6
     * 1 2 3 4 5 56 6 6 6 6  6
     * 1 2 3 4 5 56 6 6 6 6  6
     * 假设上面的数组就是一个图片如果要取第三行中的1，就会用到下面offset的方法
     * 计算出改点的位置，y + top代表的是要取第几行的数据，dataWidth代表的是一行数据的长度，left要取数据左边的数据的个数
     * 因为图像是在手机中存在的，坐标点不可能是原点，left代表的就是图像的左边的x的坐标点
     */
    int offset = (y + top) * dataWidth + left;
    System.arraycopy(yuvData, offset, row, 0, width);
    return row;
  }
 // getMatrix()方法，它用来返回我们裁剪的图像转换成的像素数组数据。
  @Override
  public byte[] getMatrix() {
    Log.i("ssssss","getRow"+"正在执行");
    //获取扫描框的宽度
    int width = getWidth();
    //获取扫描框的高度
    int height = getHeight();

    // If the caller asks for the entire underlying image, save the copy and give them the
    // original data. The docs specifically warn that result.length must be ignored.
    if (width == dataWidth && height == dataHeight) {
      return yuvData;
    }

    int area = width * height;
    byte[] matrix = new byte[area];
    int inputOffset = top * dataWidth + left;

    // If the width matches the full width of the underlying data, perform a single copy.
    if (width == dataWidth) {
      System.arraycopy(yuvData, inputOffset, matrix, 0, area);
      return matrix;
    }

    // Otherwise copy one cropped row at a time.
    byte[] yuv = yuvData;
    for (int y = 0; y < height; y++) {
      int outputOffset = y * width;
      /**
       * src -- 这是源数组.

       srcPos -- 这是源数组中的起始位置。

       dst -- 这是目标数组。

       dstPos -- 这是目标数据中的起始位置。

       length -- 这是一个要复制的数组元素的数目
       */
      System.arraycopy(yuv, inputOffset, matrix, outputOffset, width);
      inputOffset += dataWidth;
    }
    return matrix;
  }
  //是否支持裁剪
  @Override
  public boolean isCropSupported() {
    return true;
  }

  public int getDataWidth() {
    return dataWidth;
  }

  public int getDataHeight() {
    return dataHeight;
  }
//取得灰度图
  public Bitmap renderCroppedGreyscaleBitmap() {
    int width = getWidth();
    int height = getHeight();
    // // 首先，要取得该图片的像素数组内容
    int[] pixels = new int[width * height];
    byte[] yuv = yuvData;
    int inputOffset = top * dataWidth + left;

    for (int y = 0; y < height; y++) {
      int outputOffset = y * width;
      for (int x = 0; x < width; x++) {
        int grey = yuv[inputOffset + x] & 0xff;
        pixels[outputOffset + x] = 0xFF000000 | (grey * 0x00010101);
      }
      inputOffset += dataWidth;
    }
        /**
         * 返回一个指定高度和宽度的不可改变的位图。它的初始密度由getDensity()决定。
         *   参数

         width 位图的宽度

         height 位图的高度

         config 位图的结构
         */
    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    /**
     * 用数组中的颜色值替换位图的像素值。数组中的每个元素是包装的整型，代表了颜色值。
     *  pixels        写到位图中的颜色值

     offset        从pixels[]中读取的第一个颜色值的索引

     stride        位图行之间跳过的颜色个数。通常这个值等于位图宽度，但它可以更更大(或负数)

     X               被写入位图中第一个像素的x坐标。

     Y               被写入位图中第一个像素的y坐标

     width        从pixels[]中拷贝的每行的颜色个数

     height       写入到位图中的行数
     */
    bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
    return bitmap;
  }
}
