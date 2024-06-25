package dacd.adrianpalacio.control;

import dacd.adrianpalacio.model.Hotel;

import java.util.List;

public interface HotelSupplier {
    List<Hotel> get(String place);
}
