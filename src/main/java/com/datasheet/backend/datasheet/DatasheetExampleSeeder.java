package com.datasheet.backend.datasheet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

/**
 * 개발용 예시 데이터. MU-100C/CH-252 최종검사 양식(614-0325) 기준.
 * 좌표는 PDF 좌하단 원점 (프론트 pdf-region.ts 와 동일).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatasheetExampleSeeder implements ApplicationRunner {

	private static final String CH250_CODE = "CH-250";
	private static final int CH250_REVISION = 1;
	private static final int CH250_EXPECTED_ITEMS = 32;

	/** 실제 PDF 페이지 크기 (세로 A4) */
	private static final BigDecimal PAGE_W = bd("595.44");
	private static final BigDecimal PAGE_H = bd("841.92");

	private final DatasheetRepository datasheetRepository;

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		ensureCh250FormSeed();
		if (!datasheetRepository.existsByCodeAndRevision("FI-70", 1)) {
			seedFi70();
		}
	}

	private void ensureCh250FormSeed() {
		Optional<Datasheet> existing = findByCodeAndRevision(CH250_CODE, CH250_REVISION);
		if (existing.isPresent()) {
			Datasheet sheet = datasheetRepository.findByIdWithItems(existing.get().getId()).orElse(existing.get());
			boolean sameCount = sheet.getItems().size() == CH250_EXPECTED_ITEMS;
			boolean samePage = PAGE_W.compareTo(nullToZero(sheet.getPageWidth())) == 0
					&& PAGE_H.compareTo(nullToZero(sheet.getPageHeight())) == 0;
			boolean dateSlots = sheet.getItems().stream()
					.anyMatch(i -> "검사일".equals(i.getLabel()) && i.isArray() && i.getSlots().size() == 3);
			boolean okNgSlots = sheet.getItems().stream()
					.anyMatch(i -> i.getInputType() == InputType.OK_NG && i.isArray() && i.getSlots().size() == 2);
			if (sameCount && samePage && dateSlots && okNgSlots) {
				return;
			}
			log.info("CH-250 양식 갱신 필요(items={}, page={}x{}) → 재시드",
					sheet.getItems().size(), sheet.getPageWidth(), sheet.getPageHeight());
			datasheetRepository.delete(sheet);
			datasheetRepository.flush();
		}
		seedCh250();
	}

	private static BigDecimal nullToZero(BigDecimal v) {
		return v == null ? BigDecimal.ZERO : v;
	}

	private Optional<Datasheet> findByCodeAndRevision(String code, int revision) {
		return datasheetRepository.findByActiveTrueOrderByCodeAscRevisionDesc().stream()
				.filter(d -> code.equals(d.getCode()) && d.getRevision() == revision)
				.findFirst();
	}

	private void seedCh250() {
		Datasheet sheet = Datasheet.builder()
				.code(CH250_CODE)
				.name("MU-100C/CH-252 단품 최종검사데이터시트")
				.revision(CH250_REVISION)
				.fileName("CH-250_614-0325 단품 데이터시트.pdf")
				.pageWidth(PAGE_W)
				.pageHeight(PAGE_H)
				.active(true)
				.build();

		int n = 1;

		// ── 유닛트 체크박스 (□ 위치) ──
		DatasheetItem unit = DatasheetItem.builder()
				.itemNo(n).sortOrder(n).groupName("기본정보").label("유닛트")
				.inspectionItem(false).array(true).arraySize(3)
				.inputType(InputType.CHECK)
				.required(true).skippable(false)
				.pdfPage(1)
				.posX(bd("97.30")).posY(bd(by(96.6, 10))).posWidth(bd("10.00")).posHeight(bd("10.00"))
				.build();
		String[] unitLabels = {"MU-100C", "MU-100C-CI", "CH-252"};
		double[] unitX = {97.3, 172.4, 262.0};
		for (int i = 0; i < 3; i++) {
			unit.addSlot(DatasheetItemSlot.builder()
					.slotIndex(i).label(unitLabels[i]).pdfPage(1)
					.posX(bd(fmt(unitX[i]))).posY(bd(by(96.6, 10)))
					.posWidth(bd("10.00")).posHeight(bd("10.00"))
					.build());
		}
		sheet.addItem(unit);
		n++;

		// ── 헤더 입력란 (밑줄 위) ──
		sheet.addItem(info(n++, n - 1, "기본정보", "기번", InputType.TEXT,
				true, false, 1, "90.00", by(114.0, 12), "165.00", "12.00"));

		// 검사일: 년 / 월 / 일 각각 좌표
		DatasheetItem date = DatasheetItem.builder()
				.itemNo(n).sortOrder(n).groupName("기본정보").label("검사일")
				.inspectionItem(false).array(true).arraySize(3)
				.inputType(InputType.NUMBER).decimalPlaces(0)
				.required(true).skippable(false)
				.pdfPage(1)
				.posX(bd("90.00")).posY(bd(by(134.0, 12))).posWidth(bd("48.00")).posHeight(bd("12.00"))
				.build();
		String[] dateLabels = {"년", "월", "일"};
		double[] dateX = {90.0, 155.0, 208.0};
		double[] dateW = {48.0, 35.0, 30.0};
		for (int i = 0; i < 3; i++) {
			date.addSlot(DatasheetItemSlot.builder()
					.slotIndex(i).label(dateLabels[i]).pdfPage(1)
					.posX(bd(fmt(dateX[i]))).posY(bd(by(134.0, 12)))
					.posWidth(bd(fmt(dateW[i]))).posHeight(bd("12.00"))
					.build());
		}
		sheet.addItem(date);
		n++;

		sheet.addItem(numberInfo(n++, n - 1, "환경", "온도", "℃", 1, true,
				null, null, false, true, 1, "90.00", by(154.0, 12), "30.00", "12.00"));
		sheet.addItem(numberInfo(n++, n - 1, "환경", "습도", "%", 0, false,
				bd("0"), bd("100"), false, true, 1, "188.00", by(154.0, 12), "30.00", "12.00"));
		sheet.addItem(info(n++, n - 1, "기본정보", "라인", InputType.TEXT,
				false, true, 1, "282.00", by(154.0, 12), "32.00", "12.00"));

		// ── TempNo / WORKER / LCD / PCB (라벨 우측 기입란) ──
		sheet.addItem(info(n++, n - 1, "식별정보", "TempNo", InputType.TEXT,
				false, true, 1, "292.00", by(172.0, 12), "110.00", "12.00"));
		sheet.addItem(info(n++, n - 1, "식별정보", "WORKER1", InputType.TEXT,
				true, false, 1, "292.00", by(187.0, 12), "110.00", "12.00"));
		sheet.addItem(info(n++, n - 1, "식별정보", "LCDNo1", InputType.TEXT,
				true, false, 1, "448.00", by(172.0, 12), "110.00", "12.00"));

		DatasheetItem pcb = DatasheetItem.builder()
				.itemNo(n).sortOrder(n).groupName("식별정보").label("PCBNo")
				.inspectionItem(false).array(true).arraySize(4)
				.inputType(InputType.TEXT)
				.required(true).skippable(false)
				.pdfPage(1)
				.posX(bd("448.00")).posY(bd(by(187.0, 12))).posWidth(bd("110.00")).posHeight(bd("12.00"))
				.build();
		String[] pcbLabels = {"PCBNo1", "PCBNo2", "PCBNo3", "PCBNo4"};
		double[] pcbTopY = {187.0, 202.0, 217.0, 232.0};
		for (int i = 0; i < 4; i++) {
			pcb.addSlot(DatasheetItemSlot.builder()
					.slotIndex(i).label(pcbLabels[i]).pdfPage(1)
					.posX(bd("448.00")).posY(bd(by(pcbTopY[i], 12)))
					.posWidth(bd("110.00")).posHeight(bd("12.00"))
					.build());
		}
		sheet.addItem(pcb);
		n++;

		// ── 결과란 (J/E · OK/NG 각각 좌표 · 수치) ──
		DatasheetItem je = DatasheetItem.builder()
				.itemNo(n).sortOrder(n).groupName("사양확인").label("문자표기(J/E)")
				.inspectionItem(false).array(true).arraySize(2)
				.inputType(InputType.CHECK)
				.required(true).skippable(false)
				.pdfPage(1)
				.posX(bd("483.70")).posY(bd(by(276.1, 12))).posWidth(bd("14.00")).posHeight(bd("12.00"))
				.build();
		String[] jeLabels = {"J", "E"};
		double[] jeX = {483.7, 505.0};
		for (int i = 0; i < 2; i++) {
			je.addSlot(DatasheetItemSlot.builder()
					.slotIndex(i).label(jeLabels[i]).pdfPage(1)
					.posX(bd(fmt(jeX[i]))).posY(bd(by(276.1, 12)))
					.posWidth(bd("14.00")).posHeight(bd("12.00"))
					.build());
		}
		sheet.addItem(je);
		n++;

		sheet.addItem(okNg(n++, n - 1, "에이징", "사전에이징 시험", true, false, 1, 298.4));
		sheet.addItem(okNg(n++, n - 1, "에이징", "에이징 시험", true, false, 1, 318.4));
		sheet.addItem(okNg(n++, n - 1, "에이징", "에이징 종료 시험", true, false, 1, 347.6));

		sheet.addItem(okNg(n++, n - 1, "성능", "전원 ON·화면확인", true, false, 1, 377.2));
		sheet.addItem(number(n++, n - 1, "성능", "소비전류", "A", 2, false,
				bd("0"), bd("2.00"), true, false, 1, "480.00", by(395.0, 14), "65.00", "14.00"));
		sheet.addItem(okNg(n++, n - 1, "성능", "KEY 시험", true, false, 1, 427.6));
		sheet.addItem(okNg(n++, n - 1, "성능", "LED 시험", true, false, 1, 466.2));
		sheet.addItem(okNg(n++, n - 1, "성능", "리모컨 시험", true, false, 1, 498.4));
		sheet.addItem(okNg(n++, n - 1, "성능", "Self Test", true, false, 1, 533.6));
		sheet.addItem(okNg(n++, n - 1, "성능", "TEST PATTERN", true, false, 1, 564.8));
		sheet.addItem(okNg(n++, n - 1, "성능", "간이충격", true, false, 1, 584.8));
		sheet.addItem(okNg(n++, n - 1, "성능", "LCD 표시", true, false, 1, 604.4));

		sheet.addItem(number(n++, n - 1, "성능", "휘도 Max", "cd/m²", 1, false,
				bd("100"), null, true, false, 1, "480.00", by(625.0, 18), "55.00", "18.00"));
		sheet.addItem(number(n++, n - 1, "성능", "휘도 Min", "cd/m²", 2, false,
				bd("0"), bd("1.20"), true, false, 1, "480.00", by(655.0, 18), "55.00", "18.00"));
		sheet.addItem(okNg(n++, n - 1, "성능", "휘도 그라데이션", true, false, 1, 685.1));
		sheet.addItem(okNg(n++, n - 1, "성능", "패널 조명", true, false, 1, 705.0));
		sheet.addItem(okNg(n++, n - 1, "외관", "외관검사", true, false, 1, 724.7));

		// ── 불량 수리 내역 (1차 행) ──
		sheet.addItem(info(n++, n - 1, "불량이력", "불량증상", InputType.TEXT,
				false, true, 1, "100.00", by(772.0, 16), "130.00", "16.00"));
		sheet.addItem(info(n++, n - 1, "불량이력", "원인 및 수리내역", InputType.TEXT,
				false, true, 1, "238.00", by(772.0, 16), "165.00", "16.00"));
		sheet.addItem(info(n++, n - 1, "불량이력", "확인", InputType.TEXT,
				false, true, 1, "410.00", by(772.0, 16), "80.00", "16.00"));
		sheet.addItem(info(n, n, "불량이력", "발생 LINE", InputType.TEXT,
				false, true, 1, "500.00", by(772.0, 16), "60.00", "16.00"));

		datasheetRepository.save(sheet);
		log.info("예시 데이터시트 생성: {} (항목 {}개, {}x{})",
				CH250_CODE, CH250_EXPECTED_ITEMS, PAGE_W, PAGE_H);
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

		sheet.addItem(info(1, 1, "기본정보", "기번", InputType.TEXT,
				true, false, 1, "100", "780", "100", "18"));
		sheet.addItem(number(2, 2, "전기", "절연저항", "MΩ", 1, false,
				bd("100.0"), null, true, false, 1, "180", "680", "50", "18"));
		// FI-70 간단 예시: OK/NG 슬롯 (대략 좌표)
		DatasheetItem fiOkNg = DatasheetItem.builder()
				.itemNo(3).sortOrder(3).groupName("전기").label("동작확인")
				.inspectionItem(true).array(true).arraySize(2)
				.inputType(InputType.OK_NG)
				.required(true).skippable(false)
				.pdfPage(1)
				.posX(bd("180")).posY(bd("640")).posWidth(bd("20")).posHeight(bd("20"))
				.build();
		fiOkNg.addSlot(DatasheetItemSlot.builder()
				.slotIndex(0).label("OK").pdfPage(1)
				.posX(bd("180")).posY(bd("640")).posWidth(bd("20")).posHeight(bd("20"))
				.build());
		fiOkNg.addSlot(DatasheetItemSlot.builder()
				.slotIndex(1).label("NG").pdfPage(1)
				.posX(bd("210")).posY(bd("640")).posWidth(bd("20")).posHeight(bd("20"))
				.build());
		sheet.addItem(fiOkNg);

		datasheetRepository.save(sheet);
		log.info("예시 데이터시트 생성: FI-70");
	}

	/** PDF 상단 y(top-left) + 높이 → 좌하단 원점 posY */
	private static String by(double topY, double height) {
		return PAGE_H.subtract(BigDecimal.valueOf(topY + height))
				.setScale(2, RoundingMode.HALF_UP)
				.toPlainString();
	}

	private static String fmt(double v) {
		return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).toPlainString();
	}

	private static DatasheetItem info(
			int itemNo, int sortOrder, String group, String label, InputType type,
			boolean required, boolean skippable,
			int page, String x, String y, String w, String h
	) {
		return DatasheetItem.builder()
				.itemNo(itemNo).sortOrder(sortOrder).groupName(group).label(label)
				.inspectionItem(false).array(false).inputType(type)
				.required(required).skippable(skippable)
				.pdfPage(page)
				.posX(bd(x)).posY(bd(y)).posWidth(bd(w)).posHeight(bd(h))
				.build();
	}

	/** OK / NG 각각 슬롯 좌표 (선택값은 둘 중 하나) */
	private static DatasheetItem okNg(
			int itemNo, int sortOrder, String group, String label,
			boolean required, boolean skippable,
			int page, double topY
	) {
		DatasheetItem item = DatasheetItem.builder()
				.itemNo(itemNo).sortOrder(sortOrder).groupName(group).label(label)
				.inspectionItem(true).array(true).arraySize(2)
				.inputType(InputType.OK_NG)
				.required(required).skippable(skippable)
				.pdfPage(page)
				.posX(bd("477.40")).posY(bd(by(topY, 12))).posWidth(bd("16.00")).posHeight(bd("12.00"))
				.build();
		item.addSlot(DatasheetItemSlot.builder()
				.slotIndex(0).label("OK").pdfPage(page)
				.posX(bd("477.40")).posY(bd(by(topY, 12)))
				.posWidth(bd("16.00")).posHeight(bd("12.00"))
				.build());
		item.addSlot(DatasheetItemSlot.builder()
				.slotIndex(1).label("NG").pdfPage(page)
				.posX(bd("510.00")).posY(bd(by(topY, 12)))
				.posWidth(bd("16.00")).posHeight(bd("12.00"))
				.build());
		return item;
	}

	private static DatasheetItem number(
			int itemNo, int sortOrder, String group, String label, String unit, int decimals,
			boolean allowNegative, BigDecimal min, BigDecimal max,
			boolean required, boolean skippable,
			int page, String x, String y, String w, String h
	) {
		return numberInternal(itemNo, sortOrder, group, label, unit, decimals,
				allowNegative, min, max, true, required, skippable, page, x, y, w, h);
	}

	private static DatasheetItem numberInfo(
			int itemNo, int sortOrder, String group, String label, String unit, int decimals,
			boolean allowNegative, BigDecimal min, BigDecimal max,
			boolean required, boolean skippable,
			int page, String x, String y, String w, String h
	) {
		return numberInternal(itemNo, sortOrder, group, label, unit, decimals,
				allowNegative, min, max, false, required, skippable, page, x, y, w, h);
	}

	private static DatasheetItem numberInternal(
			int itemNo, int sortOrder, String group, String label, String unit, int decimals,
			boolean allowNegative, BigDecimal min, BigDecimal max, boolean inspectionItem,
			boolean required, boolean skippable,
			int page, String x, String y, String w, String h
	) {
		return DatasheetItem.builder()
				.itemNo(itemNo).sortOrder(sortOrder).groupName(group).label(label)
				.inspectionItem(inspectionItem).array(false).inputType(InputType.NUMBER)
				.unit(unit).decimalPlaces(decimals)
				.minValue(min).maxValue(max).allowNegative(allowNegative)
				.required(required).skippable(skippable)
				.pdfPage(page)
				.posX(bd(x)).posY(bd(y)).posWidth(bd(w)).posHeight(bd(h))
				.build();
	}

	private static BigDecimal bd(String value) {
		return new BigDecimal(value);
	}
}
