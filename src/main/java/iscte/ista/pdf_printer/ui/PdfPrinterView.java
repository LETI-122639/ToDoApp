package iscte.ista.pdf_printer.ui;

import iscte.ista.base.ui.component.ViewToolbar;
import iscte.ista.pdf_printer.PdfPrinter;
import iscte.ista.pdf_printer.PdfPrinterService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import static com.vaadin.flow.spring.data.VaadinSpringDataHelpers.toSpringPageRequest;

@Route("pdf-printer")
@PageTitle("PDF Prints")
@Menu(order = 1, icon = "vaadin:print", title = "PDF Prints")
class PdfPrinterListView extends Main {

    private final PdfPrinterService pdfPrinterService;

    final TextField nameField;
    final Button createBtn;
    final Grid<PdfPrinter> printerGrid;

    PdfPrinterListView(PdfPrinterService pdfPrinterService) {
        this.pdfPrinterService = pdfPrinterService;

        nameField = new TextField();
        nameField.setPlaceholder("PDF name");
        nameField.setAriaLabel("PDF name");
        nameField.setMaxLength(100);
        nameField.setMinWidth("20em");

        createBtn = new Button("Create", event -> createPrinter());
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        printerGrid = new Grid<>();
        printerGrid.setItems(query -> pdfPrinterService.list(toSpringPageRequest(query)).stream());
        printerGrid.addColumn(PdfPrinter::getName).setHeader("Name");
        printerGrid.addColumn(printer -> printer.getCreatedAt().toString()).setHeader("Created At");
        printerGrid.setSizeFull();

        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Padding.MEDIUM, LumoUtility.Gap.SMALL);

        add(new ViewToolbar("PDF Prints", ViewToolbar.group(nameField, createBtn)));
        add(printerGrid);
    }

    private void createPrinter() {
        pdfPrinterService.createPdfPrinter(nameField.getValue());
        printerGrid.getDataProvider().refreshAll();
        nameField.clear();
        Notification.show("PDF added", 3000, Notification.Position.BOTTOM_END)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
}
