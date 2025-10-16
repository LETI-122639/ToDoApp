package iscte.ista.pdf_printer.ui;

import iscte.ista.base.ui.component.ViewToolbar;
import iscte.ista.pdf_printer.PdfPrinter;
import iscte.ista.pdf_printer.PdfPrinterService;
import iscte.ista.pdf_printer.PdfPrinterService.DueFilter;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Optional;

import static com.vaadin.flow.spring.data.VaadinSpringDataHelpers.toSpringPageRequest;

@Route("pdf-prints")
@PageTitle("PDF Prints")
@Menu(order = 1, icon = "vaadin:print", title = "PDF Prints")
class PdfPrinterListView extends Main {

    private final PdfPrinterService pdfPrinterService;

    final TextField nameField;
    final DatePicker dueDateField;
    final Button createBtn;

    // NOVO:
    final TextField searchField;
    final ComboBox<DueFilter> dueFilterBox;
    final Span totalLabel;

    final Grid<PdfPrinter> printerGrid;

    PdfPrinterListView(PdfPrinterService pdfPrinterService) {
        this.pdfPrinterService = pdfPrinterService;

        // Campo de criação: nome
        nameField = new TextField();
        nameField.setPlaceholder("PDF name");
        nameField.setAriaLabel("PDF name");
        nameField.setMaxLength(100);
        nameField.setMinWidth("20em");

        // Campo de criação: due date
        dueDateField = new DatePicker();
        dueDateField.setPlaceholder("Due date");
        dueDateField.setAriaLabel("Due date");

        // Botão criar
        createBtn = new Button("Create", event -> createPrinter());
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // NOVO: pesquisa por nome
        searchField = new TextField();
        searchField.setPlaceholder("Search by name…");
        searchField.setClearButtonVisible(true);
        searchField.setWidth("16em");

        // NOVO: filtro de vencimento
        dueFilterBox = new ComboBox<>("Filter");
        dueFilterBox.setItems(DueFilter.ALL, DueFilter.NO_DUE_DATE, DueFilter.OVERDUE, DueFilter.DUE_TODAY, DueFilter.UPCOMING);
        dueFilterBox.setItemLabelGenerator(v -> switch (v) {
            case ALL -> "All";
            case NO_DUE_DATE -> "No due date";
            case OVERDUE -> "Overdue";
            case DUE_TODAY -> "Due today";
            case UPCOMING -> "Upcoming";
        });
        dueFilterBox.setValue(DueFilter.ALL);
        dueFilterBox.setWidth("12em");
        dueFilterBox.setClearButtonVisible(true);

        // Formatação de data
        var dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(getLocale())
                .withZone(ZoneId.systemDefault());
        var dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(getLocale());

        // Grid
        printerGrid = new Grid<>();
        printerGrid.setSizeFull();

        // Colunas: nome (ordenável)
        var nameCol = printerGrid.addColumn(PdfPrinter::getName).setHeader("Name").setSortable(true);

        // Coluna: Due Date (ordenável)
        var dueCol = printerGrid.addColumn(printer ->
                Optional.ofNullable(printer.getDueDate()).map(dateFormatter::format).orElse("Not set")
        ).setHeader("Due Date").setSortable(true);

        // Coluna: Created At (ordenável)
        var createdCol = printerGrid.addColumn(printer -> dateTimeFormatter.format(printer.getCreatedAt()))
                .setHeader("Created At").setSortable(true);

        // NOVO: Coluna estado (badge)
        printerGrid.addComponentColumn(this::renderDueBadge).setHeader("Status");

        // NOVO: total
        totalLabel = new Span("Total: 0");
        totalLabel.addClassNames(LumoUtility.FontWeight.SEMIBOLD);

        // NOVO: Data Provider (lazy) com fetch+count para pesquisa/filtros/ordenacao/paginacao
        var dp = new CallbackDataProvider<PdfPrinter, Void>(
                query -> {
                    var pageable = toSpringPageRequest(query);
                    String q = searchField.getValue();
                    var filter = Optional.ofNullable(dueFilterBox.getValue()).orElse(DueFilter.ALL);

                    return pdfPrinterService
                            .list(q, filter, pageable)
                            .stream();
                },
                query -> {
                    String q = searchField.getValue();
                    var filter = Optional.ofNullable(dueFilterBox.getValue()).orElse(DueFilter.ALL);
                    long count = pdfPrinterService.count(q, filter);
                    totalLabel.setText("Total: " + count);
                    return (int) Math.min(count, Integer.MAX_VALUE);
                }
        );
        printerGrid.setDataProvider(dp);

        // Ordenação por defeito (Created At desc)
        printerGrid.sort(GridSortOrder.desc(createdCol).build());

        // Layout
        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Padding.MEDIUM, LumoUtility.Gap.SMALL);

        // Toolbar com novos controlos
        add(new ViewToolbar("PDF Prints",
                ViewToolbar.group(nameField, dueDateField, createBtn),
                ViewToolbar.group(searchField, dueFilterBox, totalLabel)
        ));

        add(printerGrid);

        // Listeners para refrescar quando pesquisa/filtro mudam
        searchField.addValueChangeListener(e -> printerGrid.getDataProvider().refreshAll());
        dueFilterBox.addValueChangeListener(e -> printerGrid.getDataProvider().refreshAll());
    }

    // Badge de estado (overdue / hoje / ok / sem data)
    private Span renderDueBadge(PdfPrinter p) {
        LocalDate today = LocalDate.now();
        Span badge;
        if (p.getDueDate() == null) {
            badge = new Span("—");
            badge.getElement().getThemeList().add("badge");
            return badge;
        }
        if (p.getDueDate().isBefore(today)) {
            badge = new Span("Overdue");
            badge.getElement().getThemeList().add("badge error");
        } else if (p.getDueDate().isEqual(today)) {
            badge = new Span("Due today");
            badge.getElement().getThemeList().add("badge contrast");
        } else {
            badge = new Span("Upcoming");
            badge.getElement().getThemeList().add("badge success");
        }
        return badge;
    }

    private void createPrinter() {
        if (dueDateField.isEmpty()) {
            Notification.show("Please select a due date.", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        pdfPrinterService.createPdfPrinter(nameField.getValue(), dueDateField.getValue());
        printerGrid.getDataProvider().refreshAll();
        nameField.clear();
        dueDateField.clear();
        Notification.show("PDF added", 3000, Notification.Position.BOTTOM_END)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
}
