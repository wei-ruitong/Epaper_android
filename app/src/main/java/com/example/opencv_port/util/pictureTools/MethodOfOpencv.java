package com.example.opencv_port.util.pictureTools;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class MethodOfOpencv {
    /**
     * 按照角度angle旋转bitmap图片
     * @param bitmap
     * @param angle
     * @return
     */
    public static Bitmap rotatePicture(Bitmap bitmap, int angle){
        Mat mat1 = new Mat();
        Utils.bitmapToMat(bitmap,mat1);
        Mat image_rot = Imgproc.getRotationMatrix2D(new Point(bitmap.getWidth() / 2-0.5, bitmap.getHeight() / 2-0.5), angle, 1);
        Mat mat2 = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC1);
        Imgproc.warpAffine(mat1,mat2,image_rot,mat2.size());
        bitmap=Bitmap.createBitmap(mat2.width(), mat2.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat2, bitmap);
        return bitmap;
    }

    /**
     * 根据width和height 设置图片的尺寸
     * @param bitmap
     * @param width
     * @param height
     * @return
     */
    public static Bitmap resizePicture(Bitmap bitmap,int width,int height){
        Mat mat1 = new Mat();
        Mat mat2 = new Mat();
        Utils.bitmapToMat(bitmap,mat1);
        Imgproc.resize(mat1,mat2, new Size(width,height));
        bitmap=Bitmap.createBitmap(mat2.width(), mat2.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat2, bitmap);
        return bitmap;
    }

    /**
     * 将图图片转化成灰度图
     * @param bitmap
     * @return
     */
    public static Bitmap convert2GrayPicture(Bitmap bitmap){
        Mat mat1 = new Mat();
        Utils.bitmapToMat(bitmap,mat1);
        Mat mat2 = new Mat();
        Imgproc.cvtColor(mat1, mat2, Imgproc.COLOR_BGRA2GRAY);
        bitmap = Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat2, bitmap);
        return bitmap;
    }
}
