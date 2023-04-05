package org.shanoir.ng.dataset.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class InputDTO {

    @JsonProperty("series")
    List<InputSerieDTO> series = new ArrayList<>();

    public List<InputSerieDTO> getSeries() {
        return series;
    }

    public void setSeries(List<InputSerieDTO> series) {
        this.series = series;
    }

    public static class InputSerieDTO {

        @JsonProperty("id")
        private Long id;

        @JsonProperty("files")
        private List<String> files = new ArrayList<>();

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public List<String> getFiles() {
            return files;
        }

        public void setFiles(List<String> files) {
            this.files = files;
        }
    }


}
