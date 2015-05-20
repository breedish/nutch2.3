package com.edmunds.vinspy.nutch.indexer;

import com.edmunds.vinspy.Status;
import com.google.common.collect.ImmutableMap;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.indexer.IndexWriter;
import org.apache.nutch.indexer.NutchDocument;
import org.bson.Document;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.Map;

import static com.edmunds.vinspy.nutch.indexer.MongoIndexerConstants.$PUSH;
import static com.edmunds.vinspy.nutch.indexer.MongoIndexerConstants.$SET;
import static com.edmunds.vinspy.nutch.indexer.MongoIndexerConstants.ATTRIBUTES;
import static com.edmunds.vinspy.nutch.indexer.MongoIndexerConstants.CREATED;
import static com.edmunds.vinspy.nutch.indexer.MongoIndexerConstants.ID;
import static com.edmunds.vinspy.nutch.indexer.MongoIndexerConstants.JOBS;
import static com.edmunds.vinspy.nutch.indexer.MongoIndexerConstants.JOB_ID;
import static com.edmunds.vinspy.nutch.indexer.MongoIndexerConstants.LOCATIONS;
import static com.edmunds.vinspy.nutch.indexer.MongoIndexerConstants.LOCATION_ID;
import static com.edmunds.vinspy.nutch.indexer.MongoIndexerConstants.META_VINSPY_TITLE;
import static com.edmunds.vinspy.nutch.indexer.MongoIndexerConstants.META_VINSPY_VIN;
import static com.edmunds.vinspy.nutch.indexer.MongoIndexerConstants.SITE_ID;
import static com.edmunds.vinspy.nutch.indexer.MongoIndexerConstants.SITE_URL;
import static com.edmunds.vinspy.nutch.indexer.MongoIndexerConstants.STATUS;
import static com.edmunds.vinspy.nutch.indexer.MongoIndexerConstants.TITLE;
import static com.edmunds.vinspy.nutch.indexer.MongoIndexerConstants.URL;
import static com.edmunds.vinspy.nutch.indexer.MongoIndexerConstants.VIN;
import static com.edmunds.vinspy.nutch.indexer.MongoIndexerConstants.VINSPY_JOB_ID;
import static com.edmunds.vinspy.nutch.indexer.MongoIndexerConstants.VINSPY_LOCATION_ID;
import static com.edmunds.vinspy.nutch.indexer.MongoIndexerConstants.VINSPY_SITE_ID;
import static com.edmunds.vinspy.nutch.indexer.MongoIndexerConstants.VINSPY_SITE_URl;

/**
 * @author alina on 6.5.15.
 */
public class MongoIndexWriter implements IndexWriter {

    private MongoClient mongoClient;

    private Configuration conf;

    private MongoCollection<Document> collection;

    @Override
    public void open(Configuration conf) throws IOException {
        String mongoDB = conf.get(MongoIndexerConstants.MONGODB_DB);
        String mongodbCollections = conf.get(MongoIndexerConstants.MONGODB_COLLECTION);
        mongoClient = new MongoClient(new MongoClientURI(conf.get(MongoIndexerConstants.MONGODB_URL)));
        collection = mongoClient.getDatabase(mongoDB).getCollection(mongodbCollections);
    }

    @Override
    public void write(NutchDocument doc) throws IOException {
        if (StringUtils.isNotEmpty(doc.getFieldValue(META_VINSPY_VIN))) {
            collection.updateOne(new Document(ID, doc.getFieldValue(META_VINSPY_VIN)),
                    new Document($SET, createStats(doc)).append($PUSH, locations(doc).append(JOBS, jobInfo())), new UpdateOptions().upsert(Boolean.TRUE));
        }
    }

    private Document createStats(NutchDocument doc) {
        return new Document(ID, doc.getFieldValue(META_VINSPY_VIN))
                .append(ATTRIBUTES, extractedAttributes(doc))
                .append(STATUS, parseStatus(doc).name());
    }

    private Document locations(NutchDocument doc) {
        return new Document(LOCATIONS, location(doc));
    }

    private Document location(NutchDocument doc) {
        return new Document(LOCATION_ID, conf.get(VINSPY_LOCATION_ID))
                .append(URL, doc.getFieldValue(URL))
                .append(SITE_ID, conf.get(VINSPY_SITE_ID))
                .append(SITE_URL, conf.get(VINSPY_SITE_URl));
    }

    private Document jobInfo() {
        return new Document(JOB_ID, conf.get(VINSPY_JOB_ID)).append(CREATED, DateTime.now().getMillis());
    }

    private Status parseStatus(NutchDocument doc) {
        Status status = (StringUtils.isNotEmpty(doc.getFieldValue(META_VINSPY_TITLE))) ? Status.GREEN : Status.RED;
        if (!doc.getFieldValue(META_VINSPY_TITLE).equals(doc.getFieldValue(TITLE)) && StringUtils.isNotEmpty(doc.getFieldValue(META_VINSPY_TITLE))) {
            status = Status.YELLOW;
        }
        return status;
    }

    private Map<String, String> extractedAttributes(NutchDocument doc) {
        return ImmutableMap.<String, String>builder()
                .put(TITLE, doc.getFieldValue(META_VINSPY_TITLE))
                .put(VIN, doc.getFieldValue(META_VINSPY_VIN))
                .build();
    }

    @Override
    public void delete(String key) throws IOException {
        collection.deleteOne(new Document(ID, key));
    }
    public Document findOne(String key) {
        return collection.find(new Document(ID, key)).first();
    }

    @Override
    public void update(NutchDocument doc) throws IOException {
        write(doc);
    }

    @Override
    public void commit() throws IOException {
    }

    @Override
    public void close() throws IOException {
        mongoClient.close();
    }

    @Override
    public String describe() {
        return "vinspy-indexer";
    }

    @Override
    public void setConf(Configuration conf) {
        this.conf = conf;
    }

    @Override
    public Configuration getConf() {
        return this.conf;
    }
}
