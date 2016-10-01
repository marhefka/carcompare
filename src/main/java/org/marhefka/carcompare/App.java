package org.marhefka.carcompare;

import com.mongodb.MongoClient;
import org.marhefka.carcompare.carinfo.CarInfoVisitor;
import org.marhefka.carcompare.carlist.CarListVisitor;
import org.marhefka.carcompare.url.QueuedUrl;
import org.marhefka.carcompare.url.Url;
import org.marhefka.carcompare.url.UrlFetchState;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class App {
    public final static DateTimeFormatter fmt = DateTimeFormatter.ISO_DATE_TIME;

    public static void main(String[] args) {
        Logger.getLogger("org.mongodb.driver").setLevel(Level.SEVERE);
        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);

        MongoClient mongoClient = new MongoClient("localhost", 27017);
        mongoClient.dropDatabase("cars");

        Morphia morphia = new Morphia();
        morphia.getMapper().getOptions().setStoreNulls(true);
        morphia.mapPackage("org.marhefka.carcompare");

        Datastore datastore = morphia.createDatastore(mongoClient, "cars");

//        Url url = new Url();
//        url.url = "http://www.autoscout24.ch/de/d/skoda-octavia-kombi-2010-occasion?index=1638&make=71&model=496&page=41&vehid=4128574&returnurl=%2fde%2fauto-modelle%2fskoda--octavia%3fpage%3d41%26r%3d9";
//        url.type = CarInfoVisitor.class.getName();

        Url url = new Url();
        url.url = "http://www.autoscout24.ch/de/auto-modelle/skoda--octavia";
        url.type = CarListVisitor.class.getName();

        App app = new App();
        app.queueUrl(datastore, url);
        app.fetchUrls(1, datastore);
    }

    private void queueUrl(Datastore datastore, Url url) {
        QueuedUrl queuedUrl = new QueuedUrl();

        queuedUrl.url = url;
        queuedUrl.state = UrlFetchState.QUEUED;
        queuedUrl.created = fmt.format(LocalDateTime.now());

        datastore.save(queuedUrl);
    }

    private void fetchUrls(int workers, Datastore datastore) {
        AtomicInteger atomicFreeWorkers = new AtomicInteger(workers);

        while (true) {
            boolean finished = startNewWorkers(datastore, atomicFreeWorkers);
            if (finished) {
                return;
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private boolean startNewWorkers(Datastore datastore, AtomicInteger atomicFreeWorkers) {
        Query<QueuedUrl> query = datastore.createQuery(QueuedUrl.class);

        int freeWorkersCounts = atomicFreeWorkers.get();
        if (freeWorkersCounts == 0) {
            return false;
        }

        List<QueuedUrl> queuedUrls = query
                .filter("state", UrlFetchState.QUEUED)
                .order("created")
                .limit(freeWorkersCounts)
                .asList();

        while (queuedUrls.size() > 0) {
            atomicFreeWorkers.decrementAndGet();

            QueuedUrl queuedUrl = queuedUrls.remove(0);
            queuedUrl.state = UrlFetchState.BEING_VISITED;
            datastore.save(queuedUrl);

            new Thread(() -> {
                try {
                    visitUrl(queuedUrl, datastore);
                    queuedUrl.state = UrlFetchState.VISITED;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    queuedUrl.errorCause = getStackTrace(ex);
                    queuedUrl.state = UrlFetchState.ERROR;
                }

                System.out.println(queuedUrl.state + ": " + queuedUrl.url.url);
                datastore.save(queuedUrl);
                atomicFreeWorkers.incrementAndGet();
            }).start();
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    private void visitUrl(QueuedUrl queuedUrl, Datastore datastore) throws Exception {
        Class<? extends UrlVisitor> visitorClass = (Class<? extends UrlVisitor>) Class.forName(queuedUrl.url.type);
        UrlVisitor urlVisitor = visitorClass.newInstance();

        VisitorResult result = urlVisitor.visit(queuedUrl.url.url);
        if (result == null) {
            return;
        }

        Object data = result.getData();
        if (data != null) {
            datastore.save(data);
        }

        result.getUrlsToVisit().forEach(nextUrl -> queueUrl(datastore, (Url) nextUrl));
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    private String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

}
