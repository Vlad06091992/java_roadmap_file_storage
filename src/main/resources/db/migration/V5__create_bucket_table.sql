CREATE TABLE buckets
(
    id      UUID NOT NULL,
    user_id UUID,
    name    VARCHAR(255),
    CONSTRAINT pk_buckets PRIMARY KEY (id)
);

ALTER TABLE buckets
    ADD CONSTRAINT uc_buckets_user UNIQUE (user_id);

ALTER TABLE buckets
    ADD CONSTRAINT FK_BUCKETS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);