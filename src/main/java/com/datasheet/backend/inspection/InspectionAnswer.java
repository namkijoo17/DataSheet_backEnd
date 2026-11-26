package com.datasheet.backend.inspection;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Entity
@Table(name = "inspection_answers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InspectionAnswer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "inspection_id", nullable = false)
	private Inspection inspection;

	@Column(name = "datasheet_item_id", nullable = false)
	private Long datasheetItemId;

	@Column(nullable = false)
	private boolean skipped = false;

	@Column(name = "answer_value", length = 1000, columnDefinition = "nvarchar(1000)")
	private String value;

	@Column(name = "slot_index")
	private Integer slotIndex;

	/** OK / NG */
	@Column(length = 10)
	private String judgment;

	/** NG 발생 시 선택한 처리 방법 */
	@Enumerated(EnumType.STRING)
	@Column(name = "ng_action", length = 20)
	private NgAction ngAction;

	@Column(name = "ng_occurred_at")
	private LocalDateTime ngOccurredAt;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "inspection_answer_slot_values", joinColumns = @JoinColumn(name = "answer_id"))
	@MapKeyColumn(name = "slot_index")
	@Column(name = "slot_value", length = 500, columnDefinition = "nvarchar(500)")
	private Map<Integer, String> slotValues = new LinkedHashMap<>();

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	public InspectionAnswer(Inspection inspection, Long datasheetItemId) {
		this.inspection = inspection;
		this.datasheetItemId = datasheetItemId;
		this.updatedAt = LocalDateTime.now();
	}

	public void apply(
			boolean skipped,
			String value,
			Integer slotIndex,
			String judgment,
			Map<Integer, String> slotValues,
			NgAction ngAction,
			LocalDateTime ngOccurredAt
	) {
		this.skipped = skipped;
		this.value = value;
		this.slotIndex = slotIndex;
		this.judgment = judgment;
		this.ngAction = ngAction;
		this.ngOccurredAt = ngAction != null ? ngOccurredAt : null;
		this.slotValues.clear();
		if (slotValues != null) {
			slotValues.forEach((key, val) -> {
				if (key != null && val != null && !val.isBlank()) {
					this.slotValues.put(key, val);
				}
			});
		}
		this.updatedAt = LocalDateTime.now();
	}

	/** 사용자가 입력한 내용이 하나라도 있으면 true */
	public boolean hasContent() {
		if (skipped) {
			return true;
		}
		if (value != null && !value.isBlank()) {
			return true;
		}
		if (slotIndex != null || judgment != null) {
			return true;
		}
		return slotValues.values().stream().anyMatch(v -> v != null && !v.isBlank());
	}
}
