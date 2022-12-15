package com.example.opencv_port.util.pictureTools;

import android.graphics.Bitmap;

public class Bitmap2Hex {
    /**
     * 将抖动后的Bitmap图片(此时图片为三通道图片)转化为1维数组
     * 即每个像素点只由1bit数据为表示，要么为0，要么为1
     * @param bitmap
     * @return string字符串，每8个像素点构成1个0-255的整数
     * 两个整数中间用“，”隔开。如：243,255,45....
     */
    public static String ConvertBitmap2HexArray(Bitmap bitmap){
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        int[] image_array = new int[width*height];
        int count = 0,byte_count= 0;
        StringBuffer image_buffer = new StringBuffer();
        String s;
        for (int x=0;x<height;x++){
            for (int y = 0;y<width;y++){
                count ++;
                int lowbit = bitmap.getPixel(x,y)&0xff;
                if (lowbit<128){
                    image_array[(x+1)*height-y-1] = 0;
                }else {
                    image_array[(x+1)*height-y-1] = 1;
                }
            }
            for (int z = 0;z<(width/8);z++){
                s="";
                for (int k =0 ;k<8;k++){
                    s+=image_array[x*height+k+z*8];
                }
                image_buffer.append(Integer.parseInt(s,2));
                image_buffer.append(",");
            }
            }
        return image_buffer.toString();
    }
}
