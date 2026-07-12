package io.roadmap.filestorage.dtos.interfaces;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.roadmap.filestorage.dtos.ResourceTypes;

public interface GetResourceData {
    String path();

    String name();

    default Long size() {
        return null;
    }

    @JsonProperty("type")
    ResourceTypes type();
}
