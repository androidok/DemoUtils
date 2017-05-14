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

package com.mining.app.zxing.decoding;

import android.content.Intent;
import android.net.Uri;

import com.google.zxing.BarcodeFormat;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

import static com.google.zxing.BarcodeFormat.EAN_13;

/**
 * DecodeFormatManager 这个配置解码格式。一维码，二维码等
 * 二维码又称QR Code，QR全称Quick Response
 * 在二维码符号表示技术研究方面，已研制出多种码制，常见的有PDF417，QR Code，Code 49，Code 16K，Code One
 */
final class DecodeFormatManager {

  private static final Pattern COMMA_PATTERN = Pattern.compile(",");

  static final Vector<BarcodeFormat> PRODUCT_FORMATS;//商品条形码格式
  static final Vector<BarcodeFormat> ONE_D_FORMATS;//一维码的格式
  static final Vector<BarcodeFormat> QR_CODE_FORMATS;//二维码的格式
  static final Vector<BarcodeFormat> DATA_MATRIX_FORMATS;//Datamatrix是二维码的一个成员
  static {
    //各种条形码的编码格式
    PRODUCT_FORMATS = new Vector<BarcodeFormat>(5);
    PRODUCT_FORMATS.add(BarcodeFormat.UPC_A);
    PRODUCT_FORMATS.add(BarcodeFormat.UPC_E);
    PRODUCT_FORMATS.add(EAN_13);
    PRODUCT_FORMATS.add(BarcodeFormat.EAN_8);
    PRODUCT_FORMATS.add(BarcodeFormat.RSS14);
    ONE_D_FORMATS = new Vector<BarcodeFormat>(PRODUCT_FORMATS.size() + 4);
    ONE_D_FORMATS.addAll(PRODUCT_FORMATS);
    ONE_D_FORMATS.add(BarcodeFormat.CODE_39);
    ONE_D_FORMATS.add(BarcodeFormat.CODE_93);
    ONE_D_FORMATS.add(BarcodeFormat.CODE_128);
    ONE_D_FORMATS.add(BarcodeFormat.ITF);
    QR_CODE_FORMATS = new Vector<BarcodeFormat>(1);
    QR_CODE_FORMATS.add(BarcodeFormat.QR_CODE);
    DATA_MATRIX_FORMATS = new Vector<BarcodeFormat>(1);
    DATA_MATRIX_FORMATS.add(BarcodeFormat.DATA_MATRIX);
  }

  private DecodeFormatManager() {}
//就是返回的编码格式
  static Vector<BarcodeFormat> parseDecodeFormats(Intent intent) {
    //获取intent传递过来的扫码的格式
    List<String> scanFormats = null;
    String scanFormatsString = intent.getStringExtra(Intents.Scan.SCAN_FORMATS);
    if (scanFormatsString != null) {
      scanFormats = Arrays.asList(COMMA_PATTERN.split(scanFormatsString));
    }
    //返回条形码的种类或者摸个确定的种类，用下面方法处理的原因是传递过来的数据我们不知道是一个还是有很多
    return parseDecodeFormats(scanFormats, intent.getStringExtra(Intents.Scan.MODE));
  }

  static Vector<BarcodeFormat> parseDecodeFormats(Uri inputUri) {
    List<String> formats = inputUri.getQueryParameters(Intents.Scan.SCAN_FORMATS);
    if (formats != null && formats.size() == 1 && formats.get(0) != null){
      formats = Arrays.asList(COMMA_PATTERN.split(formats.get(0)));
    }
    return parseDecodeFormats(formats, inputUri.getQueryParameter(Intents.Scan.MODE));
  }
  //返回扫描格式或者格式所属的条形码的种类
  private static Vector<BarcodeFormat> parseDecodeFormats(Iterable<String> scanFormats,
                                                          String decodeMode) {
    //把扫描格式转化成BarcodeFormat，并且返回
    if (scanFormats != null) {
      Vector<BarcodeFormat> formats = new Vector<BarcodeFormat>();
      try {
        for (String format : scanFormats) {
         // valueOf的作用是把 “EAN_13”格式转化成BarcodeFormat的对象，对象的名称还是EAN_13，
          formats.add(BarcodeFormat.valueOf(format));
        }
        //返回整个扫描的格式
        return formats;
      } catch (IllegalArgumentException iae) {
        // ignore it then
      }
    }
    //判断是输入商品条形码还是二维码或者其它的条形码
    if (decodeMode != null) {
      if (Intents.Scan.PRODUCT_MODE.equals(decodeMode)) {
        return PRODUCT_FORMATS;
      }
      if (Intents.Scan.QR_CODE_MODE.equals(decodeMode)) {
        return QR_CODE_FORMATS;
      }
      if (Intents.Scan.DATA_MATRIX_MODE.equals(decodeMode)) {
        return DATA_MATRIX_FORMATS;
      }
      if (Intents.Scan.ONE_D_MODE.equals(decodeMode)) {
        return ONE_D_FORMATS;
      }
    }
    return null;
  }

}
