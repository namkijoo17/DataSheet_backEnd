package com.datasheet.backend.datasheet;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.BatchSize;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "datasheet_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DatasheetItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Setter(AccessLevel.PACKAGE)
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "datasheet_id", nullable = false)
	private Datasheet datasheet;

	@Column(name = "item_no", nullable = false)
	private int itemNo;

	@Column(name = "group_name", length = 100, columnDefinition = "nvarchar(100)")
	private String groupName;

	@Column(nullable = false, length = 200, columnDefinition = "nvarchar(200)")
	private String label;

	/** true = 검사항목(판정 대상), false = 정보/환경 등 비검사 입력 */
	@Column(name = "is_inspection_item", nullable = false)
	private boolean inspectionItem = true;

	/** true = 동일 규격의 다중 값(배열) 입력 */
	@Column(name = "is_array", nullable = false)
	private boolean array = false;

	/** 배열일 때 요소 개수 */
	@Column(name = "array_size")
	private Integer arraySize;

	@Enumerated(EnumType.STRING)
	@Column(name = "input_type", nullable = false, length = 20)
	private InputType inputType;

	@Column(length = 20, columnDefinition = "nvarchar(20)")
	private String unit;

	@Column(name = "decimal_places")
	private Integer decimalPlaces;

	@Column(name = "min_value", precision = 18, scale = 6)
	private BigDecimal minValue;

	@Column(name = "max_value", precision = 18, scale = 6)
	private BigDecimal maxValue;

	@Column(name = "allow_negative", nullable = false)
	private boolean allowNegative = false;

	@Column(nullable = false)
	private boolean required = true;

	@Column(nullable = false)
	private boolean skippable = false;

	@Column(length = 500, columnDefinition = "nvarchar(500)")
	private String formula;

	@Column(name = "pdf_page", nullable = false)
	private int pdfPage = 1;

	@Column(name = "pos_x", nullable = false, precision = 10, scale = 2)
	private BigDecimal posX;

	@Column(name = "pos_y", nullable = false, precision = 10, scale = 2)
	private BigDecimal posY;

	@Column(name = "pos_width", precision = 10, scale = 2)
	private BigDecimal posWidth;

	@Column(name = "pos_height", precision = 10, scale = 2)
	private BigDecimal posHeight;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder = 0;

	@OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("slotIndex ASC")
	private Set<DatasheetItemSlot> slots = new LinkedHashSet<>();

	@OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("matchSlotIndex ASC")
	@BatchSize(size = 50)
	private Set<DatasheetItemRangeRule> rangeRules = new LinkedHashSet<>();

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@Builder
	public DatasheetItem(
			int itemNo,
			String groupName,
			String label,
			boolean inspectionItem,
			boolean array,
			Integer arraySize,
			InputType inputType,
			String unit,
			Integer decimalPlaces,
			BigDecimal minValue,
			BigDecimal maxValue,
			boolean allowNegative,
			boolean required,
			boolean skippable,
			String formula,
			int pdfPage,
			BigDecimal posX,
			BigDecimal posY,
			BigDecimal posWidth,
			BigDecimal posHeight,
			int sortOrder
	) {
		this.itemNo = itemNo;
		this.groupName = groupName;
		this.label = label;
		this.inspectionItem = inspectionItem;
		this.array = array;
		this.arraySize = arraySize;
		this.inputType = inputType;
		this.unit = unit;
		this.decimalPlaces = decimalPlaces;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.allowNegative = allowNegative;
		this.required = required;
		this.skippable = skippable;
		this.formula = formula;
		this.pdfPage = pdfPage;
		this.posX = posX;
		this.posY = posY;
		this.posWidth = posWidth;
		this.posHeight = posHeight;
		this.sortOrder = sortOrder;
	}

	public void addSlot(DatasheetItemSlot slot) {
		slots.add(slot);
		slot.setItem(this);
	}

	public void clearSlots() {
		slots.clear();
	}

	public void addRangeRule(DatasheetItemRangeRule rule) {
		rangeRules.add(rule);
		rule.setItem(this);
	}

	public void clearRangeRules() {
		rangeRules.clear();
	}

	public void updateMeta(
			String groupName,
			String label,
			boolean inspectionItem,
			boolean array,
			Integer arraySize,
			InputType inputType,
			String unit,
			Integer decimalPlaces,
			BigDecimal minValue,
			BigDecimal maxValue,
			boolean allowNegative,
			boolean required,
			boolean skippable,
			String formula,
			int sortOrder
	) {
		this.groupName = groupName;
		this.label = label;
		this.inspectionItem = inspectionItem;
		this.array = array;
		this.arraySize = arraySize;
		this.inputType = inputType;
		this.unit = unit;
		this.decimalPlaces = decimalPlaces;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.allowNegative = allowNegative;
		this.required = required;
		this.skippable = skippable;
		this.formula = formula;
		this.sortOrder = sortOrder;
	}

	public void updatePosition(
			int pdfPage,
			BigDecimal posX,
			BigDecimal posY,
			BigDecimal posWidth,
			BigDecimal posHeight
	) {
		this.pdfPage = pdfPage;
		this.posX = posX;
		this.posY = posY;
		this.posWidth = posWidth;
		this.posHeight = posHeight;
	}

	public void updateSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}

	@PrePersist
	void onCreate() {
		LocalDateTime now = LocalDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}
}
