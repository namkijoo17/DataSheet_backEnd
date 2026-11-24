package com.datasheet.backend.datasheet;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "datasheets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Datasheet {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 50)
	private String code;

	@Column(nullable = false, length = 200, columnDefinition = "nvarchar(200)")
	private String name;

	@Column(nullable = false)
	private int revision = 1;

	@Column(name = "file_name", nullable = false, length = 255, columnDefinition = "nvarchar(255)")
	private String fileName;

	@Column(name = "page_width", precision = 10, scale = 2)
	private BigDecimal pageWidth;

	@Column(name = "page_height", precision = 10, scale = 2)
	private BigDecimal pageHeight;

	@Column(nullable = false)
	private boolean active = true;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@OneToMany(mappedBy = "datasheet", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("sortOrder ASC, itemNo ASC")
	private List<DatasheetItem> items = new ArrayList<>();

	@Builder
	public Datasheet(
			String code,
			String name,
			int revision,
			String fileName,
			BigDecimal pageWidth,
			BigDecimal pageHeight,
			boolean active
	) {
		this.code = code;
		this.name = name;
		this.revision = revision;
		this.fileName = fileName;
		this.pageWidth = pageWidth;
		this.pageHeight = pageHeight;
		this.active = active;
	}

	public void addItem(DatasheetItem item) {
		items.add(item);
		item.setDatasheet(this);
	}

	public void removeItem(DatasheetItem item) {
		items.remove(item);
		item.setDatasheet(null);
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
