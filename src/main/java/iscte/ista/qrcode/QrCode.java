package iscte.ista.qrcode;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class QrCode {
    //gg
    @Id
    private Long id;

    private String content;

    public QrCode() {}

    public QrCode(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}