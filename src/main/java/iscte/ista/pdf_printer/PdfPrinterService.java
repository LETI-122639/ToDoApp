package iscte.ista.pdf_printer;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
public class PdfPrinterService {

    private final PdfPrinterRepository pdfPrinterRepository;

    PdfPrinterService(PdfPrinterRepository pdfPrinterRepository) {
        this.pdfPrinterRepository = pdfPrinterRepository;
    }

    // Modificado para aceitar a data de vencimento
    @Transactional
    public void createPdfPrinter(String name, LocalDate dueDate) {
        var printer = new PdfPrinter(name, Instant.now(), dueDate);  // Passando a dueDate para o construtor
        pdfPrinterRepository.saveAndFlush(printer);
    }

    @Transactional(readOnly = true)
    public List<PdfPrinter> list(Pageable pageable) {
        return pdfPrinterRepository.findAllBy(pageable).toList();
    }
}
