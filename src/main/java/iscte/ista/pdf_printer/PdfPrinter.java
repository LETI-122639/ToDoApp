package iscte.ista.pdf_printer;

import jakarta.persistence.*;
import org.jspecify.annotations.Nullable;

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
    private java.time.Instant createdAt;

    protected PdfPrinter() { // Para o Hibernate
    }

    public PdfPrinter(String name, java.time.Instant createdAt) {
        setName(name);
        this.createdAt = createdAt;
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

    public java.time.Instant getCreatedAt() {
        return createdAt;
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
