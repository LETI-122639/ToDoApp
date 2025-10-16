package iscte.ista.qrcode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
//ggf
@Service
public class QrCodeService {

    @Autowired
    private QrCodeRepository qrCodeRepository;

    public String getQrCodeContent(Long id) {
        return qrCodeRepository.findById(id)
                .map(QrCode::getContent)
                .orElse("QR Code not found");
    }

    public QrCode saveQrCode(String content) {
        return qrCodeRepository.save(new QrCode(content));
    }
}