package org.marhefka.carcompare.carlist;

import org.marhefka.carcompare.VisitorResult;
import org.marhefka.carcompare.url.Url;

import java.util.ArrayList;
import java.util.List;

public class CarListResult implements VisitorResult<Object> {
    public List<Url> urlsToVisit = new ArrayList<>();

    @Override
    public List<Url> getUrlsToVisit() {
        return urlsToVisit;
    }

    @Override
    public Object getData() {
        return null;
    }
}
