package ermarov.ermakovHomeWorks.dependencyInjection;

import ermarov.zadachnikOOP.Block5.Student.Student;

import java.util.Random;

public class Config {
    @Bean
    public Planet earth(){
        return new Planet("earth");
    }

    @Prototype
    @Bean
    public Planet mars(){
        return new Planet("mars");
    }

    @Prototype
    @Bean
    public Planet upiter(){
        return new Planet("upiter");
    }

    @Bean
    public Student student(){
        return new Student("student");
    }

    @Prototype
    @Bean("sun")
    public Star sun(){
        Random r = new Random();
        return new Star(r.nextInt(1000));
    }
}
