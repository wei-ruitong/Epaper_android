package com.example.opencv_port.util.Message;

import com.example.opencv_port.MainActivity;
import com.example.opencv_port.util.Mqtt.myMqtt;

public class Message {
    public static void sendMessage(String message){
        if(message.charAt(message.length()/2)==','){
            MainActivity.mqtt.sendMsg(message.substring(0,message.length()/2+1),"EPAPER_42");
            new Thread(){
                @Override
                public void run() {
                    try {
                        sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
            MainActivity.mqtt.sendMsg(message.substring(message.length()/2+1),"EPAPER_42");
        }else if(message.charAt(message.length()/2+1)==','){
            MainActivity.mqtt.sendMsg(message.substring(0,message.length()/2+2),"EPAPER_42");
            new Thread(){
                @Override
                public void run() {
                    try {
                        sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
            MainActivity.mqtt.sendMsg(message.substring(message.length()/2+2),"EPAPER_42");
        }else if(message.charAt(message.length()/2+2)==','){
            MainActivity.mqtt.sendMsg(message.substring(0,message.length()/2+3),"EPAPER_42");
            new Thread(){
                @Override
                public void run() {
                    try {
                        sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
            MainActivity.mqtt.sendMsg(message.substring(message.length()/2+3),"EPAPER_42");
        }else if(message.charAt(message.length()/2+3)==','){
            MainActivity.mqtt.sendMsg(message.substring(0,message.length()/2+4),"EPAPER_42");
            new Thread(){
                @Override
                public void run() {
                    try {
                        sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
            MainActivity.mqtt.sendMsg(message.substring(message.length()/2+4),"EPAPER_42");
        }

    }
}
