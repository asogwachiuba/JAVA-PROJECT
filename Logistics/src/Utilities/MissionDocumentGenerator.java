package Utilities;

import java.io.FileOutputStream;
import java.util.List;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

//import org.apache.poi.xwpf.usermodel.*;

import Database.DatabaseConstants;
import Models.Mission;

public class MissionDocumentGenerator {

    public static String generateMissionDocument(String dateSelected, List<Mission> missions) {
        try {
            // Create Word document
            XWPFDocument document = new XWPFDocument();

            // Add title with the chosen day
            XWPFParagraph titleParagraph = document.createParagraph();
            titleParagraph.setAlignment(ParagraphAlignment.CENTER);

            XWPFRun titleRun = titleParagraph.createRun();
            titleRun.setBold(true);
            titleRun.setFontSize(16);
            titleRun.setText(dateSelected);
            titleParagraph.setSpacingAfter(200);

            // Add missions to the document
            for (Mission mission : missions) {
                // Mission title
                XWPFParagraph missionTitleParagraph = document.createParagraph();
                missionTitleParagraph.setAlignment(ParagraphAlignment.LEFT);

                XWPFRun missionTitleRun = missionTitleParagraph.createRun();
                missionTitleRun.setBold(true);
                missionTitleRun.setFontSize(14);
                missionTitleRun.setText("Driver: " + mission.getDriverFirstName() + " " + mission.getDriverLastName());
                missionTitleParagraph.setSpacingAfter(100);

                // Mission details
                XWPFParagraph missionDetailsParagraph = document.createParagraph();
                missionDetailsParagraph.setAlignment(ParagraphAlignment.BOTH);

                StringBuilder routeBuilder = new StringBuilder(DatabaseConstants.warehouseAddress + " -> ");
                mission.getOrders().stream()
                        .sorted((o1, o2) -> Integer.compare(o1.getPriority(), o2.getPriority()))
                        .forEach(order -> routeBuilder.append(order.getDeliveryAddress()).append(" -> "));
                routeBuilder.append(DatabaseConstants.warehouseAddress);

                XWPFRun missionDetailsRun = missionDetailsParagraph.createRun();
                missionDetailsRun.setText("Route: " + routeBuilder);

                // Add space after each mission
                XWPFParagraph spaceParagraph = document.createParagraph();
                spaceParagraph.createRun().addCarriageReturn();
            }

            // Save Word document
            String outputPath = "/Users/user/eclipse-workspace/Logistics/src/Utilities/"; // Change to desired directory
            String fileName = dateSelected + "-MissionsReport.docx";

            try (FileOutputStream fileOut = new FileOutputStream(outputPath + fileName)) {
                document.write(fileOut);
            }

            return "Document generated successfully: " + outputPath + fileName;
        } catch (Exception e) {
            return "Error generating document: " + e.getMessage();
        }
    }

}

