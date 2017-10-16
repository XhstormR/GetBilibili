package Bean;

import java.util.Arrays;

public class Video {
    private String from;
    private String result;
    private String format;
    private int timelength;
    private String accept_format;
    private int[] accept_quality;
    private String seek_param;
    private String seek_type;
    private Url[] durl;

    @Override
    public String toString() {
        return "Video{" +
                "from='" + from + '\'' +
                ", result='" + result + '\'' +
                ", format='" + format + '\'' +
                ", timelength=" + timelength +
                ", accept_format='" + accept_format + '\'' +
                ", accept_quality=" + Arrays.toString(accept_quality) +
                ", seek_param='" + seek_param + '\'' +
                ", seek_type='" + seek_type + '\'' +
                ", durl=" + Arrays.toString(durl) +
                '}';
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public int getTimelength() {
        return timelength;
    }

    public void setTimelength(int timelength) {
        this.timelength = timelength;
    }

    public String getAccept_format() {
        return accept_format;
    }

    public void setAccept_format(String accept_format) {
        this.accept_format = accept_format;
    }

    public int[] getAccept_quality() {
        return accept_quality;
    }

    public void setAccept_quality(int[] accept_quality) {
        this.accept_quality = accept_quality;
    }

    public String getSeek_param() {
        return seek_param;
    }

    public void setSeek_param(String seek_param) {
        this.seek_param = seek_param;
    }

    public String getSeek_type() {
        return seek_type;
    }

    public void setSeek_type(String seek_type) {
        this.seek_type = seek_type;
    }

    public Url[] getDurl() {
        return durl;
    }

    public void setDurl(Url[] durl) {
        this.durl = durl;
    }
}
