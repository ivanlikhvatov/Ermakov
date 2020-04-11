package ermarov.ermakovHomeWorks.dependencyInjection;

import java.util.List;

public class SolarConfig {
    @Prototype
    @Bean
    public SolarSystem sSys(List<Planet> plans, Star star){
        SolarSystem sSystem = new SolarSystem();
        sSystem.star = star;
        sSystem.planets = plans;
        return sSystem;
    }
}
