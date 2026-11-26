package com.datasheet.backend.inspection;

/** NG 발생 시 작업자가 고른 처리 방법 */
public enum NgAction {
	/** 다음 항목 검사로 계속 */
	CONTINUE,
	/** 검사 종료 및 부적합 등록 */
	END,
	/** NG 항목만 재검증 */
	RECHECK
}
