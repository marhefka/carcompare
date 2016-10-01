package org.marhefka.carcompare.carinfo;

import org.marhefka.carcompare.VisitorResult;
import org.marhefka.carcompare.url.Url;

import java.util.ArrayList;
import java.util.List;

public class CarInfoResult implements VisitorResult<CarInfo> {
    public List<Url> urlsToVisit = new ArrayList<>();
    public CarInfo data;

    @Override
    public List<Url> getUrlsToVisit() {
        return urlsToVisit;
    }

    @Override
    public CarInfo getData() {
        return data;
    }
}
