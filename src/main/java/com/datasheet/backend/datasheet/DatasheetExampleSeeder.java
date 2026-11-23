package com.datasheet.backend.datasheet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 개발용 예시 데이터. 이미 code+revision이 있으면 건너뜁니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatasheetExampleSeeder implements ApplicationRunner {

	private final DatasheetRepository datasheetRepository;

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		if (!datasheetRepository.existsByCodeAndRevision("CH-250", 1)) {
			seedCh250();
		} else {
			ensureCh250ArraySample();
		}
		if (!datasheetRepository.existsByCodeAndRevision("FI-70", 1)) {
			seedFi70();
		}
	}

	private void seedCh250() {
		Datasheet sheet = Datasheet.builder()
				.code("CH-250")
				.name("CH-250 단품 데이터시트")
				.revision(1)
				.fileName("CH-250_614-0325 단품 데이터시트.pdf")
				.pageWidth(new BigDecimal("595.00"))
				.pageHeight(new BigDecimal("842.00"))
				.active(true)
				.build();

		sheet.addItem(DatasheetItem.builder()
				.itemNo(1).sortOrder(1).groupName("기본정보").label("제품번호")
				.inspectionItem(false).array(false).inputType(InputType.TEXT)
				.required(true).skippable(false)
				.pdfPage(1)
				.posX(bd("120")).posY(bd("780")).posWidth(bd("120")).posHeight(bd("18"))
				.build());

		sheet.addItem(DatasheetItem.builder()
				.itemNo(2).sortOrder(2).groupName("기본정보").label("검사자")
				.inspectionItem(false).array(false).inputType(InputType.TEXT)
				.required(true).skippable(false)
				.pdfPage(1)
				.posX(bd("320")).posY(bd("780")).posWidth(bd("80")).posHeight(bd("18"))
				.build());

		sheet.addItem(DatasheetItem.builder()
				.itemNo(3).sortOrder(3).groupName("환경").label("온도")
				.inspectionItem(false).array(false).inputType(InputType.NUMBER).unit("℃").decimalPlaces(1)
				.allowNegative(true).required(false).skippable(true)
				.pdfPage(1)
				.posX(bd("120")).posY(bd("750")).posWidth(bd("50")).posHeight(bd("18"))
				.build());

		sheet.addItem(DatasheetItem.builder()
				.itemNo(4).sortOrder(4).groupName("외관").label("외관 상태")
				.inspectionItem(true).array(false).inputType(InputType.OK_NG)
				.required(true).skippable(false)
				.pdfPage(1)
				.posX(bd("120")).posY(bd("700")).posWidth(bd("40")).posHeight(bd("20"))
				.build());

		sheet.addItem(DatasheetItem.builder()
				.itemNo(5).sortOrder(5).groupName("치수").label("전장")
				.inspectionItem(true).array(false).inputType(InputType.NUMBER).unit("mm").decimalPlaces(2)
				.minValue(bd("99.50")).maxValue(bd("100.50"))
				.required(true).skippable(false)
				.pdfPage(1)
				.posX(bd("200")).posY(bd("650")).posWidth(bd("60")).posHeight(bd("18"))
				.build());

		sheet.addItem(DatasheetItem.builder()
				.itemNo(6).sortOrder(6).groupName("치수").label("전폭")
				.inspectionItem(true).array(false).inputType(InputType.NUMBER).unit("mm").decimalPlaces(2)
				.minValue(bd("49.80")).maxValue(bd("50.20"))
				.required(true).skippable(true)
				.pdfPage(1)
				.posX(bd("200")).posY(bd("620")).posWidth(bd("60")).posHeight(bd("18"))
				.build());

		sheet.addItem(buildPointThicknessArrayItem(7, 7));

		datasheetRepository.save(sheet);
		log.info("예시 데이터시트 생성: CH-250");
	}

	private void ensureCh250ArraySample() {
		Datasheet sheet = datasheetRepository.findByIdWithItems(
				datasheetRepository.findByActiveTrueOrderByCodeAscRevisionDesc().stream()
						.filter(d -> "CH-250".equals(d.getCode()) && d.getRevision() == 1)
						.map(Datasheet::getId)
						.findFirst()
						.orElse(null)
		).orElse(null);

		if (sheet == null) {
			return;
		}
		boolean hasArray = sheet.getItems().stream().anyMatch(DatasheetItem::isArray);
		if (hasArray) {
			return;
		}

		int nextNo = sheet.getItems().stream().mapToInt(DatasheetItem::getItemNo).max().orElse(0) + 1;
		sheet.addItem(buildPointThicknessArrayItem(nextNo, nextNo));
		datasheetRepository.save(sheet);
		log.info("CH-250에 배열 예시 항목 추가: itemNo={}", nextNo);
	}

	private DatasheetItem buildPointThicknessArrayItem(int itemNo, int sortOrder) {
		DatasheetItem item = DatasheetItem.builder()
				.itemNo(itemNo).sortOrder(sortOrder).groupName("치수").label("포인트 두께")
				.inspectionItem(true)
				.array(true).arraySize(5)
				.inputType(InputType.NUMBER).unit("mm").decimalPlaces(2)
				.minValue(bd("1.00")).maxValue(bd("1.20"))
				.required(true).skippable(false)
				.pdfPage(1)
				.posX(bd("200")).posY(bd("580")).posWidth(bd("40")).posHeight(bd("16"))
				.build();

		for (int i = 0; i < 5; i++) {
			item.addSlot(DatasheetItemSlot.builder()
					.slotIndex(i)
					.label("P" + (i + 1))
					.pdfPage(1)
					.posX(bd(String.valueOf(200 + i * 45)))
					.posY(bd("580"))
					.posWidth(bd("40"))
					.posHeight(bd("16"))
					.build());
		}
		return item;
	}

	private void seedFi70() {
		Datasheet sheet = Datasheet.builder()
				.code("FI-70")
				.name("FI-70 검사 데이터시트")
				.revision(1)
				.fileName("FI-70_414-0802 데이터시트.pdf")
				.pageWidth(new BigDecimal("595.00"))
				.pageHeight(new BigDecimal("842.00"))
				.active(true)
				.build();

		sheet.addItem(DatasheetItem.builder()
				.itemNo(1).sortOrder(1).groupName("기본정보").label("기번")
				.inspectionItem(false).array(false).inputType(InputType.TEXT)
				.required(true).skippable(false)
				.pdfPage(1)
				.posX(bd("100")).posY(bd("780")).posWidth(bd("100")).posHeight(bd("18"))
				.build());

		sheet.addItem(DatasheetItem.builder()
				.itemNo(2).sortOrder(2).groupName("전기").label("절연저항")
				.inspectionItem(true).array(false).inputType(InputType.NUMBER).unit("MΩ").decimalPlaces(1)
				.minValue(bd("100.0")).maxValue(null)
				.required(true).skippable(false)
				.pdfPage(1)
				.posX(bd("180")).posY(bd("680")).posWidth(bd("50")).posHeight(bd("18"))
				.build());

		sheet.addItem(DatasheetItem.builder()
				.itemNo(3).sortOrder(3).groupName("전기").label("동작확인")
				.inspectionItem(true).array(false).inputType(InputType.OK_NG)
				.required(true).skippable(false)
				.pdfPage(1)
				.posX(bd("180")).posY(bd("640")).posWidth(bd("40")).posHeight(bd("20"))
				.build());

		datasheetRepository.save(sheet);
		log.info("예시 데이터시트 생성: FI-70");
	}

	private static BigDecimal bd(String value) {
		return new BigDecimal(value);
	}
}
