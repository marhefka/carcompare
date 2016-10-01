package org.marhefka.carcompare.carinfo;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity(noClassnameStored = true)
public class CarInfo {
    @Id
    public String vehicleId;
    public String url;

    public boolean newCar;
    public Integer firstYear;
    public Integer firstMonth;

    public Integer km;
    public Integer price;
}
