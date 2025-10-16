package iscte.ista.pdf_printer;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfPrinterService {

    public enum DueFilter {
        ALL,           // todos
        NO_DUE_DATE,   // sem due date
        OVERDUE,       // vencidos (due < hoje)
        DUE_TODAY,     // due == hoje
        UPCOMING       // due > hoje
    }

    private final PdfPrinterRepository pdfPrinterRepository;

    PdfPrinterService(PdfPrinterRepository pdfPrinterRepository) {
        this.pdfPrinterRepository = pdfPrinterRepository;
    }

    @Transactional
    public void createPdfPrinter(String name, LocalDate dueDate) {
        var printer = new PdfPrinter(name, Instant.now(), dueDate);
        pdfPrinterRepository.saveAndFlush(printer);
    }

    // ===== NOVO: Specifications =====
    private Specification<PdfPrinter> buildSpec(@Nullable String nameQuery, DueFilter filter) {
        return (root, cq, cb) -> {
            List<Predicate> preds = new ArrayList<>();

            if (nameQuery != null && !nameQuery.isBlank()) {
                String like = "%" + nameQuery.trim().toLowerCase() + "%";
                preds.add(cb.like(cb.lower(root.get("name")), like));
            }

            LocalDate today = LocalDate.now();

            switch (filter) {
                case NO_DUE_DATE -> preds.add(cb.isNull(root.get("dueDate")));
                case OVERDUE     -> preds.add(cb.lessThan(root.get("dueDate"), today));
                case DUE_TODAY   -> preds.add(cb.equal(root.get("dueDate"), today));
                case UPCOMING    -> preds.add(cb.greaterThan(root.get("dueDate"), today));
                case ALL         -> { /* sem restrição */ }
            }

            return cb.and(preds.toArray(new Predicate[0]));
        };
    }

    // ===== NOVO: list + count com filtro =====
    @Transactional(readOnly = true)
    public Page<PdfPrinter> list(String nameQuery, DueFilter filter, Pageable pageable) {
        return pdfPrinterRepository.findAll(buildSpec(nameQuery, filter), pageable);
    }

    @Transactional(readOnly = true)
    public long count(String nameQuery, DueFilter filter) {
        return pdfPrinterRepository.count(buildSpec(nameQuery, filter));
    }

    // Mantém o método antigo se quiseres compatibilidade:
    @Transactional(readOnly = true)
    public List<PdfPrinter> list(Pageable pageable) {
        return pdfPrinterRepository.findAllBy(pageable).toList();
    }
}
