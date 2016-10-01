package org.marhefka.carcompare.url;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import java.time.LocalDateTime;

@Entity(noClassnameStored = true)
public class QueuedUrl {
    @Id
    public ObjectId id;

    public Url url;
    public UrlFetchState state;
    public String created;
    public String errorCause;
}
