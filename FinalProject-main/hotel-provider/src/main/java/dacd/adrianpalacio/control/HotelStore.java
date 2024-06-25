package dacd.adrianpalacio.control;

import dacd.adrianpalacio.model.Hotel;

import java.util.List;

public interface HotelStore {
    void save(List<Hotel> hotels);
}
