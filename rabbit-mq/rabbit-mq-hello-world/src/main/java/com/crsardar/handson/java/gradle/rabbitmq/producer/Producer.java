package com.crsardar.handson.java.gradle.rabbitmq.producer;
/**
 * @author Chittaranjan Sardar
 */

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Producer
{

    private final static String QUEUE_NAME = "hello";

    public static void main(String[] argv) throws Exception
    {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");

        try (Connection connection = connectionFactory.newConnection();
                Channel channel = connection.createChannel())
        {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);

            System.out.println("Type a message and press Enter & See the console of Consumer! Press CTRL+C to exit.");

            Scanner scanner = new Scanner(System.in);

            while (true) {

                String message = scanner.nextLine();

                channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
            }
        }
    }
}
