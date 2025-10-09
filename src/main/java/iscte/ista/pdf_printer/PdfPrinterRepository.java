package iscte.ista.pdf_printer;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface PdfPrinterRepository extends JpaRepository<PdfPrinter, Long>, JpaSpecificationExecutor<PdfPrinter> {

    Slice<PdfPrinter> findAllBy(Pageable pageable);
}
