package com.edmunds.vinspy.nutch.indexer;

public final class MongoIndexerConstants {
    public static final String MONGODB_URL = "mongodb.url";
    public static final String MONGODB_DB = "mongodb.db";
    public static final String MONGODB_COLLECTION = "mongodb.collection";
    public static final String META_VINSPY_VIN = "meta_vinspy-vin";
    public static final String META_VINSPY_TITLE = "meta_vinspy-title";

    public static final String LOCATIONS = "locations";
    public static final String JOBS = "jobs";
    public static final String ATTRIBUTES = "attributes";
    public static final String STATUS = "status";
    public static final String LOCATION_ID = "locationId";
    public static final String URL = "url";
    public static final String SITE_ID = "siteId";
    public static final String SITE_URL = "siteUrl";
    public static final String JOB_ID = "jobId";
    public static final String TITLE = "title";
    public static final String VIN = "vin";
    public static final String ID = "_id";
    public static final String CREATED = "created";

    public static final String VINSPY_LOCATION_ID = "vinspy.job.locationId";
    public static final String VINSPY_SITE_ID = "vinspy.job.siteId";
    public static final String VINSPY_SITE_URl = "vinspy.job.siteUrl";
    public static final String VINSPY_JOB_ID = "vinspy.job.id";

    public static final String $SET = "$set";
    public static final String $PUSH = "$push";

    private MongoIndexerConstants() {}
}