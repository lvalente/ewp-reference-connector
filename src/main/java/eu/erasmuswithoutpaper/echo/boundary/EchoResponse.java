package eu.erasmuswithoutpaper.echo.boundary;

import java.io.Serializable;
import java.util.List;

public class EchoResponse implements Serializable {
    private int statusCode;
    private String mediaType;
    private List<String> heiId;
    private List<String> echo;
    private String errorMessage;

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public List<String> getHeiId() {
        return heiId;
    }

    public void setHeiId(List<String> heiId) {
        this.heiId = heiId;
    }

    public List<String> getEcho() {
        return echo;
    }

    public void setEcho(List<String> echo) {
        this.echo = echo;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    
}
