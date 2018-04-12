#include "ArduinoJson.h"
#include <ESP8266WiFi.h>
#include "SocketIOClient.h"
#include "DHT.h"

#define DHTPIN 2
#define DHTTYPE DHT11

DHT dht(DHTPIN, DHTTYPE);
SocketIOClient client;                  //Khia báo biến client.
const char* ssid = "Toto";              //Tên mạng Wifi mà Socket server đang kết nối.
const char* password = "hoilamgi";      //Pass mạng Wifi mà Socket server đang kết nối.

//char host[] = "192.168.1.17";              //Địa chỉ IP của Socket server, nếu đưa ra Internet thì thay bằng tên miền.
char host[] = "test-esp.herokuapp.com";
int port = 80;                          //Cổng dịch vụ tạo trong Socket server
//char namespace_esp8266[] = "esp8266";     //Namespace của esp8266

extern String tmpdata;  //External variables
extern String RID;
extern String Rname;
extern String Rcontent;

void setup()
{
  Serial.begin(9600);
  delay(10);
  WiFi.begin(ssid, password);           //Kết nối vào mạng Wifi.
  Serial.print("Connecting");
  while (WiFi.status() != WL_CONNECTED){ 
    delay(500);
    Serial.print('.');
  }
  Serial.println();
  Serial.print("Connected, IP address: ");
  Serial.println(WiFi.localIP());
  if (!client.connect(host, port)){    //Khi kết nối thất bại thì kết nối lại.
    return;
  }
  dht.begin(); //Khởi tạo biến dht đọc cảm biến.  
}

void loop()
{
  delay(5000);
  float h = dht.readHumidity(); //Đọc độ ẩm
  float t = dht.readTemperature(); //Đọc nhiệt độ theo độ C
  //Gửi lên Server
  sendSensor(h, t);
  //Kết nối lại cho ESP khi mất mạng
  if(client.monitor()){
//    Serial.print("tmpdata: ");
//    Serial.println(tmpdata);
    Serial.print("RID: ");
    Serial.println(RID);
  }
  if(!client.connected()){
    client.reconnect(host, port);
  }
}

//Hàm gửi thông tin cảm biến lên Server
void sendSensor(float h, float t)
{
  StaticJsonBuffer<500> jsonBuffer;
  JsonObject& root = jsonBuffer.createObject();
  root["Humidity"] = h;
  root["Temperature"] = t;
  String json;
  root.printTo(json); //Chuyen doi sang String
  //Serial.println(json);
  client.sendJSON("SENSOR", json);
}
