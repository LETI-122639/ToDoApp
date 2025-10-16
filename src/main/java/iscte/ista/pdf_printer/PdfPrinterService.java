package iscte.ista.pdf_printer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        OVERDUE,       // due < hoje
        DUE_TODAY,     // due == hoje
        UPCOMING       // due > hoje
    }

    private final PdfPrinterRepository pdfPrinterRepository;

    PdfPrinterService(PdfPrinterRepository pdfPrinterRepository) {
        this.pdfPrinterRepository = pdfPrinterRepository;
    }

    /** Cria um novo registo com due date e status por defeito (PENDING). */
    @Transactional
    public void createPdfPrinter(String name, LocalDate dueDate) {
        createPdfPrinter(name, dueDate, PdfPrinter.Status.PENDING);
    }

    /** Cria um novo registo com due date e status fornecido. */
    @Transactional
    public void createPdfPrinter(String name, LocalDate dueDate, PdfPrinter.Status status) {
        var printer = new PdfPrinter(name, Instant.now(), dueDate);
        printer.setStatus(status == null ? PdfPrinter.Status.PENDING : status);
        pdfPrinterRepository.saveAndFlush(printer);
    }

    /** Atualiza o status de um registo. */
    @Transactional
    public PdfPrinter updateStatus(Long id, PdfPrinter.Status status) {
        var printer = pdfPrinterRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("PdfPrinter not found: " + id));
        printer.setStatus(status == null ? PdfPrinter.Status.PENDING : status);
        return pdfPrinterRepository.save(printer);
    }

    // ===== Specifications para pesquisa e filtros =====
    private Specification<PdfPrinter> buildSpec(String nameQuery, DueFilter filter, PdfPrinter.Status statusFilter) {
        return (root, cq, cb) -> {
            List<Predicate> preds = new ArrayList<>();

            if (nameQuery != null && !nameQuery.isBlank()) {
                String like = "%" + nameQuery.trim().toLowerCase() + "%";
                preds.add(cb.like(cb.lower(root.get("name")), like));
            }

            // Filtro por Status (se definido)
            if (statusFilter != null) {
                preds.add(cb.equal(root.get("status"), statusFilter));
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

    /** Lista paginada e ordenada com pesquisa/filtros. */
    @Transactional(readOnly = true)
    public Page<PdfPrinter> list(String nameQuery, DueFilter filter, PdfPrinter.Status statusFilter, Pageable pageable) {
        return pdfPrinterRepository.findAll(buildSpec(nameQuery, filter, statusFilter), pageable);
    }

    /** Contagem total com pesquisa/filtros. */
    @Transactional(readOnly = true)
    public long count(String nameQuery, DueFilter filter, PdfPrinter.Status statusFilter) {
        return pdfPrinterRepository.count(buildSpec(nameQuery, filter, statusFilter));
    }
}
