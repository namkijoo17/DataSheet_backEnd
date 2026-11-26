package com.datasheet.backend.inspection;

import com.datasheet.backend.datasheet.Datasheet;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "inspections")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inspection {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "datasheet_id", nullable = false)
	private Datasheet datasheet;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private InspectionMode mode;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private InspectionStatus status = InspectionStatus.IN_PROGRESS;

	@Column(name = "serial_no", length = 100, columnDefinition = "nvarchar(100)")
	private String serialNo;

	@Column(length = 100, columnDefinition = "nvarchar(100)")
	private String unit;

	@Column(length = 100, columnDefinition = "nvarchar(100)")
	private String line;

	@Column(name = "inspect_date")
	private LocalDate inspectDate;

	@Column(length = 100, columnDefinition = "nvarchar(100)")
	private String inspector;

	@Column(name = "inspector_change_reason", length = 200, columnDefinition = "nvarchar(200)")
	private String inspectorChangeReason;

	@Column(length = 20, columnDefinition = "nvarchar(20)")
	private String temperature;

	@Column(length = 20, columnDefinition = "nvarchar(20)")
	private String humidity;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "inspection_parts", joinColumns = @JoinColumn(name = "inspection_id"))
	@MapKeyColumn(name = "part_key", length = 30)
	@Column(name = "part_value", length = 100, columnDefinition = "nvarchar(100)")
	private Map<String, String> parts = new LinkedHashMap<>();

	@OneToMany(mappedBy = "inspection", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<InspectionAnswer> answers = new ArrayList<>();

	@Column(name = "completed_at")
	private LocalDateTime completedAt;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	public Inspection(Datasheet datasheet, Long userId, InspectionMode mode) {
		this.datasheet = datasheet;
		this.userId = userId;
		this.mode = mode;
		LocalDateTime now = LocalDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	public void updateBasic(
			InspectionMode mode,
			String serialNo,
			String unit,
			String line,
			LocalDate inspectDate,
			String inspector,
			String inspectorChangeReason,
			String temperature,
			String humidity,
			Map<String, String> parts
	) {
		this.mode = mode;
		this.serialNo = serialNo;
		this.unit = unit;
		this.line = line;
		this.inspectDate = inspectDate;
		this.inspector = inspector;
		this.inspectorChangeReason = inspectorChangeReason;
		this.temperature = temperature;
		this.humidity = humidity;
		this.parts.clear();
		if (parts != null) {
			parts.forEach((key, value) -> {
				if (key != null && value != null && !value.isBlank()) {
					this.parts.put(key, value);
				}
			});
		}
		touch();
	}

	public InspectionAnswer findAnswer(Long datasheetItemId) {
		return answers.stream()
				.filter(answer -> answer.getDatasheetItemId().equals(datasheetItemId))
				.findFirst()
				.orElse(null);
	}

	public InspectionAnswer addAnswer(Long datasheetItemId) {
		InspectionAnswer answer = new InspectionAnswer(this, datasheetItemId);
		answers.add(answer);
		return answer;
	}

	public void removeAnswer(InspectionAnswer answer) {
		answers.remove(answer);
	}

	public void clearAnswers() {
		answers.clear();
		touch();
	}

	public void complete() {
		this.status = InspectionStatus.COMPLETED;
		this.completedAt = LocalDateTime.now();
		touch();
	}

	public boolean isEditable() {
		return status == InspectionStatus.IN_PROGRESS;
	}

	public void touch() {
		this.updatedAt = LocalDateTime.now();
	}
}
