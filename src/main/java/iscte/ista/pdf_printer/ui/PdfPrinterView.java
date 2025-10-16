package iscte.ista.pdf_printer.ui;

import iscte.ista.base.ui.component.ViewToolbar;
import iscte.ista.pdf_printer.PdfPrinter;
import iscte.ista.pdf_printer.PdfPrinterService;
import iscte.ista.pdf_printer.PdfPrinterService.DueFilter;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextField;
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

    // Create
    final TextField nameField;
    final DatePicker dueDateField;
    final ComboBox<PdfPrinter.Status> statusField; // status inicial
    final Button createBtn;

    // Filtros
    final TextField searchField;
    final ComboBox<DueFilter> dueFilterBox;
    final ComboBox<PdfPrinter.Status> statusFilterBox; // NOVO
    final Span totalLabel;

    // Grid
    final Grid<PdfPrinter> printerGrid;

    PdfPrinterListView(PdfPrinterService pdfPrinterService) {
        this.pdfPrinterService = pdfPrinterService;

        // ---- Create controls
        nameField = new TextField();
        nameField.setPlaceholder("PDF name");
        nameField.setAriaLabel("PDF name");
        nameField.setMaxLength(100);
        nameField.setMinWidth("20em");

        dueDateField = new DatePicker();
        dueDateField.setPlaceholder("Due date");
        dueDateField.setAriaLabel("Due date");

        statusField = new ComboBox<>();
        statusField.setPlaceholder("Status");
        statusField.setItems(PdfPrinter.Status.values());
        statusField.setItemLabelGenerator(this::statusLabel);
        statusField.setWidth("12em");
        statusField.setValue(PdfPrinter.Status.PENDING);

        createBtn = new Button("Create", event -> createPrinter());
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // ---- Filters
        searchField = new TextField();
        searchField.setPlaceholder("Search by name…");
        searchField.setClearButtonVisible(true);
        searchField.setWidth("16em");

        dueFilterBox = new ComboBox<>("Due filter");
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

        statusFilterBox = new ComboBox<>("Status filter");
        statusFilterBox.setItems(PdfPrinter.Status.values());
        statusFilterBox.setItemLabelGenerator(this::statusLabel);
        statusFilterBox.setPlaceholder("Any");
        statusFilterBox.setClearButtonVisible(true);
        statusFilterBox.setWidth("12em");

        totalLabel = new Span("Total: 0");
        totalLabel.addClassNames(LumoUtility.FontWeight.SEMIBOLD);

        // ---- Grid
        var dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(getLocale())
                .withZone(ZoneId.systemDefault());
        var dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(getLocale());

        printerGrid = new Grid<>();
        printerGrid.setSizeFull();

        var nameCol = printerGrid.addColumn(PdfPrinter::getName).setHeader("Name").setSortable(true);

        var dueCol = printerGrid.addColumn(printer ->
                Optional.ofNullable(printer.getDueDate()).map(dateFormatter::format).orElse("Not set")
        ).setHeader("Due Date").setSortable(true);

        var createdCol = printerGrid.addColumn(printer -> dateTimeFormatter.format(printer.getCreatedAt()))
                .setHeader("Created At").setSortable(true);

        // Badge por vencimento
        printerGrid.addComponentColumn(this::renderDueBadge).setHeader("Due").setAutoWidth(true);

        // Editor de status inline (corrigido para atualizar a instância local)
        printerGrid.addComponentColumn(this::renderStatusEditor).setHeader("Status").setAutoWidth(true);

        // Data provider com ambos os filtros
        var dp = new CallbackDataProvider<PdfPrinter, Void>(
                query -> {
                    var pageable = toSpringPageRequest(query);
                    String q = searchField.getValue();
                    var dueFilter = Optional.ofNullable(dueFilterBox.getValue()).orElse(DueFilter.ALL);
                    var statusFilter = statusFilterBox.getValue(); // pode ser null

                    return pdfPrinterService
                            .list(q, dueFilter, statusFilter, pageable)
                            .stream();
                },
                query -> {
                    String q = searchField.getValue();
                    var dueFilter = Optional.ofNullable(dueFilterBox.getValue()).orElse(DueFilter.ALL);
                    var statusFilter = statusFilterBox.getValue();
                    long count = pdfPrinterService.count(q, dueFilter, statusFilter);
                    totalLabel.setText("Total: " + count);
                    return (int) Math.min(count, Integer.MAX_VALUE);
                }
        );
        printerGrid.setDataProvider(dp);

        printerGrid.sort(GridSortOrder.desc(createdCol).build());

        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Padding.MEDIUM, LumoUtility.Gap.SMALL);

        add(new ViewToolbar("PDF Prints",
                ViewToolbar.group(nameField, dueDateField, statusField, createBtn),
                ViewToolbar.group(searchField, dueFilterBox, statusFilterBox, totalLabel)
        ));

        add(printerGrid);

        // Refresh nos filtros/pesquisa
        searchField.addValueChangeListener(e -> printerGrid.getDataProvider().refreshAll());
        dueFilterBox.addValueChangeListener(e -> printerGrid.getDataProvider().refreshAll());
        statusFilterBox.addValueChangeListener(e -> printerGrid.getDataProvider().refreshAll());
    }

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

    /** Editor de status por linha. Corrigido: atualiza a entidade local + refreshItem. */
    private ComboBox<PdfPrinter.Status> renderStatusEditor(PdfPrinter p) {
        ComboBox<PdfPrinter.Status> cb = new ComboBox<>();
        cb.setItems(PdfPrinter.Status.values());
        cb.setItemLabelGenerator(this::statusLabel);
        cb.setValue(p.getStatus());
        cb.setWidth("12em");

        cb.addValueChangeListener(e -> {
            var newStatus = e.getValue();
            if (newStatus == null || p.getId() == null) return;
            try {
                pdfPrinterService.updateStatus(p.getId(), newStatus);
                // Atualiza a instância apresentada no Grid para refletir o novo estado
                p.setStatus(newStatus);
                printerGrid.getDataProvider().refreshItem(p);
                Notification.show("Status updated", 2500, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                // Reverter UI se falhar
                cb.setValue(p.getStatus());
                Notification.show("Failed to update status: " + ex.getMessage(), 4000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        return cb;
    }

    private String statusLabel(PdfPrinter.Status s) {
        return switch (s) {
            case PENDING -> "Pending";
            case PRINTED -> "Printed";
            case SENT -> "Sent";
            case CANCELED -> "Canceled";
        };
    }

    private void createPrinter() {
        if (dueDateField.isEmpty()) {
            Notification.show("Please select a due date.", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        var status = Optional.ofNullable(statusField.getValue()).orElse(PdfPrinter.Status.PENDING);
        pdfPrinterService.createPdfPrinter(nameField.getValue(), dueDateField.getValue(), status);

        // Limpar + refresh
        nameField.clear();
        dueDateField.clear();
        statusField.setValue(PdfPrinter.Status.PENDING);
        printerGrid.getDataProvider().refreshAll();

        Notification.show("PDF added", 3000, Notification.Position.BOTTOM_END)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
}