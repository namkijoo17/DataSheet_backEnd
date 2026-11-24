package com.datasheet.backend.datasheet;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 다른 항목(기종 CIRCLE/CHECK 등)의 선택 슬롯에 따라
 * 숫자 항목의 min/max 판정 범위를 바꾼다.
 */
@Entity
@Table(name = "datasheet_item_range_rules")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DatasheetItemRangeRule {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Setter(AccessLevel.PACKAGE)
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "item_id", nullable = false)
	private DatasheetItem item;

	/** 기준이 되는 기종/선택 항목 (같은 시트) */
	@Column(name = "depends_on_item_id", nullable = false)
	private Long dependsOnItemId;

	/** 기준 항목에서 선택된 슬롯 인덱스 */
	@Column(name = "match_slot_index", nullable = false)
	private int matchSlotIndex;

	@Column(name = "min_value", precision = 18, scale = 6)
	private BigDecimal minValue;

	@Column(name = "max_value", precision = 18, scale = 6)
	private BigDecimal maxValue;

	@Builder
	public DatasheetItemRangeRule(
			Long dependsOnItemId,
			int matchSlotIndex,
			BigDecimal minValue,
			BigDecimal maxValue
	) {
		this.dependsOnItemId = dependsOnItemId;
		this.matchSlotIndex = matchSlotIndex;
		this.minValue = minValue;
		this.maxValue = maxValue;
	}
}
