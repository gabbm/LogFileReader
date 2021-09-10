DROP SCHEMA PUBLIC CASCADE;
CREATE TABLE EVENT (
    ID BIGINT identity primary key,
    EVENTID varchar(255) not null,
    DURATION BIGINT not null,
    TYPE varchar(255),
    HOST varchar(255),
    ALERT BOOLEAN not null
);
CREATE UNIQUE INDEX IXU_EVENTID ON EVENT (EVENTID);