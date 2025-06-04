package com.zhsaidk.service;

import com.opencsv.CSVWriter;
import com.zhsaidk.database.entity.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventExportService {

    public byte[] exportCsv(List<Event> events) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream))) {
            writer.writeNext(new String[]{"ID", "Название", "Дата создания(local)", "Дата создания"});
            for (Event event : events) {
                writer.writeNext(new String[]{
                        String.valueOf(event.getId()),
                        event.getName(),
                        event.getLocalCreatedAt().toString(),
                        event.getCreatedAt().toString()});
            }
        } catch (IOException exception) {
            log.error("An occurred error while writing CSV: {}", exception.getMessage());
        }
        return outputStream.toByteArray();
    }

    public byte[] exportExel(List<Event> events) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Events");
            XSSFRow row = sheet.createRow(0);
            row.createCell(0).setCellValue("ID");
            row.createCell(1).setCellValue("Название");
            row.createCell(2).setCellValue("Локальная дата создания");
            row.createCell(3).setCellValue("Дата создания");

            int rowNum = 1;
            for (Event event : events) {
                XSSFRow currentRow = sheet.createRow(rowNum++);
                currentRow.createCell(0).setCellValue(String.valueOf(event.getId()));
                currentRow.createCell(1).setCellValue(event.getName() != null ? event.getName() : "N/A");
                currentRow.createCell(2).setCellValue(event.getLocalCreatedAt() != null ? event.getLocalCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME) : "N/A");
                currentRow.createCell(3).setCellValue(event.getCreatedAt() != null ? event.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME) : "N/A");
            }
            workbook.write(outputStream);
        } catch (Exception exception) {
            log.error("An occurred error while writing Exel");
        }
        return outputStream.toByteArray();
    }
}
