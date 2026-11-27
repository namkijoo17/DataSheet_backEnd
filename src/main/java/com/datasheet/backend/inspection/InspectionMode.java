package com.datasheet.backend.inspection;

public enum InspectionMode {
	/** 기본 검사: 필수 항목 입력 후 자동 판정 */
	BASIC,
	/** 디버깅: 고른 항목만 검사하고, 나머지는 SKIP 으로 저장 */
	DEBUG,
	/** 부품출하: 스킵 가능 항목만 사용자가 의도적으로 SKIP 표시 */
	PARTS_SHIPPING
}
