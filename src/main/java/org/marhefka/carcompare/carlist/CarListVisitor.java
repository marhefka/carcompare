package org.marhefka.carcompare.carlist;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.common.base.Strings;
import org.marhefka.carcompare.UrlVisitor;
import org.marhefka.carcompare.carinfo.CarInfoVisitor;
import org.marhefka.carcompare.url.Url;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CarListVisitor implements UrlVisitor<CarListResult> {
    @Override
    public CarListResult visit(String url) throws Exception {
        CarListResult result = new CarListResult();

        try (WebClient webClient = new WebClient(BrowserVersion.CHROME)) {
            webClient.getOptions().setCssEnabled(false);

            HtmlPage page = webClient.getPage(url);

            addNextPageUrl(result, page);
            addCarInfoUrls(result, page);

            return result;
        }
    }

    private void addCarInfoUrls(CarListResult result, HtmlPage page) {
        List<HtmlAnchor> carList = (List<HtmlAnchor>) page.getByXPath("//a[@class='primary-link']");

        Stream<Url> stream = carList.stream().map(htmlAnchor -> {
            Url item = new Url();
            item.url = "http://www.autoscout24.ch" + htmlAnchor.getHrefAttribute();
            item.type = CarInfoVisitor.class.getName();
            return item;
        });

        result.urlsToVisit.addAll(stream.collect(Collectors.toList()));
    }

    private void addNextPageUrl(CarListResult result, HtmlPage page) {
        List<HtmlAnchor> next = (List<HtmlAnchor>) page.getByXPath("//a[contains(@class, 'next')]");
        if (next.size() != 1) {
            throw new RuntimeException("???");
        }

        String href = next.get(0).getHrefAttribute();
        if (!Strings.isNullOrEmpty(href)) {
            Url nextPageUrl = new Url();
            nextPageUrl.url = "http://www.autoscout24.ch" + href;
            nextPageUrl.type = CarListVisitor.class.getName();
            result.urlsToVisit.add(nextPageUrl);
        }
    }

}
