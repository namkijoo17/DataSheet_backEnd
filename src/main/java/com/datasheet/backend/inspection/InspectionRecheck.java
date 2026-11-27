package com.datasheet.backend.inspection;

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
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 항목을 다시 검사할 때 남기는 이력 (사유 포함). 답변은 지워지므로 별도 테이블에 보관한다. */
@Entity
@Table(name = "inspection_rechecks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InspectionRecheck {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "inspection_id", nullable = false)
	private Inspection inspection;

	@Column(name = "datasheet_item_id", nullable = false)
	private Long datasheetItemId;

	@Column(name = "item_label", length = 200, columnDefinition = "nvarchar(200)")
	private String itemLabel;

	@Column(nullable = false, length = 200, columnDefinition = "nvarchar(200)")
	private String reason;

	/** 다시 검사하기 전에 입력돼 있던 값 요약 */
	@Column(name = "previous_summary", length = 300, columnDefinition = "nvarchar(300)")
	private String previousSummary;

	@Column(length = 100, columnDefinition = "nvarchar(100)")
	private String inspector;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	public InspectionRecheck(
			Inspection inspection,
			Long datasheetItemId,
			String itemLabel,
			String reason,
			String previousSummary,
			String inspector
	) {
		this.inspection = inspection;
		this.datasheetItemId = datasheetItemId;
		this.itemLabel = itemLabel;
		this.reason = reason;
		this.previousSummary = previousSummary;
		this.inspector = inspector;
		this.createdAt = LocalDateTime.now();
	}
}
