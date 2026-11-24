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

@Entity
@Table(name = "datasheet_item_slots")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DatasheetItemSlot {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Setter(AccessLevel.PACKAGE)
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "item_id", nullable = false)
	private DatasheetItem item;

	@Column(name = "slot_index", nullable = false)
	private int slotIndex;

	@Column(length = 100, columnDefinition = "nvarchar(100)")
	private String label;

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

	@Builder
	public DatasheetItemSlot(
			int slotIndex,
			String label,
			int pdfPage,
			BigDecimal posX,
			BigDecimal posY,
			BigDecimal posWidth,
			BigDecimal posHeight
	) {
		this.slotIndex = slotIndex;
		this.label = label;
		this.pdfPage = pdfPage;
		this.posX = posX;
		this.posY = posY;
		this.posWidth = posWidth;
		this.posHeight = posHeight;
	}

	public void updateMeta(int slotIndex, String label) {
		this.slotIndex = slotIndex;
		this.label = label;
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
}
