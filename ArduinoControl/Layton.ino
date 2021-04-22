//Libraries
#include <SoftwareSerial.h> //Para controlar el módulo Bluetooth HC-05
#include <Servo.h> //Para controlar los servomotores


//Variables
SoftwareSerial myBT(10, 11); //10 Rx, 11 Tx
Servo myServo;

/*Modulo Bluetooth HC-05*/
char StringFin = '.';           // Char que detiene el guardado del Buffer
String Buffer = "";             // Buffer para guardar la entrada completa

/*Módulo L298N*/
int INa = 13;    // Sentido antihorario
int INb = 12;    // Sentido horario
int ENout = A0;  // Enable (la velocidad)
int vel = 0;
bool play = false;

/*Luces LED*/
int LEDout = 8;
int LEDin = 7;


//Program
void setup() {
  myBT.begin(38400);

  myServo.attach(9);
  myServo.write(90);

  pinMode(INa, OUTPUT);
  pinMode(INb, OUTPUT);
  pinMode(ENout, OUTPUT);
  digitalWrite(INa, LOW);
  digitalWrite(INb, LOW);
  analogWrite(ENout, 0);

  pinMode(LEDout, OUTPUT);
  pinMode(LEDin, INPUT_PULLUP);
  digitalWrite(LEDout, LOW);
}

void loop() {
  if (myBT.available()) {

    //Guardo en el buffer la señal de entrada

    Buffer = "";
    while (true) {
      char c = myBT.read();
      if (c == StringFin) {
        break;
      }
      Buffer = Buffer + c;
    }

    //Ir recto
    if(Buffer == "U"){
      myBT.write("u");
      digitalWrite(INb, LOW);
      digitalWrite(INa, HIGH);
      myServo.write(90);
    }

    //Ir marcha atrás;
    if (Buffer == "D"){
      myBT.println("d");
      digitalWrite(INa, LOW);
      digitalWrite(INb, HIGH);
      myServo.write(90);
    } 

    //Ruedas giradas a la izquierda
    if (Buffer == "L"){
      myBT.println("l");
      myServo.write(45);
    }

    //Ruedas giradas a la derecha
    if (Buffer == "R"){
      myBT.println("r");
      myServo.write(135);
    }

    //Detener-activar el coche
    if (Buffer == "P"){
      if (!play){
        analogWrite(ENout, vel);
        play = true;
        myBT.println("P");//Play
      } else {
        analogWrite(ENout, 0);
        play = false;
        myBT.println("p");//Pause
      }
    }
    
    //Establecer la velocidad
    if (Buffer.length() > 2){
      vel = Buffer.substring(2).toInt();
      vel = map(vel, 0, 135, 0, 255);
    }  

    //Apagar-encender las luces
    if (Buffer == "O"){
      if (digitalRead(LEDin) == LOW){
        digitalWrite(LEDout, HIGH);
        myBT.println("O");//ON
      } else {
        digitalWrite(LEDout, LOW);
        myBT.println("o");//OFF
      }
    } 
  }

  if (play == true){
        analogWrite(ENout, vel);
      }
}
