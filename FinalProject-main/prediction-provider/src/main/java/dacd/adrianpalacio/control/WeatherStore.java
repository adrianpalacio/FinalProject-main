package dacd.adrianpalacio.control;

import dacd.adrianpalacio.control.exceptions.StoreException;
import dacd.adrianpalacio.model.Weather;

import java.util.List;

public interface WeatherStore {
    void save (List<Weather> weathers) throws StoreException;
}
