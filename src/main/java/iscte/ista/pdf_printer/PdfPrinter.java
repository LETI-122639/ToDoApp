package iscte.ista.pdf_printer;

import jakarta.persistence.*;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDate;

@SuppressWarnings("all")
/**
 * Entidade que representa uma impressora PDF (registo).
 * Armazena nome, data de criação, data de vencimento e status.
 */
@Entity
@Table(name = "pdf_printer")
public class PdfPrinter {

    public enum Status {
        PENDING, PRINTED, SENT, CANCELED
    }

    /** Identificador único da impressora PDF. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "pdf_printer_id")
    private Long id;

    /** Nome da impressora PDF. */
    @Column(name = "name", nullable = false, length = 100)
    private String name = "";

    /** Data e hora de criação do registro. */
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /** Data de vencimento definida pelo usuário. */
    @Column(name = "due_date")
    private LocalDate dueDate;

    /** Estado do registo. */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status = Status.PENDING;

    /** Construtor protegido para uso do Hibernate. */
    protected PdfPrinter() { }

    /**
     * Construtor principal.
     * @param name Nome da impressora
     * @param createdAt Data de criação
     * @param dueDate Data de vencimento
     */
    public PdfPrinter(String name, Instant createdAt, LocalDate dueDate) {
        setName(name);
        this.createdAt = createdAt;
        this.dueDate = dueDate;
        this.status = Status.PENDING;
    }

    /** PrePersist para garantir campos base. */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        if (status == null) status = Status.PENDING;
    }

    /** Retorna o ID da impressora PDF. */
    public @Nullable Long getId() { return id; }

    /** Retorna o nome da impressora PDF. */
    public String getName() { return name; }

    /**
     * Define o nome da impressora PDF.
     * @param name Nome
     */
    public void setName(String name) {
        if (name == null) name = "";
        if (name.length() > 100) {
            throw new IllegalArgumentException("Nome excede 100 caracteres");
        }
        this.name = name;
    }

    /** Retorna a data de criação. */
    public Instant getCreatedAt() { return createdAt; }

    /** Retorna a data de vencimento. */
    public LocalDate getDueDate() { return dueDate; }

    /** Define a data de vencimento. */
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    /** Retorna o status. */
    public Status getStatus() { return status; }

    /** Define o status. */
    public void setStatus(Status status) { this.status = (status == null ? Status.PENDING : status); }

    /** Igualdade por ID. */
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().isAssignableFrom(obj.getClass())) return false;
        if (obj == this) return true;
        PdfPrinter other = (PdfPrinter) obj;
        return getId() != null && getId().equals(other.getId());
    }

    /** HashCode por classe (Hibernate-safe). */
    @Override
    public int hashCode() { return getClass().hashCode(); }
}
