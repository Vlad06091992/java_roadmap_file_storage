package io.roadmap.filestorage.dtos.interfaces;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.roadmap.filestorage.dtos.ResourceTypes;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ресурс в хранилище — файл или папка. Поле `size` присутствует только у файлов.")
public interface GetResourceData {

    @Schema(description = "Путь к ресурсу относительно корня пользователя", example = "docs/")
    String path();

    @Schema(description = "Имя ресурса (у папки оканчивается на `/`)", example = "report.pdf")
    String name();

    @Schema(description = "Размер файла в байтах; null для папки", example = "10240", nullable = true)
    default Long size() {
        return null;
    }

    @JsonProperty("type")
    ResourceTypes type();
}
