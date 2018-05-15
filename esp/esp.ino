// #include "ArduinoJson.h"
// #include <ESP8266WiFi.h>
// #include <SocketIOClient.h>
// #include "DHT.h"

// #define DHTPIN 0
// #define DHTTYPE DHT11

// DHT dht(DHTPIN, DHTTYPE);
// SocketIOClient socketClient;
// /**
//  * Khai báo thông tin wifi
//  */
// const char* ssid = "TPLINK";
// const char* password = "01632763089tplink";

// /**
//  * Địa chỉ của server
//  */
// char host[] = "192.168.0.101";
// //char host[] = "test-esp.herokuapp.com";
// int port = 3000;
// char nsp[] = "esp8266";

// /**
//  * Khai báo chân cho các replay
//  */
// const int decive_1 = 14;
// const int decive_2 = 12;
// const int decive_3 = 13;
// const int decive_4 = 15;

// /**
//  * Các biến chứa thông tin của data từ server gửi tới
//  */
// extern String RID;
// extern String Rfull;

// void setup()
// {
//   Serial.begin(9600);
//   delay(10);
  
//   //Kết nối vào mạng Wifi
//   WiFi.begin(ssid, password);
//   Serial.print("Connecting");
//   while (WiFi.status() != WL_CONNECTED){ 
//     delay(500);
//     Serial.print('.');
//   }
//   Serial.println();
//   Serial.print("Connected, IP address: ");
//   Serial.println(WiFi.localIP());
  
//   //Kết nối tới server
//   if (!socketClient.connect(host, port, nsp)){
//     return;
//   }
//   dht.begin(); //Khởi tạo biến dht đọc cảm biến. 
  
//   //Set mode cho các replay
//   pinMode(decive_1, OUTPUT);        //Set decive_1 là OUTPUT
//   pinMode(decive_2, OUTPUT);        //Set decive_2 là OUTPUT
//   pinMode(decive_3, OUTPUT);        //Set decive_2 là OUTPUT
//   pinMode(decive_4, OUTPUT);        //Set decive_2 là OUTPUT
// }

// const unsigned long CHU_KY = 5000UL;
// unsigned long chuky = 0;
// void loop()
// {
//   if(millis() - chuky > CHU_KY){ //Sau CHU_KY ms thì chu kỳ lặp lại
//     chuky = millis();
//     sendData();
//   }
//   //Bắt gói tin Server gửi tới và thực hiện control
//   if(socketClient.monitor()){
//     control();
//   }
//   //Kết nối lại Server khi mất kết nối
//   if(!socketClient.connected()){
//     socketClient.reconnect(host, port, nsp);
//   }
// }

// /**
//  * Hàm gửi thông tin cảm biến và trạng thái 
//  * của các replay lên Server
//  */
// void sendData()
// {
//   //Đọc thông tin cảm biến
//   float humidity = dht.readHumidity();
//   float temperature = dht.readTemperature();
  
//   //Lưu thông tin cảm biến và trạng thái replay và Json
//   StaticJsonBuffer<500> jsonBuffer;
//   JsonObject& root = jsonBuffer.createObject();
//   root["Humidity"] = humidity;
//   root["Temperature"] = temperature;
//   JsonArray& device = root.createNestedArray("Status");
//   device.add(digitalRead(decive_1));
//   device.add(digitalRead(decive_2));
//   device.add(digitalRead(decive_3));
//   device.add(digitalRead(decive_4));
//   String json;
//   root.printTo(json); //Chuyen doi sang String
//   socketClient.sendJSON("ESP_APP", json);
// }

// /**
//  * Hàm điều khiển replay khi có lệnh từ server
//  */
// void control()
// {
//   StaticJsonBuffer <200> jsonBuffer;                  
//   JsonObject& root = jsonBuffer.parseObject(Rfull);    
//   int decive_1_Status = root["Status"][0];         
//   int decive_2_Status = root["Status"][1];      
//   int decive_3_Status = root["Status"][2];
//   int decive_4_Status = root["Status"][3];

//   digitalWrite(decive_1, decive_1_Status);
//   digitalWrite(decive_2, decive_2_Status);      
//   digitalWrite(decive_3, decive_3_Status);
//   digitalWrite(decive_4, decive_4_Status);
// }


