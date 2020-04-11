package ermarov.ermakovHomeWorks.dependencyInjection;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Throwable {

        Container container = new Container(SolarConfig.class, Config.class);
        SolarSystem sSystem = container.getBean("sSys", SolarSystem.class);
        System.out.println(sSystem);

    }
}

class SolarSystem{
    List<Planet> planets = new ArrayList<>();
    Star star;

    @Override
    public String toString() {
        return "SolarSystem{" +
                "planets=" + planets +
                ", star=" + star +
                '}';
    }
}

class Planet{
    String name;

    public Planet(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Planet{" +
                "name='" + name + '\'' +
                '}';
    }
}

class Star{
    int brightness;

    public Star(int brightness) {
        this.brightness = brightness;
    }

    @Override
    public String
    toString() {
        return "Star{" +
                "brightness=" + brightness +
                '}';
    }

}
