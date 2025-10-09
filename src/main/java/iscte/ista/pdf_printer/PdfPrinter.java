package iscte.ista.pdf_printer;

import jakarta.persistence.*;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.time.Instant;

@Entity
@Table(name = "pdf_printer")
public class PdfPrinter {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "pdf_printer_id")
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name = "";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "due_date", nullable = true) // Campo para a data de vencimento
    private LocalDate dueDate;

    protected PdfPrinter() { // Para o Hibernate
    }

    public PdfPrinter(String name, Instant createdAt, LocalDate dueDate) {
        setName(name);
        this.createdAt = createdAt;
        this.dueDate = dueDate;
    }

    public @Nullable Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name.length() > 100) {
            throw new IllegalArgumentException("Nome excede 100 caracteres");
        }
        this.name = name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().isAssignableFrom(obj.getClass())) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        PdfPrinter other = (PdfPrinter) obj;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
