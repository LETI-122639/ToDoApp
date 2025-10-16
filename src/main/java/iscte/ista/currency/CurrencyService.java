package iscte.ista.currency;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@Service
public class CurrencyService {

    private static final String ECB_URL =
            "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";

    private static final MathContext MC = new MathContext(12);

    private final CurrencyRepository repo;
    private final HttpClient http = HttpClient.newHttpClient();

    public CurrencyService(CurrencyRepository repo) {
        this.repo = repo;
    }

    /** Converte amount na moeda 'from' para 'to' usando taxas baseadas em EUR. */
    public BigDecimal convert(BigDecimal amount, String from, String to) {
        Map<String, BigDecimal> rates = getRatesMap();
        BigDecimal rFrom = rates.get(from);
        BigDecimal rTo = rates.get(to);
        if (rFrom == null || rTo == null) {
            throw new IllegalArgumentException("Moeda inválida: " + from + " ou " + to);
        }
        // taxas são EUR-based: amount * (to / from)
        return amount.multiply(rTo, MC).divide(rFrom, MC);
    }

    /** Todas as moedas/taxas atuais (ordenadas pelo código). */
    public List<Currency> listAll() {
        List<Currency> list = new ArrayList<>();
        repo.findAll().forEach(list::add);
        list.sort(Comparator.comparing(Currency::getCode));
        return list;
    }

    /** Atualização manual (botão na UI chama isto). */
    public void refreshNow() {
        Map<String, BigDecimal> rates = fetchEcbRates();
        Instant now = Instant.now();
        // garante EUR=1
        rates.put("EUR", BigDecimal.ONE);
        for (Map.Entry<String, BigDecimal> e : rates.entrySet()) {
            Currency c = new Currency(e.getKey(), e.getValue(), now);
            repo.save(c);
        }
    }

    /** Atualiza de hora a hora. Ajusta o cron se quiseres. */
    @Scheduled(cron = "0 0 * * * *")
    public void scheduledRefresh() {
        try { refreshNow(); } catch (Exception ignored) {}
    }

    // ---------- helpers ----------

    private Map<String, BigDecimal> getRatesMap() {
        Map<String, BigDecimal> map = new HashMap<>();
        repo.findAll().forEach(c -> map.put(c.getCode(), c.getRateToEur()));
        if (map.isEmpty()) {
            refreshNow();
            repo.findAll().forEach(c -> map.put(c.getCode(), c.getRateToEur()));
        }
        return map;
    }

    private Map<String, BigDecimal> fetchEcbRates() {
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(ECB_URL)).GET().build();
            HttpResponse<byte[]> res = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
            String xml = new String(res.body(), StandardCharsets.UTF_8);

            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

            NodeList cubes = doc.getElementsByTagName("Cube");
            Map<String, BigDecimal> m = new HashMap<>();
            for (int i = 0; i < cubes.getLength(); i++) {
                Node n = cubes.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) n;
                    if (e.hasAttribute("currency") && e.hasAttribute("rate")) {
                        String cur = e.getAttribute("currency").toUpperCase(Locale.ROOT);
                        BigDecimal rate = new BigDecimal(e.getAttribute("rate"));
                        m.put(cur, rate);
                    }
                }
            }
            return m;
        } catch (Exception ex) {
            throw new RuntimeException("Falha a obter taxas do ECB", ex);
        }
    }
}