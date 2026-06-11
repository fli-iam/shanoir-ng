package org.shanoir.uploader.dicom.dto;

public class ConfigDTO {

    private DicomServerDTO distantDicomServer;
    
    private DicomServerDTO localDicomServer;

    public ConfigDTO(String distantHost, Integer distantPort, String distantAet,
                     String localHost, Integer localPort, String localAet) {
        this.distantDicomServer = new DicomServerDTO(distantHost, distantPort, distantAet);
        this.localDicomServer   = new DicomServerDTO(localHost, localPort, localAet);
    }

    public DicomServerDTO getDistantDicomServer() { return distantDicomServer; }
    public DicomServerDTO getLocalDicomServer()   { return localDicomServer; }

    public static class DicomServerDTO {
        private String host;
        private Integer port;
        private String aet;

        public DicomServerDTO(String host, Integer port, String aet) {
            this.host = host;
            this.port = port;
            this.aet  = aet;
        }

        public String getHost() { return host; }
        public Integer getPort() { return port; }
        public String getAet() { return aet; }

        public void setHost(String host) { this.host = host; }
        public void setPort(Integer port) { this.port = port; }
        public void setAet(String aet) { this.aet = aet; }
    }
}