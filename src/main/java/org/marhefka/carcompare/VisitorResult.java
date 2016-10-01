package org.marhefka.carcompare;

import org.marhefka.carcompare.url.Url;

import java.util.List;

public interface VisitorResult<T> {
    List<Url> getUrlsToVisit();

    T getData();
}

