package iscte.ista.pdf_printer.ui;

import iscte.ista.base.ui.component.ViewToolbar;
import iscte.ista.pdf_printer.PdfPrinter;
import iscte.ista.pdf_printer.PdfPrinterService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

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
    final Grid<PdfPrinter> printerGrid;

    PdfPrinterListView(PdfPrinterService pdfPrinterService) {
        this.pdfPrinterService = pdfPrinterService;

        // Descrição do campo de nome
        nameField = new TextField();
        nameField.setPlaceholder("PDF name");
        nameField.setAriaLabel("PDF name");
        nameField.setMaxLength(100);
        nameField.setMinWidth("20em");

        // Configuração do campo de data
        dueDateField = new DatePicker();
        dueDateField.setPlaceholder("Due date");
        dueDateField.setAriaLabel("Due date");

        // Botão de criação
        createBtn = new Button("Create", event -> createPrinter());
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // Formatação de data
        var dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(getLocale())
                .withZone(ZoneId.systemDefault());
        var dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(getLocale());

        // Grid de exibição
        printerGrid = new Grid<>();
        printerGrid.setItems(query -> pdfPrinterService.list(toSpringPageRequest(query)).stream());
        printerGrid.addColumn(PdfPrinter::getName).setHeader("Name");
        printerGrid.addColumn(printer -> Optional.ofNullable(printer.getDueDate()).map(dateFormatter::format).orElse("Not Set"))
                .setHeader("Due Date");
        printerGrid.addColumn(printer -> dateTimeFormatter.format(printer.getCreatedAt())).setHeader("Created At");
        printerGrid.setSizeFull();

        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Padding.MEDIUM, LumoUtility.Gap.SMALL);

        // Adiciona a barra de ferramentas
        add(new ViewToolbar("PDF Prints", ViewToolbar.group(nameField, dueDateField, createBtn)));
        add(printerGrid);
    }

    private void createPrinter() {
        // Verifica se a data foi selecionada
        if (dueDateField.isEmpty()) {
            Notification.show("Please select a due date.", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        // Chama o serviço para criar o PdfPrinter com a data de vencimento
        pdfPrinterService.createPdfPrinter(nameField.getValue(), dueDateField.getValue());
        printerGrid.getDataProvider().refreshAll();
        nameField.clear();
        dueDateField.clear();
        Notification.show("PDF added", 3000, Notification.Position.BOTTOM_END)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
}
