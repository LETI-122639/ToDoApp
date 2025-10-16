package iscte.ista.currency;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "currency")
public class Currency {

    @Id
    @Column(length = 3, nullable = false, updatable = false)
    private String code;             // ex.: "EUR", "USD"

    @Column(nullable = false)
    private BigDecimal rateToEur;    // taxa vs EUR (EUR=1)

    @Column(nullable = false)
    private Instant updatedAt;

    protected Currency() {} // JPA

    public Currency(String code, BigDecimal rateToEur, Instant updatedAt) {
        this.code = code;
        this.rateToEur = rateToEur;
        this.updatedAt = updatedAt;
    }

    public String getCode() { return code; }
    public BigDecimal getRateToEur() { return rateToEur; }
    public void setRateToEur(BigDecimal v) { this.rateToEur = v; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant t) { this.updatedAt = t; }
}