package jia.youmeng.com.photogaibian;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.ImageView;

/**
 * Created by jia on 2017/5/12.
 */

public class scaleAndalpha extends Activity{
    private ImageView iv, ivCopy;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scaleandroate);

        iv = (ImageView) findViewById(R.id.iv);
        ivCopy = (ImageView) findViewById(R.id.iv_copy);

        // 界面一加载去显示一张图片

        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.car);
        iv.setImageBitmap(bitmap);
        // 修改 bitmap某一个点的色值
        // 不能修改原图

        // 创建一个副本

		/*
		 * 1.宽高一样 2、颜色质量（材质一样） 3.内容一样
		 */
        daoying();
    }
public scaleAndalpha(Bitmap bitmap){
    this.bitmap=bitmap;


}


    private void rotate() {

        new Thread() {
            int degree = 0;

            public void run() {
                while (degree < 360) {
                    SystemClock.sleep(1000);
                    degree += 6;

                    // bitmapCopy空白图片
                    final Bitmap bitmapCopy = Bitmap.createBitmap(
                            bitmap.getWidth(), bitmap.getHeight(),
                            bitmap.getConfig());

                    // 画上一些内容 画笔Paint （画笔） 画布Canvas（绿色画布）
                    // 将白纸垫在画布上
                    Canvas canvas = new Canvas(bitmapCopy);
                    // 画的内容跟原图一样

                    // 线性代数 matrix 表示图形变化的方式

                    Matrix matrix = new Matrix();
                    // 以某个点为基准点旋转 旋转了多少度
                    matrix.setRotate(degree, bitmapCopy.getWidth() / 2,
                            bitmapCopy.getHeight() / 2);

                    canvas.drawBitmap(bitmap, matrix, new Paint());
                    // 副本可以进行图片的修改
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            ivCopy.setImageBitmap(bitmapCopy);

                        }
                    });

                }
            }
        }.start();

    }

    private void carRun() {

        new Thread() {
            int x = 0;

            public void run() {
                while (x < bitmap.getWidth()) {
                    SystemClock.sleep(50);
                    x += 1;

                    // bitmapCopy空白图片
                    final Bitmap bitmapCopy = Bitmap.createBitmap(
                            bitmap.getWidth() * 2, bitmap.getHeight(),
                            bitmap.getConfig());

                    // 画上一些内容 画笔Paint （画笔） 画布Canvas（绿色画布）
                    // 将白纸垫在画布上
                    Canvas canvas = new Canvas(bitmapCopy);
                    // 画的内容跟原图一样

                    // 线性代数 matrix 表示图形变化的方式

                    Matrix matrix = new Matrix();
                    // 以某个点为基准点旋转 旋转了多少度
                    matrix.setTranslate(x, 0);

                    canvas.drawBitmap(bitmap, matrix, new Paint());
                    // 副本可以进行图片的修改
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            ivCopy.setImageBitmap(bitmapCopy);

                        }
                    });

                }
            }
        }.start();

    }

    private void scale() {

        // bitmapCopy空白图片
        final Bitmap bitmapCopy = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), bitmap.getConfig());

        // 画上一些内容 画笔Paint （画笔） 画布Canvas（绿色画布）
        // 将白纸垫在画布上
        Canvas canvas = new Canvas(bitmapCopy);
        // 画的内容跟原图一样

        // 线性代数 matrix 表示图形变化的方式

        Matrix matrix = new Matrix();
        // 以左上方为基准点缩放
        // matrix.setScale(0.5f, 0.5f);
        // 以中心点
        matrix.setScale(0.5f, 0.5f, bitmapCopy.getWidth() / 2,
                bitmapCopy.getHeight() / 2);

        canvas.drawBitmap(bitmap, matrix, new Paint());
        // 副本可以进行图片的修改

        // TODO Auto-generated method stub
        ivCopy.setImageBitmap(bitmapCopy);

    }


    private void mirror() {

        // bitmapCopy空白图片
        final Bitmap bitmapCopy = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), bitmap.getConfig());

        // 画上一些内容 画笔Paint （画笔） 画布Canvas（绿色画布）
        // 将白纸垫在画布上
        Canvas canvas = new Canvas(bitmapCopy);
        // 画的内容跟原图一样

        // 线性代数 matrix 表示图形变化的方式

        Matrix matrix = new Matrix();
        // 以左上方为基准点缩放
        // matrix.setScale(0.5f, 0.5f);
        // 以中心点
        matrix.setScale(-1f, 1f, bitmapCopy.getWidth() / 2,
                bitmapCopy.getHeight() / 2);

        canvas.drawBitmap(bitmap, matrix, new Paint());
        // 副本可以进行图片的修改

        // TODO Auto-generated method stub
        ivCopy.setImageBitmap(bitmapCopy);

    }


    private void daoying() {

        // bitmapCopy空白图片
        final Bitmap bitmapCopy = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), bitmap.getConfig());

        // 画上一些内容 画笔Paint （画笔） 画布Canvas（绿色画布）
        // 将白纸垫在画布上
        Canvas canvas = new Canvas(bitmapCopy);
        // 画的内容跟原图一样

        // 线性代数 matrix 表示图形变化的方式

        Matrix matrix = new Matrix();
        // 以左上方为基准点缩放
        // matrix.setScale(0.5f, 0.5f);
        // 以中心点
        matrix.setScale(1f, -1f, bitmapCopy.getWidth() / 2,
                bitmapCopy.getHeight() / 2);

        canvas.drawBitmap(bitmap, matrix, new Paint());
        // 副本可以进行图片的修改

        // TODO Auto-generated method stub
        ivCopy.setImageBitmap(bitmapCopy);

    }


}
