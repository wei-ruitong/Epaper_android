package com.example.opencv_port;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.opencv_port.util.Message.Message;
import com.example.opencv_port.util.Mqtt.myMqtt;
import com.example.opencv_port.util.pictureTools.EpaperPicture;
import com.example.opencv_port.util.pictureTools.Bitmap2Hex;
import com.example.opencv_port.util.pictureTools.MethodOfOpencv;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener ,
        View.OnTouchListener, SeekBar.OnSeekBarChangeListener {
//    TODO： 声明控件
    private Button convert_btn,update_btn,save_btn,clean_button,canvas_update,write_button,update42_button;
    private EditText editText;
    private TextView textView;
    private ImageButton imageButton;
    private RadioGroup radioGroup;
    private SeekBar seekBar;
    private static final String TAG = "MainActivity";
    private ImageView imageView,imageView2,imageView3,imageView_cavas,cavas_show;
    public static myMqtt mqtt;
    public static final int REQUEST_CODE_CHOOSE_IMAGE = 1;
    public static final int REQUEST_CODE_CROP_IMAGE = 2;
    private Uri cropImageUri;
    private Mat imageMat;
    private Bitmap image_gray,img_floyd,image_resize,image_result,image_rotary,canvas_bitmap,origin_bitmap;
    private String strings;
    private Paint paint;
    private Canvas canvas;
    private int startX, startY, endX, endY;
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
        clean_button.setOnClickListener(this);
        canvas_update.setOnClickListener(this);
        write_button.setOnClickListener(this);
        update42_button.setOnClickListener(this);
        canvas();
        imageView_cavas.setOnTouchListener(this);
        seekBar.setOnSeekBarChangeListener(this);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.red_radio:
                        paint.setColor(Color.RED);
                        break;
                    case R.id.black_radio:
                        paint.setColor(Color.BLACK);
                        break;
                    case R.id.yellow_radio:
                        paint.setColor(Color.YELLOW);
                        break;
                }
            }
        });
    }
    private void canvas(){
        canvas_bitmap =  Bitmap.createBitmap(700,700,Bitmap.Config.ARGB_8888);
        canvas = new Canvas(canvas_bitmap);    // 创建一张画布,并图片放在画布上面
        canvas.drawColor(Color.argb(100,255,255,255));   // 设置画布背景颜色为白色
        paint = new Paint();
        paint.setStrokeWidth(10);
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        canvas.drawBitmap(canvas_bitmap,new Matrix(),paint);  //把灰色背景画在画布上
        imageView_cavas.setImageBitmap(canvas_bitmap);         // 把图片加载到ImageView上

    }
    private void initComponent()
    {
//        TODO :绑定控件
        update42_button = findViewById(R.id.update_42);
        cavas_show = findViewById(R.id.cavas_show);
        editText = findViewById(R.id.edit);
        write_button = findViewById(R.id.send);
        canvas_update =findViewById(R.id.canvas_update);
        clean_button = findViewById(R.id.clean);
        textView =  findViewById(R.id.pen_width);
        seekBar = findViewById(R.id.seekbar);
        radioGroup = findViewById(R.id.radiogroup);
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
//            TODO :按键点击事件
            case R.id.save_btn:
//                imageView3.setImageBitmap(origin_bitmap);
                Bitmap bitmap1 = MethodOfOpencv.resizePicture(origin_bitmap,400,300);
//                imageView3.setImageBitmap(bitmap1);
                Bitmap indexedImage1 = EpaperPicture.createIndexedImage(bitmap1, false, 400, 300, 0);
                imageView3.setImageBitmap(indexedImage1);
                String s = Bitmap2Hex.ConvertBitmap2HexBlackArray(indexedImage1);
                Message.sendMessage(s);
//                String s = Bitmap2Hex_Red.ConvertBitmap2HexRedArray(indexedImage1);

//                mqtt.sendMsg("x","EPAPER");
//                Log.i("LEN",String.valueOf(s.length()));
//                mqtt.sendMsg(s,"EPAPER");

                break;
            case R.id.send:
                String s1 = editText.getText().toString();
                Paint paint = new Paint();
                Typeface font = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
                paint.setTextSize(90);
                paint.setTypeface(font);
                canvas.drawText(s1,50,300,paint);
                paint.setTextSize(50);
                canvas.drawText(new Date().toLocaleString(),50,650,paint);
                canvas.drawBitmap(canvas_bitmap,new Matrix(),paint);
                imageView_cavas.setImageBitmap(canvas_bitmap);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

                break;
            case R.id.canvas_update:
                Bitmap bitmap2 = MethodOfOpencv.resizePicture(canvas_bitmap,200,200);
                Bitmap indexedImage = EpaperPicture.createIndexedImage(bitmap2, false, 200, 200, 0);
                String s2 = Bitmap2Hex.ConvertBitmap2HexBlackArray(indexedImage);
                mqtt.sendMsg(s2,"EPAPER");
                cavas_show.setImageBitmap(indexedImage);
                break;
            case R.id.clean:
                canvas_bitmap =  Bitmap.createBitmap(700,700,Bitmap.Config.ARGB_8888);
                canvas = new Canvas(canvas_bitmap);    // 创建一张画布,并图片放在画布上面
                canvas.drawColor(Color.argb(100,255,255,255));   // 设置画布背景颜色为白色
                paint = new Paint();
                paint.setStrokeWidth(10);
                paint.setAntiAlias(true);
                paint.setColor(Color.BLACK);
                canvas.drawBitmap(canvas_bitmap,new Matrix(),paint);  //把灰色背景画在画布上
                imageView_cavas.setImageBitmap(canvas_bitmap);
                break;
            case R.id.update_42:
                Bitmap bitmap3 = MethodOfOpencv.resizePicture(canvas_bitmap,400,300);
                Bitmap indexedImage2 = EpaperPicture.createIndexedImage(bitmap3, false, 400, 300, 0);
                String s3 = Bitmap2Hex.ConvertBitmap2HexBlackArray(indexedImage2);
                Message.sendMessage(s3);
                cavas_show.setImageBitmap(indexedImage2);
//                   saveBitmap(this,img_floyd);
//                   Toast.makeText(this,"图片保存成功！",Toast.LENGTH_SHORT).show();
                break;
            case R.id.imageButton:
                convert_btn.setEnabled(true);
                update_btn.setEnabled(true);
                Intent intent = new Intent(Intent.ACTION_PICK, null);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, REQUEST_CODE_CHOOSE_IMAGE);
                break;
            case R.id.update_btn:

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
                Bitmap three_color = EpaperPicture.createIndexedImage(image_resize,false,200,200,1);
                imageView3.setImageBitmap(three_color);
                strings = Bitmap2Hex.ConvertBitmap2HexBlackArray(img_floyd);
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
                             origin_bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(cropImageUri));
//                           将裁剪后的照片显示出来
                             imageButton.setImageBitmap(origin_bitmap);
//                           更改图片尺寸
                             image_resize = MethodOfOpencv.resizePicture(origin_bitmap,200,200);
//                           将图片旋转90度
//                             image_rotary = MethodOfOpencv.rotatePicture(image_resize,90);
//                           转化成灰度图片
                             image_gray =MethodOfOpencv.convert2GrayPicture(image_resize);
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

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.i("MyPaintToolsActivity", "ACTION_DOWN");
                // 获取鼠标按下时的坐标
                startX = (int) (event.getX() );
                startY = (int) (event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                Log.i("MyPaintToolsActivity", "ACTION_MOVE");
                // 获取鼠标移动后的坐标
                endX = (int) (event.getX() );
                endY = (int) (event.getY() );
                //在开始和结束之间画一条直线
                canvas.drawLine(startX, startY, endX, endY, paint);
                // 实时更新开始坐标
                startX = (int) (event.getX() );
                startY = (int) (event.getY());
                // 更新ImageView上的画布图片
                imageView_cavas.setImageBitmap(canvas_bitmap);
                break;
            case MotionEvent.ACTION_UP:
                Log.i("MyPaintToolsActivity", "ACTION_UP");
                break;
        }
        imageView_cavas.invalidate();
        return true;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        paint.setStrokeWidth(progress);
        textView.setText("画笔粗度： "+progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
