package io.roadmap.filestorage.components;

import io.roadmap.filestorage.dto.ResourceTypes;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class PathResolver {

    public record PathData(
            Boolean isDirectory,
            String[] parts,
            String resourceName,
            String resourcePath,
            ResourceTypes resourceType,
            String originalPath

    ) {
    }

    private Boolean isDirectory(String path) {
        return (path.charAt(path.length() - 1)) == '/';
    }

    private String[] getParts(String path) {
        return path.split("/");
    }

    private String getResourceName(String path) {
        String[] parts = getParts(path);
        return parts[parts.length - 1];
    }

    private ResourceTypes getResourceType(String path) {
        return isDirectory(path) ? ResourceTypes.DIRECTORY : ResourceTypes.FILE;

    }

    private String getResourcePath(String path) {
        String[] parts = getParts(path);

        String[] paths = Arrays.copyOf(parts, parts.length - 1);

        return parts.length > 1 ? String.join("/", paths) + "/" : "/";
    }

    public PathData getPathData(String path) {
        return new PathData(
                isDirectory(path),
                getParts(path),
                getResourceName(path),
                getResourcePath(path),
                getResourceType(path),
                path
        );
    }
}
