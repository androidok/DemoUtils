package jia.youmeng.com.uploadimage;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private static final int FILE_SELECT_CODE = 0X111;
    private static final int  REQUEST_CODE=0X112;
    ImageView iv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iv=(ImageView) findViewById(R.id.iv);
    }

    public void OpenSystemFile(View v) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "请选择文件!"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "请安装文件管理器", Toast.LENGTH_SHORT).show();
        }
    }

    public void OpenSystemPhoto(View view){
        Intent intent = new Intent();
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        //根据版本号不同使用不同的Action
        if (Build.VERSION.SDK_INT <19) {
            intent.setAction(Intent.ACTION_GET_CONTENT);
        }else {
            //大于19使用的action
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        }
        startActivityForResult(intent, REQUEST_CODE);
    }



    /** 根据返回选择的文件，来进行操作 **/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if (resultCode == Activity.RESULT_OK) {
            if(requestCode==FILE_SELECT_CODE){
                // Get the Uri of the selected file
                Uri uri = data.getData();
                //获取文件的路径
                String ss=uri.getPath();
                Toast.makeText(getApplicationContext(), ss, Toast.LENGTH_LONG).show();
            }else if(requestCode==REQUEST_CODE){
                getPathFromSystemPhoto(data);
                getImageFromContresloverPhoto(data);
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    //通过getContentResolver() 获取到图片
    public void 	getImageFromContresloverPhoto(Intent data){
        Uri uri = data.getData();
        //获取内容提供者的uri
        Uri ss=Uri.parse(uri.toString());
        Toast.makeText(getApplicationContext(), ss.toString(), Toast.LENGTH_LONG).show();

        //根据需要，也可以加上Option这个参数
        InputStream inputStream;
        try {
            //获取输入流
            inputStream = getContentResolver().openInputStream(uri);
            //通过输入流把图片转成bitmap
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            iv.setImageBitmap(bitmap);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    //通过getContentResolver() 获取到图片路径
    public void 	getPathFromSystemPhoto(Intent intent){
        Uri uri = intent.getData();
        String[] proj = {MediaStore.Images.Media.DATA};
        //好像是android多媒体数据库的封装接口，具体的看Android文档，通过uri获取到图片的绝对路径
        Cursor cursor = getContentResolver().query(uri, proj, null, null, null);
        //按我个人理解 这个是获得用户选择的图片的索引值
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        //将光标移至开头 ，这个很重要，不小心很容易引起越界
        cursor.moveToFirst();
        //最后根据索引值获取图片路径www.2cto.com
        String path = cursor.getString(column_index);
        Toast.makeText(getApplicationContext(), path, Toast.LENGTH_LONG).show();
    }
}
