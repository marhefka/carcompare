package org.marhefka.carcompare.carinfo;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.marhefka.carcompare.UrlVisitor;

public class CarInfoVisitor implements UrlVisitor<CarInfoResult> {
    @Override
    public CarInfoResult visit(String url) throws Exception {
        String vehicleId = getVehicleId(url);

        Document document = Jsoup.connect(url).get();
        Elements elements = document.select("div.column.first > div > ul > li > div");

        if (elements.size() == 0) {
            throw new RuntimeException("yyy: " + url);
        }

        if (elements.size() % 2 == 1) {
            throw new RuntimeException("zzz");
        }

        CarInfoResult result = new CarInfoResult();

        result.data = new CarInfo();
        result.data.vehicleId = vehicleId;
        result.data.url = url;

        for (int i = 0; i < elements.size() / 2; i++) {
            String prop = elements.get(i * 2).text().trim();
            String value = elements.get(i * 2 + 1).text().trim();

            if (prop.equalsIgnoreCase("Inverkehrsetzung")) {
                setStartDate(result.data, value);
            }

            if (prop.equalsIgnoreCase("Kilometer")) {
                setKm(result.data, value);
            }

            if (prop.equalsIgnoreCase("Preis")) {
                setPrice(result.data, value);
            }
        }

        return result;
    }

    private void setStartDate(CarInfo carInfo, String value) {
        if (value.equalsIgnoreCase("Neu")) {
            carInfo.newCar = true;
            return;
        }
        carInfo.newCar = false;

        String[] split = value.split("\\.");
        if (split.length != 2) {
            throw new RuntimeException("invalid start date: " + value);
        }

        Integer month = Integer.valueOf(split[0]);
        Integer year = Integer.valueOf(split[1]);

        if (month < 1 || month > 12) {
            throw new RuntimeException("invalid start date: " + value);
        }

        if (year < 1900 || year > 2016) {
            throw new RuntimeException("invalid start date: " + value);
        }

        carInfo.firstYear = year;
        carInfo.firstMonth = month;
    }

    private void setKm(CarInfo carInfo, String value) {
        if (!value.endsWith(" km")) {
            throw new RuntimeException("Invalid km: " + value);
        }

        String sKm = value.replace("'", "").replace(" km", "");
        int km = Integer.parseInt(sKm);
        carInfo.km = km;
    }

    private void setPrice(CarInfo carInfo, String value) {
        if (!value.startsWith("CHF ")) {
            throw new RuntimeException("Invalid price: " + value);
        }

        if (!value.endsWith(".-")) {
            throw new RuntimeException("Invalid price: " + value);
        }

        String sPrice = value.replace("CHF ", "").replace("'", "").replace(".-", "");
        int price = Integer.valueOf(sPrice);
        carInfo.price = price;
    }

    private String getVehicleId(String url) throws Exception {
        URIBuilder uriBuilder = new URIBuilder(url);

        for (NameValuePair pair : uriBuilder.getQueryParams()) {
            if (pair.getName().equalsIgnoreCase("vehid")) {
                return pair.getValue();
            }
        }

        throw new RuntimeException("Vehid not found: " + url);
    }
}
