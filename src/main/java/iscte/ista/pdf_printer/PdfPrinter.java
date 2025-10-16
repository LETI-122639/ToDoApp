package iscte.ista.pdf_printer;

import jakarta.persistence.*;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.time.Instant;

@SuppressWarnings("all")

/**
 * Entidade que representa uma impressora PDF.
 * Armazena informações como nome, data de criação e data de vencimento.
 */
@Entity
@Table(name = "pdf_printer")
public class PdfPrinter {

    /**
     * Identificador único da impressora PDF.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "pdf_printer_id")
    private Long id;

    /**
     * Nome da impressora PDF.
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name = "";

    /**
     * Data e hora de criação do registro.
     */
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /**
     * Data de vencimento definida pelo usuário.
     */
    @Column(name = "due_date", nullable = true)
    private LocalDate dueDate;

    /**
     * Construtor protegido para uso do Hibernate.
     */
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
    }

    /**
     * Retorna o ID da impressora PDF.
     * @return ID
     */
    public @Nullable Long getId() {
        return id;
    }

    /**
     * Retorna o nome da impressora PDF.
     * @return nome
     */
    public String getName() {
        return name;
    }

    /**
     * Define o nome da impressora PDF.
     * @param name Nome
     * @throws IllegalArgumentException se o nome exceder 100 caracteres
     */
    public void setName(String name) {
        if (name.length() > 100) {
            throw new IllegalArgumentException("Nome excede 100 caracteres");
        }
        this.name = name;
    }

    /**
     * Retorna a data de criação.
     * @return data de criação
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Retorna a data de vencimento.
     * @return data de vencimento
     */
    public LocalDate getDueDate() {
        return dueDate;
    }

    /**
     * Define a data de vencimento.
     * @param dueDate Data de vencimento
     */
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    /**
     * Compara se dois objetos PdfPrinter são iguais pelo ID.
     * @param obj Objeto a comparar
     * @return true se forem iguais, false caso contrário
     */
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

    /**
     * Retorna o hash code da classe.
     * @return hash code
     */
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
