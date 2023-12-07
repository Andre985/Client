package org.example;

import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.RaspiPin;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;


public class Controller {

    public static void main(String[] args) throws InterruptedException {
        final String host = "<6f9f7fc904ab457abcf2476bd3d722ca>"; // use your host-name, it should look like '<alphanumeric>.s2.eu.hivemq.cloud'
        final String username = "<IOTGroup18>"; // your credentials
        final String password = "<IOTGroup18>";



        //Creating client

        final Mqtt5Client client = Mqtt5Client.builder()
                //.identifier("sensor-" + getMacAddress()) // use a unique identifier
                .serverHost(host)
                .automaticReconnectWithDefaultConfig() // the client automatically reconnects
                .serverPort(8883) // this is the port of your cluster, for mqtt it is the default port 8883
                .sslWithDefaultConfig() // establish a secured connection to HiveMQ Cloud using TLS
                .build();


            //connecting client
        client.toBlocking().connectWith()
                .simpleAuth() // using authentication, which is required for a secure connection
                .username(username) // use the username and password you just created
                .password(password.getBytes(StandardCharsets.UTF_8))
                .applySimpleAuth()
                .willPublish() // the last message, before the client disconnects
                .topic("home/will")
                .payload("sensor gone".getBytes())
                .applyWillPublish()
                .send();


        while (true) {
            client.toBlocking().publishWith()
                    .topic("home/CO2")
                    .payload(getTemp())
                    .send();

            TimeUnit.MILLISECONDS.sleep(500);

            client.toBlocking().publishWith()
                    .topic("home/temperature")
                    .payload(readInputC02())
                    .send();

            TimeUnit.MILLISECONDS.sleep(500);
        }



    }

    private static byte[] getTemp() {
        // simulate a temperature sensor with values between 20°C and 30°C
        final int temperature = ThreadLocalRandom.current().nextInt(20, 30);
        return (temperature + "°C").getBytes(StandardCharsets.UTF_8);
    }


    private static byte[] readInputC02() {
        GpioController gpio = GpioFactory.getInstance();
        final GpioPinDigitalInput inputPin;
        //read from pin
        inputPin = gpio.provisionDigitalInputPin(RaspiPin.GPIO_04);//Sensor input from Pin 2 on RPI


        final String CO22 = inputPin.toString();
        return (CO22 + "rec. from RPI").getBytes(StandardCharsets.UTF_8);
    }




}
