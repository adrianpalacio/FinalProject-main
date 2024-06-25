package dacd.adrianpalacio.control;

import dacd.adrianpalacio.model.Location;
import dacd.adrianpalacio.model.Weather;

import java.util.List;

public interface WeatherSupplier {
    List<Weather> get(Location location);
}
