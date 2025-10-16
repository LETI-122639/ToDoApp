package iscte.ista.pdf_printer;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Repositório Spring Data JPA para a entidade {@link PdfPrinter}.
 * Permite operações CRUD, paginação e Specifications.
 */
interface PdfPrinterRepository extends JpaRepository<PdfPrinter, Long>, JpaSpecificationExecutor<PdfPrinter> {

    /**
     * Lista "simples" paginada (sem filtros). Mantida por compatibilidade.
     */
    Slice<PdfPrinter> findAllBy(Pageable pageable);
}
