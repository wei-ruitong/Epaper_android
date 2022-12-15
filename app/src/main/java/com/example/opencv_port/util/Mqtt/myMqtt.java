package com.example.opencv_port.util.Mqtt;

import android.util.Log;
import android.widget.Toast;

import com.example.opencv_port.MainActivity;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class myMqtt {
    private static String TAG = "myMqtt";
    private static String userName = "322443"; //非必须
    private static String password = "AQMGbhRTeCQafE=3jeyxF3ezX80="; //非必须
    private static String clientId = "709603626";
    private static String HOST = "tcp://183.230.40.39:6002"; //本地ipv4
    private static MqttConnectOptions options;
    private static MqttClient client;
    private static final int QOS0 = 0;//只发送一次 可能会丢失
    public static String TOPIC = "photo_topic";
    private static final ExecutorService executorService = Executors.newCachedThreadPool();



    public  void initMQTTClient(){
        try{
            //MQTT的连接设置
            options = new MqttConnectOptions();
            //host为主机名，clientid即连接MQTT的客户端ID，一般以客户端唯一标识符表
            client = new MqttClient(HOST, clientId, new MemoryPersistence());
            //设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
            options.setCleanSession(false);
            //options.setWill(myTopic,null,2,false); //遗嘱   断开连接时，会发送一条信息
            //设置连接的用户名
            options.setUserName(userName);
            //设置连接的密码
            options.setPassword(password.toCharArray());
            // 设置超时时间 单位为秒
            options.setConnectionTimeout(10);
            // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
            options.setKeepAliveInterval(120);
            //设置回调
            client.setCallback(mqttCallBack);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public  final MqttCallbackExtended mqttCallBack = new MqttCallbackExtended() {
        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
//        链接成功,订阅主题
            subscribeTopic();
        }

        @Override
        public void connectionLost(Throwable cause) {
            startReconnect();

        }
        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            byte[] realMessage = message.getPayload();
            String result = new String(message.getPayload());
            Log.i(TAG,result);
        }
        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
//        Toast.makeText(MainActivity.this,"发送成功！",Toast.LENGTH_SHORT).show();
            Log.i(TAG,"发送成功");
        }
    };
    /**
     * 连接服务器
     */
    public  void connectService(){
        try{
            if(!client.isConnected()){
                client.connect(options);
            }
        }catch(Exception e){
            Log.e(TAG,"链接失败！");
        }
    }
    /**
     * 重连
     */
    public  void startReconnect(){
        executorService.execute(this::connectService);
    }
    /**
     * 订阅主题
     */
    public  void subscribeTopic(){
        try{
            if(client.isConnected()){
                int[] Qos = {QOS0};
                String[] topic1 = {TOPIC};
                client.subscribe(topic1, Qos);
            }else{

            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    /**
     * 发送消息
     *
     * @param msg   消息
     * @param topic 主题
     */
    public void sendMsg(String msg, String topic){
        MqttMessage message = new MqttMessage();
        message.setPayload(msg.getBytes());
        message.setQos(QOS0);
        message.setRetained(false);
        try{
            if(client != null && client.isConnected()){
                client.publish(topic, message);
            }
        }catch(MqttException e){
            e.printStackTrace();
        }
    }

}
