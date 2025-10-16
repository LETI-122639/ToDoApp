package iscte.ista.pdf_printer;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Repositório Spring Data JPA para a entidade {@link PdfPrinter}.
 * Permite operações CRUD e consultas paginadas/especificadas.
 */
interface PdfPrinterRepository extends JpaRepository<PdfPrinter, Long>, JpaSpecificationExecutor<PdfPrinter> {
    Slice<PdfPrinter> findAllBy(Pageable pageable);
    // (novo) — não é necessário. Vamos usar o executor de Specifications diretamente no serviço.
}


