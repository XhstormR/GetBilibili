package Bean;

import java.util.Arrays;

public class Url {
    private int order;
    private int length;
    private int size;
    private String url;
    private String[] backup_url;

    @Override
    public String toString() {
        return "Url{" +
                "order=" + order +
                ", length=" + length +
                ", size=" + size +
                ", url='" + url + '\'' +
                ", backup_url=" + Arrays.toString(backup_url) +
                '}';
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String[] getBackup_url() {
        return backup_url;
    }

    public void setBackup_url(String[] backup_url) {
        this.backup_url = backup_url;
    }
}
