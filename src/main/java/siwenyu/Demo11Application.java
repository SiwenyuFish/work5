package siwenyu;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableRabbit
@SpringBootApplication
public class Demo11Application {

    public static void main(String[] args) {
        SpringApplication.run(Demo11Application.class, args);
    }

}
