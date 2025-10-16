package iscte.ista.currency.ui;

import iscte.ista.base.ui.component.ViewToolbar;
import iscte.ista.currency.Currency;
import iscte.ista.currency.CurrencyService;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Route("currency")
@PageTitle("Currency")
@Menu(order = 20, icon = "vaadin:dollar", title = "Currency")
class CurrencyListView extends Main {

    private final CurrencyService currencyService;

    final NumberField amount;
    final Select<String> from;
    final Select<String> to;
    final Button convertBtn;
    final Button refreshBtn;
    final Grid<Currency> grid;

    CurrencyListView(CurrencyService currencyService) {
        this.currencyService = currencyService;

        amount = new NumberField();
        amount.setPlaceholder("Amount");
        amount.setAriaLabel("Amount");
        amount.setMin(0);
        amount.setStep(1);
        amount.setValue(100.0);

        from = new Select<>();
        from.setLabel("From");

        to = new Select<>();
        to.setLabel("To");

        convertBtn = new Button("Convert", e -> convert());
        convertBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        refreshBtn = new Button("Refresh rates", e -> {
            try {
                currencyService.refreshNow();
                loadRates();
                Notification.show("Rates updated", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification.show("Failed to update rates: " + ex.getMessage(), 4000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        var dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                .withLocale(getLocale()).withZone(ZoneId.systemDefault());

        grid = new Grid<>();
        grid.addColumn(Currency::getCode).setHeader("Code").setAutoWidth(true).setSortable(true);
        grid.addColumn(c -> formatNumber(c.getRateToEur())).setHeader("Rate vs EUR").setSortable(true);
        grid.addColumn(c -> dateTimeFormatter.format(c.getUpdatedAt())).setHeader("Updated").setAutoWidth(true);
        grid.setSizeFull();

        setSizeFull();
        addClassNames(
                LumoUtility.BoxSizing.BORDER,
                LumoUtility.Display.FLEX,
                LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Padding.MEDIUM,
                LumoUtility.Gap.SMALL
        );

        add(new ViewToolbar("Currency",
                ViewToolbar.group(from, to, amount, convertBtn, refreshBtn)));
        add(grid);

        loadRates();
    }

    private void loadRates() {
        List<Currency> list = currencyService.listAll();
        if (list.isEmpty()) {
            // primeira carga
            currencyService.refreshNow();
            list = currencyService.listAll();
        }
        grid.setItems(list);

        var codes = list.stream()
                .map(Currency::getCode)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());

        from.setItems(codes);
        to.setItems(codes);

        if (!codes.isEmpty()) {
            from.setValue(codes.contains("EUR") ? "EUR" : codes.get(0));
            to.setValue(codes.contains("USD") ? "USD" : codes.get(0));
        }
    }

    private void convert() {
        try {
            if (amount.getValue() == null) amount.setValue(0.0);
            BigDecimal in = BigDecimal.valueOf(amount.getValue());
            BigDecimal out = currencyService.convert(in, from.getValue(), to.getValue());

            String msg = formatNumber(in) + " " + from.getValue()
                    + " = " + formatNumber(out) + " " + to.getValue();

            Notification.show(msg, 4000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_PRIMARY);
        } catch (Exception ex) {
            Notification.show("Conversion error: " + ex.getMessage(), 4000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private String formatNumber(BigDecimal v) {
        NumberFormat nf = NumberFormat.getNumberInstance(getLocaleOrPt());
        nf.setMaximumFractionDigits(6);
        return nf.format(v);
    }

    private Locale getLocaleOrPt() {
        Locale l = getLocale();
        return l != null ? l : new Locale("pt", "PT");
    }
}
