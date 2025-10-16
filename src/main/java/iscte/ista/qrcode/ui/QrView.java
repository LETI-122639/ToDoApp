package iscte.ista.qrcode.ui;

import iscte.ista.base.ui.component.ViewToolbar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant; // <- igual ao TaskList: usar variant
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

@Route("qrcode")
@PageTitle("QR Code")
@Menu(order = 1, title = "QR Code")
class QrView extends Main { // <- sem 'public', tal como o TaskListView

    // <- campos 'final' sem 'private', tal como no TaskListView
    final TextField content;
    final Button generateBtn;
    final Image qrImage;

    QrView() { // <- construtor sem 'public', igual ao TaskListView
        content = new TextField();
        content.setPlaceholder("Texto/URL para o QR");
        content.setAriaLabel("QR content");
        content.setMinWidth("20em");

        generateBtn = new Button("Generate", e -> generateQr());
        generateBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        qrImage = new Image();
        qrImage.setWidth("256px");
        qrImage.setHeight("256px");
        qrImage.getStyle().set("border", "1px solid var(--lumo-contrast-10pct)");
        qrImage.setAlt("QR Code");
        qrImage.setVisible(false);

        setSizeFull();
        // <- chamada num único statement, como no TaskListView
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Padding.MEDIUM, LumoUtility.Gap.SMALL);

        add(new ViewToolbar("QR Code", ViewToolbar.group(content, generateBtn)));
        add(qrImage);
    }

    private void generateQr() {
        String text = content.getValue();
        if (text == null || text.isBlank()) {
            Notification.show("Escreve algum texto/URL primeiro.", 2500, Notification.Position.BOTTOM_END);
            return;
        }

        try {
            int size = 256;
            BitMatrix matrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, size, size);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", baos);
            byte[] png = baos.toByteArray();

            StreamResource resource = new StreamResource("qrcode.png", () -> new ByteArrayInputStream(png));

            qrImage.setSrc(resource);
            qrImage.setVisible(true);

            // <- tal como no TaskListView: sucesso com variant + limpar campo
            Notification.show("QR Code gerado!", 1500, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            content.clear();

        } catch (Exception ex) {
            Notification.show("Falha a gerar QR: " + ex.getMessage(), 3000, Notification.Position.BOTTOM_END);
        }
    }
}