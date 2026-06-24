package io.roadmap.filestorage.dto.interfaces;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.roadmap.filestorage.dto.ResourceTypes;

public interface GetResourceData {
    String path();
    String name();

    @JsonProperty("type")
    ResourceTypes type();
}
