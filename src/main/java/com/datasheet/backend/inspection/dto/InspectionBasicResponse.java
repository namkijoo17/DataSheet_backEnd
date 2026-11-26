package com.datasheet.backend.inspection.dto;

import com.datasheet.backend.inspection.Inspection;
import lombok.Builder;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Builder
public class InspectionBasicResponse {

	private String serialNo;
	private String unit;
	private String line;
	/** yyyy-MM-dd */
	private String inspectDate;
	private String inspector;
	private String inspectorChangeReason;
	private String temperature;
	private String humidity;
	private Map<String, String> parts;

	public static InspectionBasicResponse from(Inspection inspection) {
		return InspectionBasicResponse.builder()
				.serialNo(inspection.getSerialNo())
				.unit(inspection.getUnit())
				.line(inspection.getLine())
				.inspectDate(inspection.getInspectDate() != null ? inspection.getInspectDate().toString() : null)
				.inspector(inspection.getInspector())
				.inspectorChangeReason(inspection.getInspectorChangeReason())
				.temperature(inspection.getTemperature())
				.humidity(inspection.getHumidity())
				.parts(new LinkedHashMap<>(inspection.getParts()))
				.build();
	}
}
