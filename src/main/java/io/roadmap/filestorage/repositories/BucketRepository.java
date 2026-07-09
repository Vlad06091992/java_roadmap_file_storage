package io.roadmap.filestorage.repositories;

import io.roadmap.filestorage.entity.Bucket;
import io.roadmap.filestorage.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BucketRepository extends JpaRepository<Bucket, UUID> { }
