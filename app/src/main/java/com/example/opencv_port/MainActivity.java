package com.example.opencv_port;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.opencv_port.util.Mqtt.myMqtt;
import com.example.opencv_port.util.pictureTools.EpaperPicture;
import com.example.opencv_port.util.pictureTools.Bitmap2Hex;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
//    TODO： 声明控件
    private Button convert_btn,update_btn,save_btn;
    private ImageButton imageButton;
    private static final String TAG = "MainActivity";
    private ImageView imageView,imageView2,imageView3,imageView_cavas;
    private myMqtt mqtt;
    public static final int REQUEST_CODE_CHOOSE_IMAGE = 1;
    public static final int REQUEST_CODE_CROP_IMAGE = 2;
    private Uri cropImageUri;
    private Mat imageMat;
    private Bitmap image_gray,img_floyd,image_resize,image_result,image_rotary;
    private String strings;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestAllPower();//获取动态权限
//        初始化控件
        initComponent();
        mqtt = new myMqtt();
        mqtt.initMQTTClient();
        mqtt.connectService();
        update_btn.setOnClickListener(this);
        convert_btn.setOnClickListener(this);
        save_btn.setOnClickListener(this);
        imageButton.setOnClickListener(this);
    }
    private void initComponent()
    {
//        TODO :绑定控件
        convert_btn =findViewById((R.id.convert_btn));
        update_btn = findViewById(R.id.update_btn);
        save_btn = findViewById(R.id.save_btn);
        imageButton = findViewById(R.id.imageButton);
        imageView =findViewById(R.id.imageview);
        imageView2 =findViewById(R.id.imageview2);
        imageView3 =findViewById(R.id.imageview3);
        imageView_cavas = findViewById(R.id.cavas);

    }
    public void requestAllPower() {

        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.INTERNET,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1);

    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.save_btn:
                   saveBitmap(this,img_floyd);
                   Toast.makeText(this,"图片保存成功！",Toast.LENGTH_SHORT).show();
                break;
            case R.id.imageButton:
                convert_btn.setEnabled(true);
                update_btn.setEnabled(true);
                Intent intent = new Intent(Intent.ACTION_PICK, null);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, REQUEST_CODE_CHOOSE_IMAGE);

                break;
            case R.id.update_btn:
//                StringBuffer stringBuffer = new StringBuffer();
//                for (int i=0;i<5000;i++){
//                    stringBuffer.append(strings);
//                    stringBuffer.append(",");
//                }
                mqtt.sendMsg(strings,"EPAPER");
                break;
            case R.id.convert_btn:
                save_btn.setEnabled(true);
//                显示灰度图像
                imageView.setImageBitmap(image_gray);
//                显示黑白抖动图像
                img_floyd = EpaperPicture.createIndexedImage(image_resize,false,200,200,0);
                imageView2.setImageBitmap(img_floyd);
//                显示黑白红三色颜色抖动图像
                Bitmap bb = EpaperPicture.createIndexedImage(image_resize,false,200,200,1);
                imageView3.setImageBitmap(bb);
                //                TODO ：将图片转化为1维数组
                image_result = EpaperPicture.createIndexedImage(image_rotary,false,200,200,0);
                strings = Bitmap2Hex.ConvertBitmap2HexArray(image_result);
//                Log.i("HEX",strings);
                break;
        }
    }

    /**
     * 裁减图片操作
     *
     * @param
     */
    private void startCropImage(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // 使图片处于可裁剪状态
        intent.putExtra("crop", "true");
        // 裁剪框的比例（根据需要显示的图片比例进行设置）
        if (Build.MANUFACTURER.contains("HUAWEI")) {
            //硬件厂商为华为的，默认是圆形裁剪框，这里让它无法成圆形
            intent.putExtra("aspectX", 9999);
            intent.putExtra("aspectY", 9998);
        }else{
            //其他手机一般默认为方形
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
        }
//        intent.putExtra("outputX", 500);
//        intent.putExtra("outputY", 500);
        // 设置裁剪区域的形状，默认为矩形，也可设置为圆形，可能无效
        //intent.putExtra("circleCrop", true);
        // 让裁剪框支持缩放
        intent.putExtra("scale", true);
        // 传递原图路径
        File cropFile = new File(Environment.getExternalStorageDirectory() + "/crop_image.jpg");
        try {
            if (cropFile.exists()) {
                cropFile.delete();
            }
            cropFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        cropImageUri = Uri.fromFile(cropFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cropImageUri);
        // 设置图片的输出格式
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        // return-data=true传递的为缩略图，小米手机默认传递大图，所以会导致onActivityResult调用失败
        intent.putExtra("return-data", false);
        startActivityForResult(intent, REQUEST_CODE_CROP_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (intent != null) {
            switch (requestCode) {
                // 将选择的图片进行裁剪
                case REQUEST_CODE_CHOOSE_IMAGE:
                    if (intent.getData() != null) {
                        Uri iconUri = intent.getData();
                        startCropImage(iconUri);
                    }
                    break;
                case REQUEST_CODE_CROP_IMAGE:
                    if (resultCode == RESULT_OK) {
                        try {
                            Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(cropImageUri));
                            // 将裁剪后的照片显示出来
                            imageButton.setImageBitmap(bitmap);
                            Mat mat_src = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC4);
                            Utils.bitmapToMat(bitmap, mat_src);
                            Mat mat_result = new Mat();
//                          更改图片尺寸
                            Imgproc.resize(mat_src,mat_result, new Size(200,200));
                            image_resize=Bitmap.createBitmap(mat_result.width(), mat_result.height(), Bitmap.Config.ARGB_8888);
                            Utils.matToBitmap(mat_result, image_resize);
//                            将图片旋转90度
                            Mat image_rot = Imgproc.getRotationMatrix2D(new Point(mat_result.width() / 2-0.5, mat_result.height() / 2-0.5), 90, 1);
                            Mat mat_result2 = new Mat();
                            Imgproc.warpAffine(mat_result,mat_result2,image_rot,mat_result.size());
                            image_rotary=Bitmap.createBitmap(mat_result.width(), mat_result.height(), Bitmap.Config.ARGB_8888);
                            Utils.matToBitmap(mat_result2, image_rotary);
//                            图片镜像
//                            Mat mat_result3 = new Mat();
//                            Imgproc.filter2D(mat_result2,mat_result3,1,);
//                           转化成灰度图片

                            Mat mat_gray = new Mat(mat_result.width(),mat_result.height(), CvType.CV_8UC1);
                            Imgproc.cvtColor(mat_result, mat_gray, Imgproc.COLOR_BGRA2GRAY);
                            image_gray = Bitmap.createBitmap(mat_result.width(), mat_result.height(), Bitmap.Config.ARGB_8888);
                            Utils.matToBitmap(mat_gray, image_gray);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                default:
                    break;

            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 加载并初始化OpenCV类库
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_10, this, mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

    }
    /**
     * 加载回调
     */
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    Log.i(TAG, "OpenCV loaded successfully");
                    imageMat=new Mat();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    public void saveBitmap(Context context, Bitmap bmp) {
        //检查有没有存储权限
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, "请至权限中心打开应用权限", Toast.LENGTH_SHORT).show();
        } else {
            // 新建目录appDir，并把图片存到其下
            File appDir = new File(context.getExternalFilesDir(null).getPath()+ "BarcodeBitmap");
            if (!appDir.exists()) {
                appDir.mkdir();
            }
            String fileName = System.currentTimeMillis() + ".jpg";
            File file = new File(appDir, fileName);
            try {
                FileOutputStream fos = new FileOutputStream(file);
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // 把file里面的图片插入到系统相册中
            try {
                MediaStore.Images.Media.insertImage(context.getContentResolver(),
                        file.getAbsolutePath(), fileName, null);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            Toast.makeText(this, fileName, Toast.LENGTH_LONG);

            // 通知相册更新
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
        }
    }

}
